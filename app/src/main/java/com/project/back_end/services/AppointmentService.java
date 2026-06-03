package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.entity.Appointment;
import com.project.back_end.entity.Doctor;
import com.project.back_end.entity.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Core Service Layer component responsible for handling complex medical scheduling business logic rules.
 * Manages atomic database transformations across appointment transactions defensively.
 */
@Service // 1. Marks class as a Spring-managed Service Component containing transactional core logic
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService;
    private final com.project.back_end.services.Service helperService; // Reference to general validation helper service

    /**
     * 2. Constructor Injection for Dependencies.
     * Guarantees immutable fields and thread-safe dependency wire-ups at container launch.
     */
    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              TokenService tokenService,
                              com.project.back_end.services.Service helperService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
        this.helperService = helperService;
    }

    /**
     * 4. Book Appointment Method.
     * Persists a newly formulated appointment slot safely.
     * @param appointment Target transaction record details entity.
     * @return 1 if successfully saved, 0 if a fault or validation barrier drops.
     */
    @Transactional // 3. Wraps data mutations in a transaction boundary to ensure database write atomicity
    public int bookAppointment(Appointment appointment) {
        if (appointment == null) {
            return 0;
        }
        try {
            // Force status to baseline default state state (0 = Scheduled)
            appointment.setStatus(0);
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception error) {
            System.err.println("Critical fault intercepted inside bookAppointment pipeline: " + error.getMessage());
            return 0; // Failure indicator response mapping code
        }
    }

    /**
     * 5. Update Appointment Method.
     * Evaluates existing entities, performs business layer cross-checks, and rewrites records.
     * @param appointment Modification candidate wrapper class containing target values.
     * @return Standardized Response Map notifying client interfaces of operation outcomes.
     */
    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        Map<String, String> responseBody = new HashMap<>();

        if (appointment == null || appointment.getId() == null) {
            responseBody.put("message", "Invalid mutation request: Appointment identifier missing.");
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        }

        // Check if the appointment target metadata matches actual operational records
        Optional<Appointment> activeRecord = appointmentRepository.findById(appointment.getId());
        if (activeRecord.isEmpty()) {
            responseBody.put("message", "Target appointment record not found.");
            return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);
        }

        Appointment existingAppointment = activeRecord.get();

        // Check if the appointment status prevents further updating
        if (existingAppointment.getStatus() == 1) {
            responseBody.put("message", "Modifications rejected: Completed appointments cannot be altered.");
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        }

        try {
            // Leverage application base service layer to execute core collision/validation checks
            Map<String, String> serviceValidationErrors = helperService.validateAppointment(appointment);
            if (serviceValidationErrors != null && !serviceValidationErrors.isEmpty()) {
                return new ResponseEntity<>(serviceValidationErrors, HttpStatus.BAD_REQUEST);
            }

            // Sync updated tracking parameters definitions
            existingAppointment.setAppointmentTime(appointment.getAppointmentTime());
            if (appointment.getDoctor() != null) existingAppointment.setDoctor(appointment.getDoctor());
            if (appointment.getPatient() != null) existingAppointment.setPatient(appointment.getPatient());
            
            appointmentRepository.save(existingAppointment);
            
            responseBody.put("message", "Appointment updated successfully.");
            return new ResponseEntity<>(responseBody, HttpStatus.OK);

        } catch (Exception error) {
            System.error.println("Unexpected execution barrier during updateAppointment: " + error.getMessage());
            responseBody.put("message", "Internal server error processing update transaction.");
            return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 6. Cancel Appointment Method.
     * Validates account execution authorization contexts prior to performing eviction commands.
     * @param id Tracking index of target consultation row.
     * @param token Authentication tracking key payload bearer.
     */
    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        Map<String, String> responseBody = new HashMap<>();

        // Extract and decode credentials signature variables from header text tokens
        String callerEmail = tokenService.extractEmail(token);
        String callerRole = tokenService.extractRole(token);

        if (callerEmail == null) {
            responseBody.put("message", "Unauthorized action: Session credentials cannot be parsed.");
            return new ResponseEntity<>(responseBody, HttpStatus.UNAUTHORIZED);
        }

        Optional<Appointment> activeTarget = appointmentRepository.findById(id);
        if (activeTarget.isEmpty()) {
            responseBody.put("message", "Cancellation failed: Requested consultation index not found.");
            return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);
        }

        Appointment appointment = activeTarget.get();

        // Multi-tenant defensive wall check: Ensure patients can only delete their own records
        if ("loggedPatient".equalsIgnoreCase(callerRole) || "patient".equalsIgnoreCase(callerRole)) {
            Patient attachedPatient = appointment.getPatient();
            if (attachedPatient == null || !attachedPatient.getEmail().equalsIgnoreCase(callerEmail)) {
                responseBody.put("message", "Access Denied: You do not have permissions to revoke this record.");
                return new ResponseEntity<>(responseBody, HttpStatus.FORBIDDEN);
            }
        }

        try {
            appointmentRepository.delete(appointment);
            responseBody.put("message", "Appointment canceled successfully.");
            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        } catch (Exception error) {
            responseBody.put("message", "An error took place while wiping database record entries.");
            return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 7. Get Appointments Method.
     * Gathers a clinician's calendar slots for a targeted date range, optionally filtering by patient names.
     * @param pname Filter criteria lookup constraint text parameter string.
     * @param date Target calendar index filter parameters day tracker.
     * @param token Provider validation token key.
     */
    @Transactional(readOnly = true) // Read-only query optimization boundary
    public Map<String, Object> getAppointment(String pname, LocalDate date, String token) {
        Map<String, Object> masterResponse = new HashMap<>();
        List<AppointmentDTO> cleanDtoResultsList = new ArrayList<>();

        // Extract clinician details from session security properties context
        String doctorEmail = tokenService.extractEmail(token);
        if (doctorEmail == null) {
            masterResponse.put("appointments", cleanDtoResultsList);
            masterResponse.put("error", "Invalid or missing context session authentication token.");
            return masterResponse;
        }

        Doctor activeClinician = doctorRepository.findByEmail(doctorEmail);
        if (activeClinician == null) {
            masterResponse.put("appointments", cleanDtoResultsList);
            masterResponse.put("error", "Doctor account map matching credentials was not found.");
            return masterResponse;
        }

        // Establish the day window boundaries (00:00:00.000 to 23:59:59.999)
        LocalDateTime dayStartBoundary = date.atStartOfDay();
        LocalDateTime dayEndBoundary = date.atTime(LocalTime.MAX);

        List<Appointment> rawEntitiesList;

        // Route extraction calls via the optimal graph fetch repository channels
        if (pname != null && !pname.trim().isEmpty() && !"null".equalsIgnoreCase(pname.trim())) {
            rawEntitiesList = appointmentRepository
                .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                    activeClinician.getId(), pname.trim(), dayStartBoundary, dayEndBoundary
                );
        } else {
            rawEntitiesList = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(activeClinician.getId(), dayStartBoundary, dayEndBoundary);
        }

        // Map internal entities into structured flat DTOs to avoid circular reference crashes
        if (rawEntitiesList != null) {
            for (Appointment entity : rawEntitiesList) {
                AppointmentDTO dto = new AppointmentDTO(
                        entity.getId(),
                        entity.getDoctor() != null ? entity.getDoctor().getId() : null,
                        entity.getDoctor() != null ? entity.getDoctor().getName() : "N/A",
                        entity.getPatient() != null ? entity.getPatient().getId() : null,
                        entity.getPatient() != null ? entity.getPatient().getName() : "Unknown",
                        entity.getPatient() != null ? entity.getPatient().getEmail() : "N/A",
                        entity.getPatient() != null ? entity.getPatient().getPhone() : "N/A",
                        entity.getPatient() != null ? entity.getPatient().getAddress() : "N/A",
                        entity.getAppointmentTime(),
                        entity.getStatus()
                );
                cleanDtoResultsList.add(dto);
            }
        }

        masterResponse.put("appointments", cleanDtoResultsList);
        return masterResponse;
    }

    /**
     * 8. Change Status Method.
     * Perimeters modifications updating index workflow states (e.g. Scheduled to Completed).
     * @param status Next computational target value parameter (1 = Completed, 0 = Active).
     * @param id Targeted schedule item long record ID.
     */
    @Transactional
    public void changeStatus(int status, long id) {
        try {
            // Call modification repository query method inline to trigger clean SQL update writes
            appointmentRepository.updateStatus(status, id);
        } catch (Exception error) {
            System.err.println("Failed to mutate appointment status index tracking metric inside changeStatus: " + error.getMessage());
            throw new RuntimeException("Status migration failed due to database connection limits.", error);
        }
    }
}
