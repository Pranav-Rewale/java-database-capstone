package com.project.back_end.repo;

import com.project.back_end.entity.Prescription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data Access Layer repository interface managing Prescription document persistence.
 * Extends MongoRepository to inherit standard NoSQL BSON/JSON mapping operations,
 * indexing structures, and automated query derivation routines natively for MongoDB.
 */
@Repository // Marks this interface as a data repository container component for Spring's component scanning mechanics
public interface PrescriptionRepository extends MongoRepository<Prescription, String> {

    /**
     * Custom query method automatically derived by Spring Data MongoDB's naming parser.
     * Generates a structural JSON query document under the hood: {"appointmentId": appointmentId}
     * Used by clinicians and patients to pull medical prescription histories linked to specific consultations.
     * * @param appointmentId The unique relational tracking identification long value.
     * @return A list containing all matching Prescription document records.
     */
    List<Prescription> findByAppointmentId(Long appointmentId);
}
