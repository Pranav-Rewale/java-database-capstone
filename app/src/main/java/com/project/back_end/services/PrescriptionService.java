package com.project.back_end.services;

import com.project.back_end.entity.Prescription;
import com.project.back_end.repo.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core Service Layer component responsible for handling NoSQL medical prescriptions business logic rules.
 * Manages validation constraints ensuring strict one-to-one mapping alignment between 
 * consultation appointments and their corresponding medication document payloads.
 */
@Service // 1. Marks this class as a Spring-managed Service component containing business logic.
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    /**
     * 2. Constructor Injection for Dependencies.
     * Guarantees immutable fields and thread-safe dependency wire-ups at container launch.
     */
    @Autowired
    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    /**
     * 3. savePrescription Method.
     * Validates business logic limits and persists a new medical prescription into the MongoDB document store.
     * @param prescription The prescription details document to be saved.
     * @return ResponseEntity holding operational transaction confirmation text or conflict status mappings.
     */
    public ResponseEntity<Map<String, String>> savePrescription(Prescription prescription) {
        Map<String, String> response = new HashMap<>();

        if (prescription == null || prescription.getAppointmentId() == null) {
            response.put("message", "Malformed request: Submission details or appointment assignment mapping is missing.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        try {
            // Before saving, ensure that a prescription does not already exist for the same appointment ID
            List<Prescription> existingPrescriptions = prescriptionRepository.findByAppointmentId(prescription.getAppointmentId());
            
            if (existingPrescriptions != null && !existingPrescriptions.isEmpty()) {
                // If a prescription exists, return a 400 Bad Request to block multi-prescription creation conflicts
                response.put("message", "Prescription already exists for this appointment.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // If no duplicate boundaries drop, persist the new prescription document safely
            prescriptionRepository.save(prescription);
            
            response.put("message", "Prescription saved");
            return new ResponseEntity<>(response, HttpStatus.CREATED); // Returns a 201 Created status with success message

        } catch (Exception error) {
            // 5. Handles potential database connection or server exceptions using try-catch blocks gracefully
            System.err.println("Critical fault intercepted inside savePrescription execution stream: " + error.getMessage());
            response.put("message", "Internal server error: Unable to record prescription parameters.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // HTTP 500 Internal Server Error
        }
    }

    /**
     * 4. getPrescription Method.
     * Unpacks medical drug delivery parameters mapped to a target consultation session tracker.
     * @param appointmentId The unique relational identifier tracking the original consultation slot.
     * @return ResponseEntity wrapping the matched prescription dictionary document, empty array notifications, or server warnings.
     */
    public ResponseEntity<Map<String, Object>> getPrescription(Long appointmentId) {
        Map<String, Object> response = new HashMap<>();

        if (appointmentId == null) {
            response.put("message", "Malformed query: Appointment tracking parameter is unassigned.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        try {
            // Fetch potential document lists using optimal derived query mappings
            List<Prescription> prescriptionsList = prescriptionRepository.findByAppointmentId(appointmentId);

            if (prescriptionsList == null || prescriptionsList.isEmpty()) {
                // Handle edge cases where no prescriptions are found gracefully by providing a meaningful response
                response.put("message", "No prescriptions found for the given appointment.");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // Return the target prescription data packet within a 200 OK status container map wrapper
            response.put("prescription", prescriptionsList.get(0)); // Extract primary prescription record payload
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception error) {
            // Logs execution barriers and falls back to server fault states to prevent framework drops
            System.err.println("Unexpected failure caught within getPrescription data retrieval: " + error.getMessage());
            response.put("message", "An error occurred while fetching prescription parameters from database collections.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // HTTP 500 Internal Server Error
        }
    }
}
