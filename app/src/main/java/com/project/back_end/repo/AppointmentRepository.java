package com.project.back_end.repo;

import com.project.back_end.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Access Layer repository interface managing Appointment persistence operations.
 * Coordinates optimized graph fetching queries to prevent N+1 lazy loading issues
 * across Doctor, Patient, and Availability tables.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Retrieves a list of appointments for a specific doctor within a given time range.
     * Uses JOIN FETCH to eagerly pull doctor and relationship data collections in a single roundtrip.
     */
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.doctor d WHERE d.id = :doctorId AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(
            @Param("doctorId") Long doctorId, 
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end
    );

    /**
     * Retrieves filtered appointments for a doctor by matching a partial patient name (case-insensitive) 
     * within a specified calendar time window range.
     */
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.doctor d LEFT JOIN FETCH a.patient p " +
           "WHERE d.id = :doctorId AND LOWER(p.name) LIKE LOWER(CONCAT('%', :patientName, '%')) " +
           "AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
            @Param("doctorId") Long doctorId, 
            @Param("patientName") String patientName, 
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end
    );

    /**
     * Purges all appointment table rows mapped to a specific clinician.
     * Marked as @Modifying and @Transactional to permit secure write execution bounds.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Appointment a WHERE a.doctor.id = :doctorId")
    void deleteAllByDoctorId(@Param("doctorId") Long doctorId);

    /**
     * Automatically derives standard query array locating all historical logs for a given patient.
     */
    List<Appointment> findByPatientId(Long patientId);

    /**
     * Locates patient-specific appointments matching particular state conditions, 
     * sorted chronologically in ascending sequence order.
     */
    List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(Long patientId, int status);

    /**
     * Custom search lookup filter retrieving records matching partial doctor names for a unique patient.
     */
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.doctor d WHERE a.patient.id = :patientId " +
           "AND LOWER(d.name) LIKE LOWER(CONCAT('%', :doctorName, '%'))")
    List<Appointment> filterByDoctorNameAndPatientId(
            @Param("doctorName") String doctorName, 
            @Param("patientId") Long patientId
    );

    /**
     * Parameterized multi-criteria lookup filtering records by partial doctor name, patient identifier, 
     * and explicit appointment status metrics simultaneously.
     */
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.doctor d WHERE a.patient.id = :patientId " +
           "AND a.status = :status AND LOWER(d.name) LIKE LOWER(CONCAT('%', :doctorName, '%'))")
    List<Appointment> filterByDoctorNameAndPatientIdAndStatus(
            @Param("doctorName") String doctorName, 
            @Param("patientId") Long patientId, 
            @Param("status") int status
    );

    /**
     * Inline modification command pattern designed to manually flip appointment statuses.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Appointment a SET a.status = :status WHERE a.id = :id")
    void updateStatus(@Param("status") int status, @Param("id") long id);
}
