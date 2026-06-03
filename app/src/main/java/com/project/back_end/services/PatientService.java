package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.entity.Appointment;
import com.project.back_end.entity.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core Service Layer component responsible for handling patient management and medical history tracking.
 * Coordinates token verification cross-checks, security mappings, and conditional status groupings.
 */
@Service // 1. Marks this class as a Spring-managed Service component containing business logic.
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    /**
     * 2. Constructor Injection for Dependencies.
     * Guarantees immutable fields and thread-safe dependency wire-ups at container launch.
     */
    @Autowired
    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    /**
     * 3. createPatient Method.
     * Saves a brand new patient record in the database system.
     * @param patient The patient details entity to be saved.
     * @return 1 on success, and 0 on failure.
     */
    @Transactional
    public int createPatient(Patient patient) {
        if (patient == null) {
            return 0;
        }
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception error) {
            // 9. Handles exceptions using try-catch blocks and logs any tracking errors
            System.err.println("Critical fault intercepted inside createPatient database operations: " + error.getMessage());
            return 0;
        }
    }

    /**
     * 4. getPatientAppointment Method.
     * Retrieves a list of appointments for a specific patient after authorizing ownership via token.
     * @param id The unique patient ID.
     * @param token The active user's structural session token.
     * @return ResponseEntity holding appointment list records or an error feedback dictionary block.
     */
    @Transactional(readOnly = true) // Ensures data consistency during the transaction window
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 8. Extract the patient's email from the provided token
            String extractedEmail = tokenService.extractEmail(token);
            if (extractedEmail == null) {
                response.put("message", "Unauthorized action: Session context cannot be resolved.");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            Patient currentPatient = patientRepository.findByEmail(extractedEmail);
            if (currentPatient == null || !currentPatient.getId().equals(id)) {
                response.put("message", "Access Denied: Requested profile index mismatch.");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }

            List<Appointment> entities = appointmentRepository.findByPatientId(id);
            // 10. Map internal entity data into a collection of AppointmentDTO models
            List<AppointmentDTO> dtos = convertToDtoList(entities);

            response.put("appointments", dtos);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception error) {
            System.err.println("Unexpected failure inside getPatientAppointment workflow: " + error.getMessage());
            response.put("message", "An error occurred while fetching medical history schedules.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 5. filterByCondition Method.
     * Groups appointment arrays based on time state metrics (past or future).
     * @param condition Time filter constraint ("past" or "future").
     * @param id The unique patient ID.
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long id) {
        Map<String, Object> response = new HashMap<>();
        
        if (condition == null || condition.trim().isEmpty()) {
            response.put("message", "Malformed parameters: Filter condition keyword cannot be blank.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String normalizedCondition = condition.trim().toLowerCase();
        int statusCode;

        // Maps status criteria: 0 for future/scheduled, 1 for past/completed
        if ("future".equals(normalizedCondition)) {
            statusCode = 0;
        } else if ("past".equals(normalizedCondition)) {
            statusCode = 1;
        } else {
            response.put("message", "Invalid filter parameter configuration. Expected 'past' or 'future'.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        try {
            List<Appointment> filteredEntities = appointmentRepository
                    .findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, statusCode);
            List<AppointmentDTO> dtos = convertToDtoList(filteredEntities);

            response.put("appointments", dtos);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception error) {
            System.err.println("Unexpected exception trapped inside filterByCondition pipeline: " + error.getMessage());
            response.put("message", "Internal error processing condition query filters.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 6. filterByDoctor Method.
     * Filters appointments based on a doctor's name using loose case-insensitive matching.
     * @param name Part of the doctor's name string criteria.
     * @param patientId The unique patient ID.
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByDoctor(String name, Long patientId) {
        Map<String, Object> response = new HashMap<>();
        String cleanName = (name != null) ? name.trim() : "";

        try {
            List<Appointment> filteredEntities = appointmentRepository.filterByDoctorNameAndPatientId(cleanName, patientId);
            List<AppointmentDTO> dtos = convertToDtoList(filteredEntities);

            response.put("appointments", dtos);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception error) {
            System.err.println("Unexpected exception trapped inside filterByDoctor pipeline: " + error.getMessage());
            response.put("message", "Internal error processing clinician search filters.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 7. filterByDoctorAndCondition Method.
     * Combines both filtering vectors (partial doctor name and timeline status context) concurrently.
     * @param condition Time condition string selector ("past" or "future").
     * @param name The clinician pattern text search element.
     * @param patientId The unique patient ID.
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String condition, String name, long patientId) {
        Map<String, Object> response = new HashMap<>();
        
        if (condition == null || condition.trim().isEmpty()) {
            response.put("message", "Filter operations request missing time condition modifier.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String normalizedCondition = condition.trim().toLowerCase();
        int statusCode;

        if ("future".equals(normalizedCondition)) {
            statusCode = 0;
        } else if ("past".equals(normalizedCondition)) {
            statusCode = 1;
        } else {
            response.put("message", "Invalid timeline condition option provided.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String cleanName = (name != null) ? name.trim() : "";

        try {
            List<Appointment> filteredEntities = appointmentRepository
                    .filterByDoctorNameAndPatientIdAndStatus(cleanName, patientId, statusCode);
            List<AppointmentDTO> dtos = convertToDtoList(filteredEntities);

            response.put("appointments", dtos);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception error) {
            System.err.println("Unexpected exception trapped inside filterByDoctorAndCondition pipeline: " + error.getMessage());
            response.put("message", "Internal database error parsing joint criteria limits.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 8. getPatientDetails Method.
     * Unpacks user information dictionaries directly mapped from active credential text blocks.
     * @param token Authentication tracking key payload bearer.
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            String extractedEmail = tokenService.extractEmail(token);
            if (extractedEmail == null) {
                response.put("message", "Session missing or user verification context is invalid.");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            Patient patient = patientRepository.findByEmail(extractedEmail);
            if (patient == null) {
                response.put("message", "Patient profile record mapping target was not found.");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // Expose structural profile parameters securely without compromising passwords
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("id", patient.getId());
            profileData.put("name", patient.getName());
            profileData.put("email", patient.getEmail());
            profileData.put("phone", patient.getPhone());
            profileData.put("address", patient.getAddress());

            response.put("patient", profileData);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception error) {
            System.err.println("Unexpected execution barrier inside getPatientDetails: " + error.getMessage());
            response.put("message", "Internal server error fetching demographic parameters.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Private Internal Helper Utility.
     * Converts raw bi-directional Appointment entities to flat DTOs to protect sensitive infrastructure layers.
     */
    private List<AppointmentDTO> convertToDtoList(List<Appointment> entities) {
        List<AppointmentDTO> dtos = new ArrayList<>();
        if (entities == null) return dtos;

        for (Appointment app : entities) {
            AppointmentDTO dto = new AppointmentDTO(
                    app.getId(),
                    app.getDoctor() != null ? app.getDoctor().getId() : null,
                    app.getDoctor() != null ? app.getDoctor().getName() : "N/A",
                    app.getPatient() != null ? app.getPatient().getId() : null,
                    app.getPatient() != null ? app.getPatient().getName() : "Unknown",
                    app.getPatient() != null ? app.getPatient().getEmail() : "N/A",
                    app.getPatient() != null ? app.getPatient().getPhone() : "N/A",
                    app.getPatient() != null ? app.getPatient().getAddress() : "N/A",
                    app.getAppointmentTime(),
                    app.getStatus()
            );
            dtos.add(dto);
        }
        return dtos;
    }
}
