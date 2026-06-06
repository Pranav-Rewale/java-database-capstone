package com.project.back_end.entity;

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

/**
 * Appointment entity representing a scheduled patient-doctor meeting.
 * Implements validation rules and explicit helper methods for UI processing.
 */
@Entity // Declares the class as a database-mapped entity
public class Appointment {

    @Id // Marks the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generates the primary key
    private Long id;

    @ManyToOne // Defines a many-to-one relationship
    @NotNull(message = "Doctor cannot be null") // Field must not be null
    private Doctor doctor;

    @ManyToOne // Defines a many-to-one relationship
    @NotNull(message = "Patient cannot be null") // Field must not be null
    private Patient patient;

    @Future(message = "Appointment time must be in the future") // Date/time must be in the future
    @NotNull(message = "Appointment time cannot be null")
    private LocalDateTime appointmentTime;

    @NotNull(message = "Status cannot be null")
    private int status; // 0 = Scheduled, 1 = Completed

    // --- Enhanced Fields ---

    @NotNull(message = "Reason for visit cannot be blank")
    @Size(min = 5, max = 250, message = "Provide a brief description between 5 and 250 characters")
    private String reasonForVisit;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String triageNotes;

    // --- Constructors ---

    // No-argument constructor required by JPA
    public Appointment() {
    }

    // Parameterized constructor for field initialization
    public Appointment(Doctor doctor, Patient patient, LocalDateTime appointmentTime, int status) {
        this.doctor = doctor;
        this.patient = patient;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    // Parameterized constructor including enhanced fields
    public Appointment(Doctor doctor, Patient patient, LocalDateTime appointmentTime, int status, String reasonForVisit, String triageNotes) {
        this.doctor = doctor;
        this.patient = patient;
        this.appointmentTime = appointmentTime;
        this.status = status;
        this.reasonForVisit = reasonForVisit;
        this.triageNotes = triageNotes;
    }

    // --- Transient Helper Methods for UI Logic ---

    /**
     * Calculates the end time of the appointment (1 hour after start time).
     * @Transient prevents this calculation logic from being stored in the database.
     */
    @Transient
    public LocalDateTime getEndTime() {
        return appointmentTime != null ? appointmentTime.plusHours(1) : null;
    }

    /**
     * Extracts only the date portion from the appointmentTime.
     */
    @Transient
    public LocalDate getAppointmentDate() {
        return appointmentTime != null ? appointmentTime.toLocalDate() : null;
    }

    /**
     * Extracts only the time portion from the appointmentTime.
     */
    @Transient
    public LocalTime getAppointmentTimeOnly() {
        return appointmentTime != null ? appointmentTime.toLocalTime() : null;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getReasonForVisit() {
        return reasonForVisit;
    }

    public void setReasonForVisit(String reasonForVisit) {
        this.reasonForVisit = reasonForVisit;
    }

    public String getTriageNotes() {
        return triageNotes;
    }

    public void setTriageNotes(String triageNotes) {
        this.triageNotes = triageNotes;
    }
}
