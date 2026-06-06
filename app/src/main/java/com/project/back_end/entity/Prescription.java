package com.project.back_end.entity;

import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Prescription document model mapping to a NoSQL MongoDB collection.
 * Utilizes Spring Data MongoDB mappings alongside standard data validation constraints.
 */
@Document(collection = "prescriptions") // Marks the class as a MongoDB document mapped to a collection
public class Prescription {

    @Id // Marks the primary key in the MongoDB document
    private String id; // Commonly used for MongoDB's ObjectId string format

    @NotNull(message = "Patient name cannot be null") // Required field
    @Size(min = 3, max = 100, message = "Patient name must be between 3 and 100 characters") // Length constraint
    private String patientName;

    @NotNull(message = "Appointment ID cannot be null") // Required field
    private Long appointmentId; // Links this prescription to a relational Appointment ID record

    @NotNull(message = "Medication name cannot be null") // Required field
    @Size(min = 3, max = 100, message = "Medication name must be between 3 and 100 characters") // Length constraint
    private String medication;

    @NotNull(message = "Dosage details cannot be null") // Required field
    @Size(min = 3, max = 20, message = "Dosage must be between 3 and 20 characters")
    private String dosage;

    @Size(max = 200, message = "Doctor notes cannot exceed 200 characters") // Optional constraint
    private String doctorNotes; // Kept optional by omitting @NotNull

    // --- Enhanced Fields ---

    @Min(value = 0, message = "Refill counts cannot fall below zero")
    private int refillCount;

    @NotNull(message = "Target fulfillment pharmacy name is required")
    @Size(min = 3, max = 150)
    private String targetedPharmacy;

    // --- Constructors ---

    // No-argument constructor required by Spring Data MongoDB for document reconstruction
    public Prescription() {
    }

    // Parameterized constructor to easily initialize core prescription metadata
    public Prescription(String patientName, Long appointmentId, String medication, String dosage, String doctorNotes) {
        this.patientName = patientName;
        this.appointmentId = appointmentId;
        this.medication = medication;
        this.dosage = dosage;
        this.doctorNotes = doctorNotes;
    }

    // Parameterized constructor including enhanced fields
    public Prescription(String patientName, Long appointmentId, String medication, String dosage, String doctorNotes, int refillCount, String targetedPharmacy) {
        this.patientName = patientName;
        this.appointmentId = appointmentId;
        this.medication = medication;
        this.dosage = dosage;
        this.doctorNotes = doctorNotes;
        this.refillCount = refillCount;
        this.targetedPharmacy = targetedPharmacy;
    }

    // --- Getters and Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getMedication() {
        return medication;
    }

    public void setMedication(String medication) {
        this.medication = medication;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getDoctorNotes() {
        return doctorNotes;
    }

    public void setDoctorNotes(String doctorNotes) {
        this.doctorNotes = doctorNotes;
    }

    public int getRefillCount() {
        return refillCount;
    }

    public void setRefillCount(int refillCount) {
        this.refillCount = refillCount;
    }

    public String getTargetedPharmacy() {
        return targetedPharmacy;
    }

    public void setTargetedPharmacy(String targetedPharmacy) {
        this.targetedPharmacy = targetedPharmacy;
    }
}
