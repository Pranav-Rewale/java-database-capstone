package com.project.back_end.controllers;

import com.project.back_end.entity.Prescription;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Endpoint Controller managing NoSQL medical prescriptions.
 * Coordinates multi-role validation check firewalls, manages document persistence pipelines, 
 * triggers appointment status state changes, and houses the global validation exception interceptor.
 */
@RestController // 1. Designates this class as a REST controller that serves JSON responses natively.
@RequestMapping("${api.path}prescription") // Base API prefix constraint path configuration.
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final Service generalService;
    private final AppointmentService appointmentService;

    /**
     * 2. Constructor Injection for Dependencies.
     * Establishes safe dependency decoupling boundaries separating web controllers from service logic.
     */
    @Autowired
    public PrescriptionController(PrescriptionService prescriptionService, 
                                  Service generalService, 
                                  AppointmentService appointmentService) {
        this.prescriptionService = prescriptionService;
        this.generalService = generalService;
        this.appointmentService = appointmentService;
    }

    /**
     * 3. Define the savePrescription Method.
     * Handles HTTP POST requests to register a new prescription tied to an appointment.
     * Mapping: POST /prescription/{token}
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(
            @PathVariable("token") String token,
            @RequestBody Prescription prescription) {

        Map<String, String> responseBody = new HashMap<>();

        // Enforce role firewall isolation: Verify token context parameter belongs to a clinician
        Map<String, String> tokenErrors = generalService.validateToken(token, "doctor");
        if (tokenErrors != null && !tokenErrors.isEmpty()) {
            responseBody.put("message", "Access Denied: Invalid or expired doctor session credentials.");
            return new ResponseEntity<>(responseBody, HttpStatus.UNAUTHORIZED); // 401 Unauthorized
        }

        if (prescription == null || prescription.getAppointmentId() == null) {
            responseBody.put("message", "Malformed payload: Missing required prescription or appointment tracking references.");
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST); // 400 Bad Request
        }

        // Delegate persistence operation directly to your MongoDB-backed business service layer
        ResponseEntity<Map<String, String>> serviceResponse = prescriptionService.savePrescription(prescription);

        // If successfully saved, update corresponding appointment status to reflect prescription has been added (1 = Completed)
        if (serviceResponse.getStatusCode() == HttpStatus.CREATED || 
            "Prescription saved".equalsIgnoreCase(serviceResponse.getBody().get("message"))) {
            
            try {
                // Mutate state transition inside relational tables (status code 1 indicates consultation completed)
                appointmentService.changeStatus(1, prescription.getAppointmentId());
            } catch (Exception error) {
                System.err.println("Warning: Prescription was persisted, but appointment status migration failed: " + error.getMessage());
            }
        }

        return serviceResponse;
    }

    /**
     * 4. Define the getPrescription Method.
     * Retrieves prescription details linked to an appointment ID.
     * Mapping: GET /prescription/{appointmentId}/{token}
     */
    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescription(
            @PathVariable("appointmentId") Long appointmentId,
            @PathVariable("token") String token) {

        Map<String, Object> responseBody = new HashMap<>();

        // Enforce role firewall isolation to secure patient medical records privacy
        Map<String, String> tokenErrors = generalService.validateToken(token, "doctor");
        if (tokenErrors != null && !tokenErrors.isEmpty()) {
            responseBody.put("message", "Access Denied: Invalid or expired doctor session credentials.");
            return new ResponseEntity<>(responseBody, HttpStatus.UNAUTHORIZED); // 401 Unauthorized
        }

        // Delegate document query logic natively upstream to MongoDB service channels
        return prescriptionService.getPrescription(appointmentId);
    }

    // ===================================================================
    // GLOBAL VALIDATION EXCEPTION HANDLER (ValidationFailed Advice)
    // ===================================================================

    /**
     * Nested Global REST Controller Exception Advice intercepting parameter binding failures.
     * Automatically traps MethodArgumentNotValidException occurrences when incoming payload constraints 
     * (such as @NotNull, @Size, or @Email) are violated, translating them into standard client error logs.
     */
    @RestControllerAdvice // Combines ControllerAdvice and ResponseBody frameworks into a centralized global error gateway
    public static class ValidationFailed {

        /**
         * Intercepts body constraint verification faults, replacing standard heavy stack traces with error maps.
         * @param ex Caught MethodArgumentNotValidException parsing bundle.
         * @return Standardized 400 Bad Request envelope mapping affected data fields to validation notes.
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST) // Automatically forces HTTP status code 400 Bad Request
        public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
            Map<String, String> validationErrorsMap = new HashMap<>();

            // Extract structural constraint binding elements and parse individual validation breaches
            if (ex.getBindingResult() != null) {
                for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
                    // Map affected entities out uniformly using standardized message lookup keys
                    validationErrorsMap.put("message", fieldError.getDefaultMessage());
                    
                    // Optional descriptive debugging extension: mapping fields explicitly to their validation messages
                    // validationErrorsMap.put(fieldError.getField(), fieldError.getDefaultMessage());
                }
            }

            if (validationErrorsMap.isEmpty()) {
                validationErrorsMap.put("message", "Provided payload parameters failed model constraint verification.");
            }

            return new ResponseEntity<>(validationErrorsMap, HttpStatus.BAD_REQUEST);
        }
    }
}
