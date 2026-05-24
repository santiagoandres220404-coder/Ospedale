package packagee;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.json.JSONArray;

public class AppointmentController {
    private final HospitalStore store;

    public AppointmentController() {
        store = HospitalStore.getInstance();
        store.loadUsersFromJson();
    }

    public Response requestAppointment(long patientId, Long doctorId, Specialty specialty, String date,
            String time, String reason, boolean inPerson) {
        Patient patient = store.findPatient(patientId);
        if (patient == null) {
            return Response.error(StatusCode.NOT_FOUND, "Paciente no encontrado.");
        }
        LocalDate parsedDate = Validation.parseDate(date);
        LocalTime parsedTime = Validation.parseQuarterHour(time);
        if (parsedDate == null || parsedTime == null) {
            return Response.error(StatusCode.BAD_REQUEST, "La fecha debe ser AAAA-MM-DD y la hora hh:mm en cuartos.");
        }
        LocalDateTime datetime = LocalDateTime.of(parsedDate, parsedTime);
        Doctor doctor = resolveAvailableDoctor(doctorId, specialty, datetime);
        if (doctor == null) {
            return Response.error(StatusCode.CONFLICT, "No hay un doctor disponible para ese horario.");
        }
        String id = nextAppointmentId(patientId);
        Appointment appointment = new Appointment(id, patient, doctor, doctor.getSpecialty(), datetime, reason, inPerson);
        store.getAppointments().add(appointment);
        return Response.created("Cita solicitada.", store.serializeAppointment(appointment).toString());
    }

    public Response requestAppointment(String patientId, String doctorId, String specialtyName, String date,
            String time, String reason, boolean inPerson) {
        try {
            Long parsedDoctorId = doctorId == null || doctorId.length() == 0 ? null : Long.parseLong(doctorId);
            Specialty specialty = specialtyName == null || specialtyName.length() == 0 ? null : Specialty.valueOf(specialtyName);
            return requestAppointment(Long.parseLong(patientId), parsedDoctorId, specialty, date, time, reason, inPerson);
        } catch (IllegalArgumentException ex) {
            return Response.error(StatusCode.BAD_REQUEST, "Datos de cita invalidos.");
        }
    }

    public Response acceptAppointment(String appointmentId) {
        Appointment appointment = requireAppointment(appointmentId);
        if (appointment == null) {
            return Response.error(StatusCode.NOT_FOUND, "Cita no encontrada.");
        }
        appointment.setStatus(AppointmentStatus.PENDING);
        return Response.ok("Cita aceptada.", store.serializeAppointment(appointment).toString());
    }

    public Response completeAppointment(String appointmentId, String diagnosis, String observations,
            String recommendedTreatment, String followUp) {
        Appointment appointment = requireAppointment(appointmentId);
        if (appointment == null) {
            return Response.error(StatusCode.NOT_FOUND, "Cita no encontrada.");
        }
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setDiagnosis(diagnosis);
        appointment.setObservations(observations);
        appointment.setRecommendedTreatment(recommendedTreatment);
        appointment.setFollowUp(followUp);
        return Response.ok("Cita completada.", store.serializeAppointment(appointment).toString());
    }

    public Response cancelAppointment(String appointmentId) {
        Appointment appointment = requireAppointment(appointmentId);
        if (appointment == null) {
            return Response.error(StatusCode.NOT_FOUND, "Cita no encontrada.");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            return Response.error(StatusCode.CONFLICT, "No se puede cancelar una cita completada.");
        }
        appointment.setStatus(AppointmentStatus.CANCELED);
        return Response.ok("Cita cancelada.", store.serializeAppointment(appointment).toString());
    }

    public Response rescheduleAppointment(String appointmentId, String time, String reason) {
        Appointment appointment = requireAppointment(appointmentId);
        if (appointment == null) {
            return Response.error(StatusCode.NOT_FOUND, "Cita no encontrada.");
        }
        LocalTime parsedTime = Validation.parseQuarterHour(time);
        if (parsedTime == null) {
            return Response.error(StatusCode.BAD_REQUEST, "La hora debe seguir el formato hh:mm y estar en cuartos.");
        }
        LocalDateTime datetime = LocalDateTime.of(appointment.getDatetime().toLocalDate(), parsedTime);
        if (!isDoctorAvailable(appointment.getDoctor(), datetime, appointment)) {
            return Response.error(StatusCode.CONFLICT, "El doctor no esta disponible en ese horario.");
        }
        appointment.setDatetime(datetime);
        appointment.appendReason(reason);
        return Response.ok("Cita reprogramada.", store.serializeAppointment(appointment).toString());
    }

