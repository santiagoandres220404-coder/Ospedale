/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package packagee;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 *
 * @author edangulo
 */
public class Appointment {
    
    private final String id;
    private Patient patient;
    private Doctor doctor;
    private Specialty specialty;
    private LocalDateTime datetime;
    private String reason;

    public void setReason(String reason) {
        this.reason = reason;
    }
    private boolean type;
    private ArrayList<Prescription> prescriptions;
    private AppointmentStatus status;
    private String diagnosis;
    private String observations;
    private String recommendedTreatment;
    private String followUp;

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public void setRecommendedTreatment(String recommendedTreatment) {
        this.recommendedTreatment = recommendedTreatment;
    }

    public void setFollowUp(String followUp) {
        this.followUp = followUp;
    }

    public Appointment(String id, Patient patient, Doctor doctor, Specialty specialty, LocalDateTime datetime, String reason, boolean type) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.specialty = specialty;
        this.datetime = datetime;
        this.reason = reason;
        this.type = type;
        this.status = AppointmentStatus.REQUESTED;
        this.prescriptions = new ArrayList<>();
        patient.addAppointment(this);
        doctor.addAppointment(this);
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public void setDatetime(LocalDateTime datetime) {
        this.datetime = datetime;
    }

    public void appendReason(String reason) {
        this.reason = this.reason + " | Reprogramacion: " + reason;
    }

    public String getId() {
        return id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public Specialty getSpecialty() {
        return specialty;
    }

    public LocalDateTime getDatetime() {
        return datetime;
    }

    public boolean isType() {
        return type;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public Patient getPatient() {
        return patient;
    }

    public boolean addPrescription(Prescription prescrip) {
        return this.prescriptions.add(prescrip);
    }

    public String getReason() {
        return reason;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public String getObservations() {
        return observations;
    }

    public String getRecommendedTreatment() {
        return recommendedTreatment;
    }

    public String getFollowUp() {
        return followUp;
    }
    
}
