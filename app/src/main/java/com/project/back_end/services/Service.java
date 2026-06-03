package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.entity.Admin;
import com.project.back_end.entity.Appointment;
import com.project.back_end.entity.Doctor;
import com.project.back_end.entity.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Main Orchestration Facade Service handling centralized authentication mechanisms, 
 * cross-domain business rule validation matrices, and dynamic query routing.
 */
@org.springframework.stereotype.Service // 1. Marks this class as a Spring-managed service component
public class Service {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    /**
     * 2. Constructor Injection for Dependencies.
     * Incorporates `@Lazy` annotations on circular service dependencies to guarantee smooth
     * Spring bean initialization lifetimes while maintaining tight architectural decoupling.
     */
    @Autowired
    public Service(TokenService tokenService,
                   AdminRepository adminRepository,
                   DoctorRepository doctorRepository,
                   PatientRepository patientRepository,
                   DoctorService doctorService,
                   @Lazy PatientService patientService) { // Prevents potential circular initialization loops
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    /**
     * 3. validateToken Method.
     * Evaluates a security session signature block against an explicit username and role.
     * @return Map holding error configurations, or an empty map if validation succeeds seamlessly.
     */
    public Map<String, String> validateToken(String token, String user) {
        Map<String, String> errorMap = new HashMap<>();
        
        // Execute background boolean signature check using token service layer
        boolean isValid = tokenService.validateToken(token, user);
        
        if (!isValid) {
            errorMap.put("error", "Unauthorized: Provided access token is missing, expired, or invalid.");
        }
        return errorMap;
    }

    /**
     * 4. validateAdmin Method.
     * Evaluates administrative credential parameters and packages active JWT access tokens.
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> responseBody = new HashMap<>();
        
        if (receivedAdmin == null || receivedAdmin.getUsername() == null || receivedAdmin.getPassword() == null) {
            responseBody.put("message", "Authentication rejected: Missing mandatory credentials fields.");
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        }

        try {
            // Find admin record matching incoming primary lookup username index
            Admin admin = adminRepository.findByUsername(receivedAdmin.getUsername().trim());
            
            if (admin == null) {
                responseBody.put("message", "Invalid credentials!");
                return new ResponseEntity<>(responseBody, HttpStatus.UNAUTHORIZED);
            }

            // Verify plain text password matches exactly
            if (!admin.getPassword().equals(receivedAdmin.getPassword())) {
                responseBody.put("message", "Invalid credentials!");
                return new ResponseEntity<>(responseBody, HttpStatus.UNAUTHORIZED);
            }

            // Generate and return a secure JWT signature token for successful authorization
            String token = tokenService.generateToken(admin.getUsername(), "admin");
            responseBody.put("token", token);
            responseBody.put("role", "admin");
            return new ResponseEntity<>(responseBody, HttpStatus.OK);

        } catch (Exception error) {
            System.err.println("Unexpected failure caught inside validateAdmin workflow: " + error.getMessage());
            responseBody.put("message", "Internal server error occurred processing request parameters.");
            return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 5. filterDoctor Method.
     * Routes search criteria variables across structural multi-filter methods on DoctorService.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        boolean hasName = (name != null && !name.trim().isEmpty() && !"null".equalsIgnoreCase(name.trim()));
        boolean hasSpecialty = (specialty != null && !specialty.trim().isEmpty() && !"null".equalsIgnoreCase(specialty.trim()));
        boolean hasTime = (time != null && !time.trim().isEmpty() && !"null".equalsIgnoreCase(time.trim()));

        // Matrix routing optimization logic based on parameter presence permutations
        if (hasName && hasSpecialty && hasTime) {
            return doctorService.filterDoctorsByNameSpecilityandTime(name.trim(), specialty.trim(), time.trim());
        } else if (hasName && hasTime) {
            return doctorService.filterDoctorByNameAndTime(name.trim(), time.trim());
        } else if (hasName && hasSpecialty) {
            return doctorService.filterDoctorByNameAndSpecility(name.trim(), specialty.trim());
        } else if (hasTime && hasSpecialty) {
            return doctorService.filterDoctorByTimeAndSpecility(specialty.trim(), time.trim());
        } else if (hasSpecialty) {
            return doctorService.filterDoctorBySpecility(specialty.trim());
        } else if (hasTime) {
            return doctorService.filterDoctorsByTime(time.trim());
        } else if (hasName) {
            return doctorService.findDoctorByName(name.trim());
        } else {
            // Default fallback: If no filters are provided, fetch all available doctors natively
            Map<String, Object> defaultMap = new HashMap<>();
            defaultMap.put("doctors", doctorService.getDoctors());
            return defaultMap;
        }
    }

    /**
     * 6. validateAppointment Method.
     * Verifies clinician identity mapping and cross-checks slot calendars to intercept scheduling overlaps.
     * @return 1 if valid, 0 if unavailable/taken, and -1 if doctor record is unmapped.
     */
    @Transactional(readOnly = true)
    public int validateAppointment(Appointment appointment) {
        if (appointment == null || appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
            return -1;
        }

        Long doctorId = appointment.getDoctor().getId();
        Optional<Doctor> checkDoctor = doctorRepository.findById(doctorId);
        if (checkDoctor.isEmpty()) {
            return -1; // Doctor record does not exist
        }

        if (appointment.getAppointmentTime() == null) {
            return 0;
        }

        // Isolate date and time values from the appointment timestamp metadata
        var targetDate = appointment.getAppointmentTime().toLocalDate();
        LocalTime targetTime = appointment.getAppointmentTime().toLocalTime();
        
        // Format local timestamp elements to clean matching structural strings ("HH:mm")
        String formattedTargetTime = targetTime.format(DateTimeFormatter.ofPattern("HH:mm"));

        // Fetch remaining free open slots for the clinician on that specific day
        List<String> freeSlots = doctorService.getDoctorAvailability(doctorId, targetDate);

        // Evaluate whether the requested time matches a valid open slot
        for (String openSlot : freeSlots) {
            if (openSlot != null && openSlot.trim().equals(formattedTargetTime)) {
                return 1; // Appointment slot matches an open window and is fully valid
            }
        }
        return 0; // Appointment slot is unavailable or taken
    }

    /**
     * 7. validatePatient Method.
     * Enforces unique demographic boundaries to block duplicate registrations.
     * @return true if safe/unregistered, false if record parameters hit an existing profile match.
     */
    @Transactional(readOnly = true)
    public boolean validatePatient(Patient patient) {
        if (patient == null || patient.getEmail() == null || patient.getPhone() == null) {
            return false;
        }
        // Query database using custom compound method rule
        Patient existingPatient = patientRepository.findByEmailOrPhone(patient.getEmail().trim(), patient.getPhone().trim());
        return existingPatient == null; // Returns true if no matching record is found
    }

    /**
     * 8. validatePatientLogin Method.
     * Validates patient credential parameters and generates security tokens.
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Map<String, String> responseBody = new HashMap<>();

        if (login == null || login.getIdentifier() == null || login.getPassword() == null) {
            responseBody.put("message", "Malformed payload signature: Login details missing.");
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        }

        try {
            // Find patient by unique email identifier address index
            Patient patient = patientRepository.findByEmail(login.getIdentifier().trim());
            if (patient == null) {
                responseBody.put("message", "Invalid credentials!");
                return new ResponseEntity<>(responseBody, HttpStatus.UNAUTHORIZED);
            }

            // Compare passwords directly
            if (!patient.getPassword().equals(login.getPassword())) {
                responseBody.put("message", "Invalid credentials!");
                return new ResponseEntity<>(responseBody, HttpStatus.UNAUTHORIZED);
            }

            // Generate secure JWT authorization context token upon success
            String sessionToken = tokenService.generateToken(patient.getEmail(), "loggedPatient");
            responseBody.put("token", sessionToken);
            responseBody.put("role", "loggedPatient");
            return new ResponseEntity<>(responseBody, HttpStatus.OK);

        } catch (Exception error) {
            System.err.println("Unexpected exception caught inside validatePatientLogin pipeline: " + error.getMessage());
            responseBody.put("message", "Internal server error occurred processing request parameters.");
            return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 9. filterPatient Method.
     * Authorizes data access permissions via token decoding, then cascades actions to PatientService layers.
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        Map<String, Object> errorBody = new HashMap<>();
        
        // Extract email parameter context natively from token structure
        String extractedEmail = tokenService.extractEmail(token);
        if (extractedEmail == null) {
            errorBody.put("message", "Unauthorized context: Credentials token cannot be parsed.");
            return new ResponseEntity<>(errorBody, HttpStatus.UNAUTHORIZED);
        }

        Patient currentPatient = patientRepository.findByEmail(extractedEmail);
        if (currentPatient == null) {
            errorBody.put("message", "Profile record matching authentication token was not found.");
            return new ResponseEntity<>(errorBody, HttpStatus.NOT_FOUND);
        }

        Long patientId = currentPatient.getId();
        boolean hasCondition = (condition != null && !condition.trim().isEmpty() && !"null".equalsIgnoreCase(condition.trim()));
        boolean hasDocName = (name != null && !name.trim().isEmpty() && !"null".equalsIgnoreCase(name.trim()));

        try {
            // Delegate multi-criteria operations based on parameter filter combinations
            if (hasCondition && hasDocName) {
                return patientService.filterByDoctorAndCondition(condition.trim(), name.trim(), patientId);
            } else if (hasCondition) {
                return patientService.filterByCondition(condition.trim(), patientId);
            } else if (hasDocName) {
                return patientService.filterByDoctor(name.trim(), patientId);
            } else {
                // If no filters are provided, fallback cleanly to aggregate all appointments for this patient
                return patientService.getPatientAppointment(patientId, token);
            }
        } catch (Exception error) {
            System.err.println("Cascading query resolution failed inside filterPatient: " + error.getMessage());
            errorBody.put("message", "Internal execution error occurred parsing historical filters.");
            return new ResponseEntity<>(errorBody, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
