package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.entity.Doctor;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Endpoint Controller managing Doctor accounts and profiling channels.
 * Coordinates system administrative actions, login authorization pipelines, 
 * clinical shift calculations, and multi-criteria public index queries.
 */
@RestController // 1. Designates this class as a REST controller that serves JSON responses natively
@RequestMapping("${api.path}doctor") // Base API path dynamic mapping pattern configuration
public class DoctorController {

    private final DoctorService doctorService;
    private final Service generalService;

    /**
     * 2. Constructor Injection for Dependencies.
     * Establishes safe dependency decoupling boundaries separating web controllers from service logic.
     */
    @Autowired
    public DoctorController(DoctorService doctorService, Service generalService) {
        this.doctorService = doctorService;
        this.generalService = generalService;
    }

    /**
     * 1. Get Doctor Availability Endpoint.
     * Inspects active schedules to isolate free appointment time windows on a targeted date.
     * Mapping: GET /doctor/availability/{user}/{doctorId}/{date}/{token}
     */
    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(
            @PathVariable("user") String user,
            @PathVariable("doctorId") Long doctorId,
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable("token") String token) {

        Map<String, Object> responseBody = new HashMap<>();

        // Validate the path authorization token signature against the provided user role type
        Map<String, String> tokenErrors = generalService.validateToken(token, user);
        if (tokenErrors != null && !tokenErrors.isEmpty()) {
            responseBody.put("message", "Unauthorized: Invalid or expired security token context.");
            return new ResponseEntity<>(responseBody, HttpStatus.UNAUTHORIZED);
        }

        // Fetch free slots from specialized core calendar calculation engines
        List<String> openSlots = doctorService.getDoctorAvailability(doctorId, date);
        responseBody.put("availability", openSlots);
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    /**
     * 2. Get List of Doctors Endpoint.
     * Public index directory mapping designed to pull the aggregate block of active clinicians.
     * Mapping: GET /doctor
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctor() {
        Map<String, Object> responseBody = new HashMap<>();
        List<Doctor> doctorsList = doctorService.getDoctors();
        
        responseBody.put("doctors", doctorsList);
        return new ResponseEntity<>(responseBody, HttpStatus.OK); // Returns 200 OK status containing profiles
    }

    /**
     * 3. Add New Doctor Endpoint.
     * Restricted to authenticated system Administrators to commit a new clinical provider record.
     * Mapping: POST /doctor/{token}
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> saveDoctor(
            @PathVariable("token") String token,
            @RequestBody Doctor doctor) {

        Map<String, String> responseBody = new HashMap<>();

        // Strict administrative boundary access check
        Map<String, String> tokenErrors = generalService.validateToken(token, "admin");
        if (tokenErrors != null && !tokenErrors.isEmpty()) {
            responseBody.put("message", "Access Denied: Administrative authorization required.");
            return new ResponseEntity<>(responseBody, HttpStatus.UNAUTHORIZED);
        }

        // Execute save operational transaction
        int serviceResultCode = doctorService.saveDoctor(doctor);

        switch (serviceResultCode) {
            case 1:
                responseBody.put("message", "Doctor added to db");
                return new ResponseEntity<>(responseBody, HttpStatus.CREATED); // 201 Created
            case -1:
                responseBody.put("message", "Doctor already exists");
                return new ResponseEntity<>(responseBody, HttpStatus.CONFLICT); // 409 Conflict
            default:
                responseBody.put("message", "Some internal error occurred");
                return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Error
        }
    }

    /**
     * 4. Doctor Login Endpoint.
     * Verifies submitted DTO structures to issue active cryptographic session token payloads.
     * Mapping: POST /doctor/login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> doctorLogin(@RequestBody Login login) {
        // Delegates authentication and secure token formulation actions directly to specialized services
        return doctorService.validateDoctor(login);
    }

    /**
     * 5. Update Doctor Details Endpoint.
     * Allows authenticated administrators to modify active clinical profile settings.
     * Mapping: PUT /doctor/{token}
     */
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(
            @PathVariable("token") String token,
            @RequestBody Doctor doctor) {

        Map<String, String> responseBody = new HashMap<>();

        // Enforce administrative firewall validation check
        Map<String, String> tokenErrors = generalService.validateToken(token, "admin");
        if (tokenErrors != null && !tokenErrors.isEmpty()) {
            responseBody.put("message", "Access Denied: Administrative authorization required.");
            return new ResponseEntity<>(responseBody, HttpStatus.UNAUTHORIZED);
        }

        // Dispatches payload data arrays to update engines
        int serviceResultCode = doctorService.updateDoctor(doctor);

        switch (serviceResultCode) {
            case 1:
                responseBody.put("message", "Doctor updated");
                return new ResponseEntity<>(responseBody, HttpStatus.OK); // 200 OK
            case -1:
                responseBody.put("message", "Doctor not found");
                return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND); // 404 Not Found
            default:
                responseBody.put("message", "Some internal error occurred");
                return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Error
        }
    }

    /**
     * 6. Delete Doctor Endpoint.
     * Triggers a cascading purge cycle, clearing appointment chains prior to eliminating profile indices.
     * Mapping: DELETE /doctor/{id}/{token}
     */
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(
            @PathVariable("id") long id,
            @PathVariable("token") String token) {

        Map<String, String> responseBody = new HashMap<>();

        // Validate token to ensure operations are bounded to authentic administrators only
        Map<String, String> tokenErrors = generalService.validateToken(token, "admin");
        if (tokenErrors != null && !tokenErrors.isEmpty()) {
            responseBody.put("message", "Access Denied: Administrative authorization required.");
            return new ResponseEntity<>(responseBody, HttpStatus.UNAUTHORIZED);
        }

        // Execute secure cross-relational cascading deletion workflows
        int serviceResultCode = doctorService.deleteDoctor(id);

        switch (serviceResultCode) {
            case 1:
                responseBody.put("message", "Doctor deleted successfully");
                return new ResponseEntity<>(responseBody, HttpStatus.OK); // 200 OK
            case -1:
                responseBody.put("message", "Doctor not found with id");
                return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND); // 404 Not Found
            default:
                responseBody.put("message", "Some internal error occurred");
                return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Error
        }
    }

    /**
     * 7. Filter Doctors Endpoint.
     * Multi-criteria cross-examination routing lookup used for processing search components asynchronously.
     * Mapping: GET /doctor/filter/{name}/{time}/{speciality}
     */
    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<Map<String, Object>> filter(
            @PathVariable("name") String name,
            @PathVariable("time") String time,
            @PathVariable("speciality") String specialty) {

        // Invokes dynamic criteria orchestration mapping methods on the general service facade layer
        Map<String, Object> filteredDoctorsMap = generalService.filterDoctor(name, specialty, time);
        return new ResponseEntity<>(filteredDoctorsMap, HttpStatus.OK);
    }
}
