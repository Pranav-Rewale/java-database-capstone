package com.project.back_end.mvc;

import com.project.back_end.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

/**
 * Spring MVC Controller managing access control for secure Thymeleaf dashboards.
 * Validates session context signatures before serving views rather than writing JSON streams.
 */
@Controller // Marks the class as an MVC controller returning view templates rather than REST payloads
public class DashboardController {

    private final Service sharedService;

    /**
     * Parameterized Constructor Injection for the shared business service layer.
     * Enforces explicit, uncoupled dependencies that align with Spring's testing best practices.
     */
    @Autowired
    public DashboardController(Service sharedService) {
        this.sharedService = sharedService;
    }

    /**
     * Intercepts HTTP GET requests to resolve and authorize Admin Dashboard access.
     * * @param token Secure JWT session signature pulled directly from the path variable context.
     * @return Resolves to the 'adminDashboard.html' view path if valid, otherwise redirects to the application root.
     */
    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable("token") String token) {
        // Validate token signatures against administrative role requirements
        Map<String, String> errors = sharedService.validateToken(token, "admin");

        // If the errors map is empty, validation passed seamlessly
        if (errors == null || errors.isEmpty()) {
            return "admin/adminDashboard"; // Forwards to src/main/resources/templates/admin/adminDashboard.html
        }

        // If authorization fails, clear view references and bounce the client back to the login screen root
        return "redirect:/";
    }

    /**
     * Intercepts HTTP GET requests to resolve and authorize Doctor Dashboard access.
     * * @param token Secure JWT session signature pulled directly from the path variable context.
     * @return Resolves to the 'doctorDashboard.html' view path if valid, otherwise redirects to the application root.
     */
    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable("token") String token) {
        // Validate token signatures against clinician/doctor role requirements
        Map<String, String> errors = sharedService.validateToken(token, "doctor");

        // If the errors map is empty, validation passed seamlessly
        if (errors == null || errors.isEmpty()) {
            return "doctor/doctorDashboard"; // Forwards to src/main/resources/templates/doctor/doctorDashboard.html
        }

        // Redirect invalid access attempts back to the home page at the default address
        return "redirect:/";
    }
}
