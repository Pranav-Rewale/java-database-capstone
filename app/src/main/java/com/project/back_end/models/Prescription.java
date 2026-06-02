package com.project.back_end.models;

import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "prescriptions") // Directs object maps to NoSQL collections [cite: 98, 100]
public class Prescription {

    @Id // Standard identifier mapping inside document trees [cite: 102]
    private String id;

    @NotNull
    @Size(min = 3, max = 100)
    private String patientName;

    @NotNull
    private Long appointmentId;

    @NotNull
    @Size(min = 3, max = 100)
    private String medication;

    @NotNull
    @Size(min = 3, max = 20)
    private String dosage;

    @Size(max = 200)
    private String doctorNotes;

    // --- Enhanced Fields ---

    @Min(value = 0, message = "Refill counts cannot fall below zero")
    private int refillCount;

    @NotNull(message = "Target fulfillment pharmacy name is required")
    @Size(min = 3, max = 150)
    private String targetedPharmacy;

    // --- Constructors ---
    public Prescription() {}

    // --- Getters and Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public String getMedication() { return medication; }
    public void setMedication(String medication) { this.medication = medication; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getDoctorNotes() { return doctorNotes; }
    public void setDoctorNotes(String doctorNotes) { this.doctorNotes = doctorNotes; }

    public int getRefillCount() { return refillCount; }
    public void setRefillCount(int refillCount) { this.refillCount = refillCount; }

    public String getTargetedPharmacy() { return targetedPharmacy; }
    public void setTargetedPharmacy(String targetedPharmacy) { this.targetedPharmacy = targetedPharmacy; }
}
