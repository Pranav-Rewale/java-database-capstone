package com.project.back_end.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne // Explicit relationship declaration [cite: 93]
    @NotNull
    private Doctor doctor;

    @ManyToOne
    @NotNull
    private Patient patient;

    @Future(message = "Appointment must be set for a later date") // Future scheduling rule [cite: 21, 94]
    @NotNull
    private LocalDateTime appointmentTime;

    @NotNull
    private int status; // 0 = Scheduled, 1 = Completed

    // --- Enhanced Fields ---

    @NotNull(message = "Reason for visit cannot be blank")
    @Size(min = 5, max = 250, message = "Provide a brief description between 5 and 250 characters")
    private String reasonForVisit;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String triageNotes;

    // --- Constructors ---
    public Appointment() {}

    // --- Transient Methods (Ignored by Database Schema Layer) [cite: 95] ---
    @Transient
    public LocalDateTime getEndTime() {
        return appointmentTime != null ? appointmentTime.plusHours(1) : null;
    }

    @Transient
    public LocalDate getAppointmentDate() {
        return appointmentTime != null ? appointmentTime.toLocalDate() : null;
    }

    @Transient
    public LocalTime getAppointmentTimeOnly() {
        return appointmentTime != null ? appointmentTime.toLocalTime() : null;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalDateTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getReasonForVisit() { return reasonForVisit; }
    public void setReasonForVisit(String reasonForVisit) { this.reasonForVisit = reasonForVisit; }

    public String getTriageNotes() { return triageNotes; }
    public void setTriageNotes(String triageNotes) { this.triageNotes = triageNotes; }
}
