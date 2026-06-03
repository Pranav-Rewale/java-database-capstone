package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.entity.Appointment;
import com.project.back_end.entity.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
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
import java.util.stream.Collectors;

/**
 * Core Service Layer component responsible for handling doctor management and scheduling business logic rules.
 * Manages complex search arrays and availability filtering matrix configurations cleanly.
 */
@Service // 1. Marks class as a Spring-managed Service Component for business logic
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    /**
     * 2. Constructor Injection for Dependencies.
     * Guarantees immutable fields and thread-safe dependency wire-ups at container launch.
     */
    @Autowired
    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    /**
     * 4. getDoctorAvailability Method.
     * Compares configured baseline shifts against active appointments to isolate free slots.
     */
    @Transactional(readOnly = true) // 3. Marked with transactional properties for safe database queries
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        Optional<Doctor> docOpt = doctorRepository.findById(doctorId);
        if (docOpt.isEmpty()) {
            return new ArrayList<>();
        }

        Doctor doctor = docOpt.get();
        List<String> baselineSlots = doctor.getAvailableTimes();
        if (baselineSlots == null || baselineSlots.isEmpty()) {
            return new ArrayList<>();
        }

        // Establish the day window boundaries
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

        // Fetch all appointments for the doctor on the specified date
        List<Appointment> bookedAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, dayStart, dayEnd);

        // Extract and normalize the hours/minutes text from booked appointments
        List<String> bookedSlots = bookedAppointments.stream()
                .map(app -> {
                    LocalTime time = app.getAppointmentTime().toLocalTime();
                    return String.format("%02d:%02d", time.getHour(), time.getMinute());
                })
                .collect(Collectors.toList());

        // Filter out the booked slots from the available baseline slots
        return baselineSlots.stream()
                .filter(slot -> !bookedSlots.contains(slot.trim()))
                .collect(Collectors.toList());
    }

    /**
     * 5. saveDoctor Method.
     * Persists new doctor credentials, preventing duplicated email conflictions.
     */
    @Transactional
    public int saveDoctor(Doctor doctor) {
        if (doctor == null || doctor.getEmail() == null) {
            return 0;
        }
        try {
            // Check if a doctor with the same email already exists
            Doctor existing = doctorRepository.findByEmail(doctor.getEmail().trim());
            if (existing != null) {
                return -1; // Conflict indicator code
            }
            doctorRepository.save(doctor);
            return 1; // Success
        } catch (Exception error) {
            System.err.println("Unexpected exception trapped in saveDoctor: " + error.getMessage());
            return 0; // Internal error code fallback
        }
    }

    /**
     * 6. updateDoctor Method.
     * Alters active clinician profiles safely after verifying record presence.
     */
    @Transactional
    public int updateDoctor(Doctor doctor) {
        if (doctor == null || doctor.getId() == null) {
            return 0;
        }
        try {
            Optional<Doctor> targetRecord = doctorRepository.findById(doctor.getId());
            if (targetRecord.isEmpty()) {
                return -1; // Record lookup failed
            }

            Doctor activeDoc = targetRecord.get();
            activeDoc.setName(doctor.getName().trim());
            activeDoc.setSpecialty(doctor.getSpecialty().trim());
            activeDoc.setEmail(doctor.getEmail().trim());
            activeDoc.setPhone(doctor.getPhone());
            if (doctor.getPassword() != null && !doctor.getPassword().isEmpty()) {
                activeDoc.setPassword(doctor.getPassword());
            }
            if (doctor.getAvailableTimes() != null) {
                activeDoc.setAvailableTimes(doctor.getAvailableTimes());
            }

            doctorRepository.save(activeDoc);
            return 1;
        } catch (Exception error) {
            System.err.println("Unexpected exception trapped in updateDoctor: " + error.getMessage());
            return 0;
        }
    }

    /**
     * 7. getDoctors Method.
     * Pulls the entirety of registered clinicians, eagerly evaluating collection elements.
     */
    @Transactional(readOnly = true)
    public List<Doctor> getDoctors() {
        List<Doctor> doctorsList = doctorRepository.findAll();
        // Eager execution safety line to guarantee lazy elements (availableTimes collection) instantiate properly
        doctorsList.forEach(doc -> {
            if (doc.getAvailableTimes() != null) doc.getAvailableTimes().size();
        });
        return doctorsList;
    }

    /**
     * 8. deleteDoctor Method.
     * Safely executes cascading cleanups, wiping scheduled consultations prior to removing doctor records.
     */
    @Transactional
    public int deleteDoctor(long id) {
        try {
            Optional<Doctor> targets = doctorRepository.findById(id);
            if (targets.isEmpty()) {
                return -1;
            }
            // Wipe all associated appointments before deleting the doctor to maintain relational integrity
            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.deleteById(id);
            return 1;
        } catch (Exception error) {
            System.err.println("Cascading deletion failed internally for doctor ID " + id + ": " + error.getMessage());
            return 0;
        }
    }

    /**
     * 9. validateDoctor Method.
     * Assesses clinician credential blocks and generates security payload signatures.
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Map<String, String> payload = new HashMap<>();
        if (login == null || login.getIdentifier() == null || login.getPassword() == null) {
            payload.put("message", "Malformed request: Submission details missing.");
            return new ResponseEntity<>(payload, HttpStatus.BAD_REQUEST);
        }

        Doctor doctor = doctorRepository.findByEmail(login.getIdentifier().trim());
        if (doctor == null) {
            payload.put("message", "Invalid credentials!");
            return new ResponseEntity<>(payload, HttpStatus.UNAUTHORIZED);
        }

        // Verify password match directly
        if (!doctor.getPassword().equals(login.getPassword())) {
            payload.put("message", "Invalid credentials!");
            return new ResponseEntity<>(payload, HttpStatus.UNAUTHORIZED);
        }

        // Generate a token for the doctor if the login is successful
        String secureToken = tokenService.generateToken(doctor.getEmail(), "doctor");
        payload.put("token", secureToken);
        payload.put("role", "doctor");
        return new ResponseEntity<>(payload, HttpStatus.OK);
    }

    /**
     * 10. findDoctorByName Method.
     * Performs partial case-sensitive wildcard tracking query updates.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> findDoctorByName(String name) {
        Map<String, Object> wrap = new HashMap<>();
        List<Doctor> rawList = doctorRepository.findByNameLike(name);
        
        // Eager loading fallback safeguard
        rawList.forEach(doc -> {
            if (doc.getAvailableTimes() != null) doc.getAvailableTimes().size();
        });

        wrap.put("doctors", rawList);
        return wrap;
    }

    /**
     * 11. filterDoctorsByNameSpecilityandTime Method.
     * Cross-evaluates multi-parameter boundaries using isolated database index sets and timelines.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorsByNameSpecilityandTime(String name, String specialty, String amOrPm) {
        Map<String, Object> map = new HashMap<>();
        List<Doctor> baseline = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        List<Doctor> processed = filterDoctorByTime(baseline, amOrPm);
        map.put("doctors", processed);
        return map;
    }

    /**
     * 13. filterDoctorByNameAndTime Method.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        Map<String, Object> map = new HashMap<>();
        List<Doctor> baseline = doctorRepository.findByNameLike(name);
        List<Doctor> processed = filterDoctorByTime(baseline, amOrPm);
        map.put("doctors", processed);
        return map;
    }

    /**
     * 14. filterDoctorByNameAndSpecility Method.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specilty) {
        Map<String, Object> map = new HashMap<>();
        List<Doctor> processed = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specilty);
        map.put("doctors", processed);
        return map;
    }

    /**
     * 15. filterDoctorByTimeAndSpecility Method.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByTimeAndSpecility(String specilty, String amOrPm) {
        Map<String, Object> map = new HashMap<>();
        List<Doctor> baseline = doctorRepository.findBySpecialtyIgnoreCase(specilty);
        List<Doctor> processed = filterDoctorByTime(baseline, amOrPm);
        map.put("doctors", processed);
        return map;
    }

    /**
     * 16. filterDoctorBySpecility Method.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorBySpecility(String specilty) {
        Map<String, Object> map = new HashMap<>();
        List<Doctor> processed = doctorRepository.findBySpecialtyIgnoreCase(specilty);
        map.put("doctors", processed);
        return map;
    }

    /**
     * 17. filterDoctorsByTime Method.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        Map<String, Object> map = new HashMap<>();
        List<Doctor> baseline = doctorRepository.findAll();
        List<Doctor> processed = filterDoctorByTime(baseline, amOrPm);
        map.put("doctors", processed);
        return map;
    }

    /**
     * 12. filterDoctorByTime (Private Internal Filtering Utility Method).
     * Splits list metrics based on AM/PM parameters. Assumes standard military time formats ("HH:mm").
     */
    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        if (doctors == null) return new ArrayList<>();
        if (amOrPm == null || amOrPm.trim().isEmpty()) return doctors;

        String standardIndicator = amOrPm.trim().toUpperCase();

        return doctors.stream().filter(doc -> {
            List<String> times = doc.getAvailableTimes();
            if (times == null || times.isEmpty()) return false;

            for (String slot : times) {
                try {
                    String[] parts = slot.trim().split(":");
                    int hour = Integer.parseInt(parts[0]);

                    // AM criteria: 00:00 to 11:59 || PM criteria: 12:00 to 23:59
                    if ("AM".equals(standardIndicator) && hour < 12) {
                        return true;
                    } else if ("PM".equals(standardIndicator) && hour >= 12) {
                        return true;
                    }
                } catch (Exception parseError) {
                    // Skip malformed string entries gracefully
                    System.err.println("Skipped unparseable time slot configuration value [" + slot + "]: " + parseError.getMessage());
                }
            }
            return false;
        }).collect(Collectors.toList());
    }
}
