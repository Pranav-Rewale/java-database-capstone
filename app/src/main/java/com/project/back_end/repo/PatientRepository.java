package com.project.back_end.repo;

import com.project.back_end.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Data Access Layer repository interface managing Patient persistence operations.
 * Extends JpaRepository to inherit standard relational operations, pagination rules,
 * and automated multi-criteria index query methods natively.
 */
@Repository // Marks this interface as a data repository container component for Spring's component scanning mechanics
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Custom query method derived automatically by Spring Data's naming parser.
     * Generates a "SELECT * FROM patient WHERE email = ?" clause under the hood.
     * Used primarily for patient credentials login checks.
     * @param email The targeted lookup email identifier string.
     * @return The matching Patient entity profile record, or null if no record matches.
     */
    Patient findByEmail(String email);

    /**
     * Compound query method derived automatically using Spring Data convention rules.
     * Generates a "SELECT * FROM patient WHERE email = ? OR phone = ?" clause.
     * Essential for validating account uniqueness and preventing duplicate entry signups.
     * @param email The target evaluation verification email string.
     * @param phone The target evaluation verification contact number string.
     * @return The matched Patient entity instance, or null if neither attribute matches.
     */
    Patient findByEmailOrPhone(String email, String phone);
}
