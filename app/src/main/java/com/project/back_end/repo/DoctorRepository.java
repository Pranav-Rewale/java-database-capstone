package com.project.back_end.repo;

import com.project.back_end.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data Access Layer repository interface managing Doctor persistence operations.
 * Extends JpaRepository to support automated query execution pipelines for 
 * credential checking, public index queries, and multi-criteria clinical matching.
 */
@Repository // Marks this interface as a data repository container component for Spring's component scanning mechanics
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    /**
     * Finds a doctor by their exact email address.
     * Primarily leveraged for credentials authentication checking.
     * @param email Unique lookup email identifier string.
     * @return The matching Doctor entity, or null if no record matches.
     */
    Doctor findByEmail(String email);

    /**
     * Custom query method designed to discover clinician records using partial matching.
     * Explicitly uses JPQL string bindings to match pattern wildcard characters.
     * @param name Name matching token string criteria.
     * @return List containing matched Doctor instances.
     */
    @Query("SELECT d FROM Doctor d WHERE d.name LIKE CONCAT('%', :name, '%')")
    List<Doctor> findByNameLike(@Param("name") String name);

    /**
     * Advanced lookup querying database entries dynamically based on a combined cross-filter 
     * of partial case-insensitive name tokens alongside case-insensitive specialty markers.
     * Used by real-time dashboard searches.
     */
    @Query("SELECT d FROM Doctor d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "AND LOWER(d.specialty) = LOWER(:specialty)")
    List<Doctor> findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(
            @Param("name") String name, 
            @Param("specialty") String specialty
    );

    /**
     * Locates a list of doctors matched to a selected clinical field, ignoring case boundaries.
     * @param specialty Target department/specialty lookup keyword (e.g. 'Cardiologist').
     * @return List of matching Doctor profiles.
     */
    List<Doctor> findBySpecialtyIgnoreCase(String specialty);
}
