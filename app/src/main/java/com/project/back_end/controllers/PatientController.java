package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.entity.Patient;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Endpoint Controller managing Patient demographic routing and medical scheduling tables.
 * Coordinates client registration boundaries, token check firewalls, profile queries,
 * and parameterized history filter channels.
 */
@RestController // 1. Designates this class as a REST API controller for patient-related operations returning JSON responses.
@RequestMapping("/patient") // Base API path prefix constraint grouping patient functionalities under a common route.
public class PatientController {

    private final PatientService patientService;
    private final Service generalService;

    /**
     * 2. Constructor Injection for Dependencies.
     * Establishes reliable, thread-safe dependency wire-ups at container launch.
     */
    @Autowired
    public PatientController(PatientService patientService, Service generalService) {
        this.patientService = patientService;
        this.generalService = generalService;
    }

    /**
     * 1. Get Patient Details Endpoint.
     * Decodes session metadata to expose a logged-in patient's personal records securely.
     * Mapping: GET /patient/{token}
     */
    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatient(@PathVariable("token") String token) {
        Map<String, Object> responseBody = new HashMap<>();

        // Enforce role firewall isolation: Validate token context parameter signature
        Map<String, String> tokenErrors = generalService.validateToken(token, "loggedPatient");
        if (tokenErrors != null && !tokenErrors.isEmpty()) {
            responseBody.put("message", "Access Denied: Invalid or expired patient credentials signature.");
            return new ResponseEntity<>(responseBody, HttpStatus.UNAUTHORIZED); // 401 Unauthorized
        }

        // Delegate lookup queries to the underlying service layer
        return patientService.getPatientDetails(token);
    }

    /**
     * 2. Create a New Patient Endpoint.
     * Processes public client submittals to construct and persist a new patient profile.
     * Mapping: POST /patient
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createPatient(@RequestBody Patient patient) {
        Map<String, String> responseBody = new HashMap<>();

        if (patient == null) {
            responseBody.put("message", "Malformed request: Submission details missing.");
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        }

        // Check if the patient already exists by evaluating unique email or phone fields
        boolean isUnique = generalService.validatePatient(patient);
        if (!isUnique) {
            responseBody.put("message", "Patient with email id or phone no already exist");
            return new ResponseEntity<>(responseBody, HttpStatus.CONFLICT); // 409 Conflict boundary check
        }

        // Proceed to execute structural write transaction inside the persistence layer
        int serviceResultCode = patientService.createPatient(patient);
        if (serviceResultCode == 1) {
            responseBody.put("message", "Signup successful");
            return new ResponseEntity<>(responseBody, HttpStatus.CREATED); // 201 Created status code upon success
        } else {
            responseBody.put("message", "Internal server error");
            return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Error
        }
    }

    /**
     * 3. Patient Login Endpoint.
     * Validates submitted credential attributes to return active JWT authorization payload maps.
     * Mapping: POST /patient/login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Login login) {
        // Delegates authentication verification logic directly to the specialized service handler
        return generalService.validatePatientLogin(login);
    }

    /**
     * 4. Get Patient Appointments Endpoint.
     * Retrieves scheduled consultation metrics associated with a unique client token context.
     * Mapping: GET /patient/{id}/{token}
     */
    @GetMapping("/{id}/{token}")
    public ResponseEntity<Map<String, Object>> getPatientAppointment(
            @PathVariable("id") Long id,
            @PathVariable("token") String token) {

        // Validate the path authorization token matches the generic patient access schema parameters
        Map<String, String> tokenErrors = generalService.validateToken(token, "loggedPatient");
        if (tokenErrors != null && !tokenErrors.isEmpty()) {
            Map<String, Object> unauthResponse = new HashMap<>();
            unauthResponse.put("message", "Access Denied: Identity token missing or validation rejected.");
            return new ResponseEntity<>(unauthResponse, HttpStatus.UNAUTHORIZED);
        }

        // Cascade retrieval operations upstream directly into the business service container
        return patientService.getPatientAppointment(id, token);
    }

    /**
     * 5. Filter Patient Appointments Endpoint.
     * Parameterized lookup utility designed to extract filtered medical timelines cleanly.
     * Mapping: GET /patient/filter/{condition}/{name}/{token}
     */
    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<Map<String, Object>> filterPatientAppointment(
            @PathVariable("condition") String condition,
            @PathVariable("name") String name,
            @PathVariable("token") String token) {

        // Enforce token validation boundary constraint checking for security parameters alignment
        Map<String, String> tokenErrors = generalService.validateToken(token, "loggedPatient");
        if (tokenErrors != null && !tokenErrors.isEmpty()) {
            Map<String, Object> unauthResponse = new HashMap<>();
            unauthResponse.put("message", "Access Denied: Filtering action blocked due to unauthorized session key.");
            return new ResponseEntity<>(unauthResponse, HttpStatus.UNAUTHORIZED);
        }

        // Delegate joint criteria calculations seamlessly back to the centralized orchestration service
        return generalService.filterPatient(condition, name, token);
    }
}