    public Response prescribe(String appointmentId, String medicationName, double dose, String administrationRoute,
            int treatmentDuration, String additionalInstructions, int frecuency) {
        Appointment appointment = requireAppointment(appointmentId);
        if (appointment == null) {
            return Response.error(StatusCode.NOT_FOUND, "Cita no encontrada.");
        }
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            return Response.error(StatusCode.CONFLICT, "Solo se puede prescribir en citas aceptadas.");
        }
        Prescription prescription = new Prescription(appointment, medicationName, dose, administrationRoute,
                treatmentDuration, additionalInstructions, frecuency);
        appointment.addPrescription(prescription);
        return Response.created("Medicamento prescrito.", store.serializeAppointment(appointment).toString());
    }

    public Response prescribe(String appointmentId, String medicationName, String dose, String administrationRoute,
            String treatmentDuration, String additionalInstructions, String frecuency) {
        try {
            return prescribe(appointmentId, medicationName, Double.parseDouble(dose), administrationRoute,
                    Integer.parseInt(treatmentDuration), additionalInstructions, Integer.parseInt(frecuency));
        } catch (NumberFormatException ex) {
            return Response.error(StatusCode.BAD_REQUEST, "Dosis, duracion y frecuencia deben ser numericas.");
        }
    }

    public Response getPatientAppointments(long patientId) {
        JSONArray array = new JSONArray();
        for (Appointment appointment : sortedAppointments()) {
            if (appointment.getPatient().getId() == patientId) {
                array.put(store.serializeAppointment(appointment));
            }
        }
        return Response.ok("Citas del paciente.", array.toString());
    }

    public Response getPatientAppointments(String patientId) {
        try {
            return getPatientAppointments(Long.parseLong(patientId));
        } catch (NumberFormatException ex) {
            return Response.error(StatusCode.BAD_REQUEST, "El paciente debe ser numerico.");
        }
    }

    public Response getDoctorAppointments(long doctorId, boolean onlyPending) {
        JSONArray array = new JSONArray();
        for (Appointment appointment : sortedAppointments()) {
            if (appointment.getDoctor().getId() == doctorId
                    && (!onlyPending || appointment.getStatus() == AppointmentStatus.PENDING)) {
                array.put(store.serializeAppointment(appointment));
            }
        }
        return Response.ok("Citas del doctor.", array.toString());
    }

    private ArrayList<Appointment> sortedAppointments() {
        ArrayList<Appointment> copy = new ArrayList<>(store.getAppointments());
        Collections.sort(copy, new Comparator<Appointment>() {
            public int compare(Appointment a, Appointment b) {
                return b.getDatetime().compareTo(a.getDatetime());
            }
        });
        return copy;
    }

    private Appointment requireAppointment(String appointmentId) {
        return store.findAppointment(appointmentId);
    }

    private Doctor resolveAvailableDoctor(Long doctorId, Specialty specialty, LocalDateTime datetime) {
        if (doctorId != null) {
            Doctor doctor = store.findDoctor(doctorId);
            if (doctor != null && isDoctorAvailable(doctor, datetime, null)) {
                return doctor;
            }
            return null;
        }
        for (User user : store.getUsers()) {
            if (user instanceof Doctor) {
                Doctor doctor = (Doctor) user;
                if (doctor.getSpecialty() == specialty && isDoctorAvailable(doctor, datetime, null)) {
                    return doctor;
                }
            }
        }
        return null;
    }

    private boolean isDoctorAvailable(Doctor doctor, LocalDateTime datetime, Appointment ignored) {
        for (Appointment appointment : store.getAppointments()) {
            if (appointment == ignored) {
                continue;
            }
            if (appointment.getDoctor().getId() == doctor.getId()
                    && appointment.getStatus() != AppointmentStatus.CANCELED
                    && appointment.getDatetime().equals(datetime)) {
                return false;
            }
        }
        return true;
    }

    private String nextAppointmentId(long patientId) {
        int count = 0;
        String prefix = "A-" + patientId + "-";
        for (Appointment appointment : store.getAppointments()) {
            if (appointment.getId().startsWith(prefix)) {
                count++;
            }
        }
        return prefix + String.format("%04d", count);
    }
}
