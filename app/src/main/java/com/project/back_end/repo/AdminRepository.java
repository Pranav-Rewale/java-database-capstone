package com.project.back_end.repo;

import com.project.back_end.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Data Access Layer repository interface managing Admin persistence operations.
 * Extends JpaRepository to inherit robust CRUD capabilities, paging frameworks, 
 * and automated query derivation routines natively.
 */
@Repository // Marks this interface as a data repository container component for Spring's component scanning mechanics
public interface AdminRepository extends JpaRepository<Admin, Long> {

    /**
     * Custom query method derived automatically by Spring Data's naming convention parser.
     * Generates a structural "SELECT * FROM admin WHERE username = ?" query under the hood.
     * * @param username The unique identification username string to filter by.
     * @return The matching Admin entity record instance, or null if no matching record exists.
     */
    Admin findByUsername(String username);
}
