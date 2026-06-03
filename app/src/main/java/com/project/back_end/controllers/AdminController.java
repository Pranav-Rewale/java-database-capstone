package com.project.back_end.controllers;

import com.project.back_end.entity.Admin;
import com.project.back_end.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST Endpoint Controller managing system Administrator authentication operations.
 * Processes front-end credential submittals, communicating via JSON payloads.
 */
@RestController // 1. Designates this class as a REST controller to manage web requests and return JSON streams natively
@RequestMapping("${api.path}admin") // Maps base path dynamically leveraging external properties placeholder
public class AdminController {

    private final Service service;

    /**
     * 2. Constructor-based Dependency Injection.
     * Injects the centralized facade Service layer bean. This isolates endpoint handling routing structures
     * cleanly away from transactional business logic rules and promotes cleaner testability.
     */
    @Autowired
    public AdminController(Service service) {
        this.service = service;
    }

    /**
     * 3. Define the adminLogin Method.
     * Intercepts HTTP POST requests targeting administrative validation checkpoints.
     * * @param receivedAdmin Credential payload object (username and password) deserialized out of the request body.
     * @return A ResponseEntity wrapping a Map that contains either an active authorization token or localized error notes.
     */
    @PostMapping // Handles empty suffix sub-path POST requests mapping directly to the base "/admin" route
    public ResponseEntity<Map<String, String>> adminLogin(@RequestBody Admin receivedAdmin) {
        // Delegates authentication verification logic directly to the dedicated validation facade method
        return service.validateAdmin(receivedAdmin);
    }
}
