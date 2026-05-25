package packagee;

import java.time.LocalDate;
import org.json.JSONArray;

public class HospitalizationController {
    private final HospitalStore store;

    public HospitalizationController() {
        store = HospitalStore.getInstance();
        store.loadUsersFromJson();
    }

    public Response requestHospitalization(long patientId, long doctorId, String date, String reason,
            RoomType roomType, String observations) {
        Patient patient = store.findPatient(patientId);
        Doctor doctor = store.findDoctor(doctorId);
        LocalDate parsedDate = Validation.parseDate(date);
        if (patient == null || doctor == null) {
            return Response.error(StatusCode.NOT_FOUND, "Paciente o doctor no encontrado.");
        }
        if (parsedDate == null) {
            return Response.error(StatusCode.BAD_REQUEST, "La fecha debe seguir el formato AAAA-MM-DD.");
        }
        Hospitalization hospitalization = new Hospitalization(nextHospitalizationId(patientId), patient, doctor,
                parsedDate, reason, roomType, observations);
        store.getHospitalizations().add(hospitalization);
        store.notifyObservers();
        return Response.created("Hospitalizacion solicitada.", store.serializeHospitalization(hospitalization).toString());
    }

    public Response requestHospitalization(String patientId, String doctorId, String date, String reason,
            RoomType roomType, String observations) {
        try {
            return requestHospitalization(Long.parseLong(patientId), Long.parseLong(doctorId), date, reason,
                    roomType, observations);
        } catch (NumberFormatException ex) {
            return Response.error(StatusCode.BAD_REQUEST, "Paciente y doctor deben ser numericos.");
        }
    }

    public Response approveHospitalization(String hospitalizationId) {
        Hospitalization hospitalization = requireHospitalization(hospitalizationId);
        if (hospitalization == null) {
            return Response.error(StatusCode.NOT_FOUND, "Hospitalizacion no encontrada.");
        }
        hospitalization.setStatus(HospitalizationStatus.ONGOING);
        store.notifyObservers();
        return Response.ok("Hospitalizacion aprobada.", store.serializeHospitalization(hospitalization).toString());
    }

    public Response denyHospitalization(String hospitalizationId) {
        Hospitalization hospitalization = requireHospitalization(hospitalizationId);
        if (hospitalization == null) {
            return Response.error(StatusCode.NOT_FOUND, "Hospitalizacion no encontrada.");
        }
        hospitalization.setStatus(HospitalizationStatus.CANCELED);
        store.notifyObservers();
        return Response.ok("Hospitalizacion cancelada.", store.serializeHospitalization(hospitalization).toString());
    }

    public Response cancelHospitalization(String hospitalizationId) {
        return denyHospitalization(hospitalizationId);
    }

    public Response sendToHospitalizationFromAppointment(String appointmentId, String date, String reason,
            RoomType roomType, String observations) {
        Appointment appointment = store.findAppointment(appointmentId);
        LocalDate parsedDate = Validation.parseDate(date);
        if (appointment == null) {
            return Response.error(StatusCode.NOT_FOUND, "Cita no encontrada.");
        }
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            return Response.error(StatusCode.CONFLICT, "Solo se puede hospitalizar directamente desde citas aceptadas.");
        }
        if (parsedDate == null) {
            return Response.error(StatusCode.BAD_REQUEST, "La fecha debe seguir el formato AAAA-MM-DD.");
        }
        appointment.setStatus(AppointmentStatus.COMPLETED);
        Hospitalization hospitalization = new Hospitalization(nextHospitalizationId(appointment.getPatient().getId()),
                appointment.getPatient(), appointment.getDoctor(), parsedDate, reason, roomType, observations,
                HospitalizationStatus.ONGOING);
        store.getHospitalizations().add(hospitalization);
        store.notifyObservers();
        return Response.created("Hospitalizacion creada desde cita.", store.serializeHospitalization(hospitalization).toString());
    }

    public Response getHospitalizations() {
        JSONArray array = new JSONArray();
        for (Hospitalization hospitalization : store.getHospitalizations()) {
            array.put(store.serializeHospitalization(hospitalization));
        }
        return Response.ok("Hospitalizaciones.", array.toString());
    }

    private Hospitalization requireHospitalization(String hospitalizationId) {
        return store.findHospitalization(hospitalizationId);
    }

    private String nextHospitalizationId(long patientId) {
        int count = 0;
        String prefix = "H-" + patientId + "-";
        for (Hospitalization hospitalization : store.getHospitalizations()) {
            if (hospitalization.getId().startsWith(prefix)) {
                count++;
            }
        }
        return prefix + String.format("%04d", count);
    }
}
