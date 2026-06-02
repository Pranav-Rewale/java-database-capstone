package com.project.back_end.models;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Doctor entity mapping to the database table.
 * Implements rigorous data validation, secure password handling, and an availability tracking system.
 */
@Entity // Declares the class as a database-mapped entity [cite: 25, 43]
public class Doctor {

    @Id // Marks the primary key [cite: 25, 50]
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generates the primary key [cite: 25, 63]
    private Long id;

    @NotNull(message = "Name cannot be null") // Field must not be null [cite: 25, 53]
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters") // String length constraint [cite: 25, 54]
    private String name;

    @NotNull(message = "Specialty cannot be null") // Field must not be null [cite: 25, 55]
    @Size(min = 3, max = 50, message = "Specialty must be between 3 and 50 characters") // String length constraint [cite: 25, 56]
    private String specialty;

    @NotNull(message = "Email cannot be null") // Field must not be null [cite: 25, 59]
    @Email(message = "Field must be a valid email address") // Validates standard email structure [cite: 25, 58]
    private String email;

    @NotNull(message = "Password cannot be null") // Field must not be null
    @Size(min = 6, message = "Password must be at least 6 characters long") // String length constraint [cite: 61, 62]
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Hides sensitive fields from JSON responses 
    private String password;

    @NotNull(message = "Phone number cannot be null")
    @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits") // Field must match the regular expression [cite: 25, 66]
    private String phone;

    @ElementCollection // Used to store a list of simple values in a separate system-managed table [cite: 68, 72]
    private List<String> availableTimes;

    // --- Constructors ---

    // No-argument constructor required by JPA for entity creation [cite: 41]
    public Doctor() {
    }

    // Parameterized constructor for object initialization
    public Doctor(String name, String specialty, String email, String password, String phone, List<String> availableTimes) {
        this.name = name;
        this.specialty = specialty;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.availableTimes = availableTimes;
    }

    // --- Getters and Setters --- [cite: 41]

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<String> getAvailableTimes() {
        return availableTimes;
    }

    public void setAvailableTimes(List<String> availableTimes) {
        this.availableTimes = availableTimes;
    }
}