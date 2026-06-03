package com.project.back_end.controllers;

import com.project.back_end.entity.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Endpoint Controller managing public and private scheduled clinical consultations.
 * Centralizes standard lifecycle routes dealing with bookings, updates, history fetches, and deletions.
 */
@RestController // 1. Defines the class as a REST API controller handling web requests and returning JSON streams
@RequestMapping("/appointments") // Centralizes base URL path mappings for all sub-endpoints
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final Service generalService;

    /**
     * 2. Constructor-based Dependency Injection.
     * Automatically wires up domain services and orchestration validation helper instances at runtime.
     */
    @Autowired
    public AppointmentController(AppointmentService appointmentService, Service generalService) {
        this.appointmentService = appointmentService;
        this.generalService = generalService;
    }

    /**
     * 3. Define the getAppointments Method.
     * Handles HTTP GET requests to fetch calendar appointments based on target dates and optional client lookups.
     * Target path example: GET /appointments/2026-06-03/Pranav/xyzTokenString
     */
    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable("patientName") String patientName,
            @PathVariable("token") String token) {

        Map<String, Object> responseBody = new HashMap<>();

        // Enforce role firewall isolation: Only authenticated doctors can query daily schedules
        Map<String, String> tokenErrors = generalService.validateToken(token, "doctor");
        if (tokenErrors != null && !tokenErrors.isEmpty()) {
            responseBody.put("message", "Access Denied: Invalid or expired clinician credentials.");
            return new ResponseEntity<>(responseBody, HttpStatus.UNAUTHORIZED);
        }

        // Delegate query lookup directly to background service layers
        Map<String, Object> appointmentData = appointmentService.getAppointment(patientName, date, token);
        return new ResponseEntity<>(appointmentData, HttpStatus.OK);
    }

    /**
     * 4. Define the bookAppointment Method.
     * Handles HTTP POST requests to structure and register a brand new consultation window.
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(
            @PathVariable("token") String token,
            @RequestBody Appointment appointment) {

        Map<String, String> responseBody = new HashMap<>();

        // Enforce role validation: Verify the incoming call stems from a legitimate patient session context
        Map<String, String> tokenErrors = generalService.validateToken(token, "patient");
        if (tokenErrors != null && !tokenErrors.isEmpty()) {
            responseBody.put("message", "Unauthorized action: Patient authentication failed.");
            return new ResponseEntity<>(responseBody, HttpStatus.UNAUTHORIZED);
        }

        // Cross-check clinical booking matrices to catch scheduling overlaps or unmapped IDs
        int checkValidationCode = generalService.validateAppointment(appointment);
        if (checkValidationCode == -1) {
            responseBody.put("message", "Booking rejected: The assigned doctor record does not exist in the system.");
            return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);
        } else if (checkValidationCode == 0) {
            responseBody.put("message", "Booking rejected: Requested time slot is already taken or unavailable.");
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        }

        // Proceed to finalize structural persistence write transaction
        int saveSuccessCode = appointmentService.bookAppointment(appointment);
        if (saveSuccessCode == 1) {
            responseBody.put("message", "Appointment saved successfully.");
            return new ResponseEntity<>(responseBody, HttpStatus.CREATED); // Return 201 Created status code upon success
        } else {
            responseBody.put("message", "Internal error occurred while attempting to record database entry.");
            return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 5. Define the updateAppointment Method.
     * Handles HTTP PUT requests to safely overwrite structural metadata configurations for active appointments.
     */
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(
            @PathVariable("token") String token,
            @RequestBody Appointment appointment) {

        // Validate the path token ensures proper identity alignment permissions
        Map<String, String> tokenErrors = generalService.validateToken(token, "patient");
        if (tokenErrors != null && !tokenErrors.isEmpty()) {
            Map<String, String> unauthResponse = new HashMap<>();
            unauthResponse.put("message", "Unauthorized access token: Update action blocked.");
            return new ResponseEntity<>(unauthResponse, HttpStatus.UNAUTHORIZED);
        }

        // Delegate modification checks and database updates directly to the appointment service layer
        return appointmentService.updateAppointment(appointment);
    }

    /**
     * 6. Define the cancelAppointment Method.
     * Handles HTTP DELETE requests to completely drop and purge an active appointment from row indices.
     */
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(
            @PathVariable("id") long id,
            @PathVariable("token") String token) {

        // Validate path token parameters to ensure secure data access bounds
        Map<String, String> tokenErrors = generalService.validateToken(token, "patient");
        if (tokenErrors != null && !tokenErrors.isEmpty()) {
            Map<String, String> unauthResponse = new HashMap<>();
            unauthResponse.put("message", "Unauthorized access token: Cancellation action blocked.");
            return new ResponseEntity<>(unauthResponse, HttpStatus.UNAUTHORIZED);
        }

        // Delegate secure cross-tenant checking and cascading row deletion logic to your business layer
        return appointmentService.cancelAppointment(id, token);
    }
}
