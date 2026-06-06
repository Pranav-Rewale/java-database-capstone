package com.project.back_end.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Admin model class mapping to the database table.
 * Enforces field-level validation and secure JSON serialization.
 */
@Entity // Marks the class as a database-mapped entity
public class Admin {

    @Id // Marks the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generates the primary key
    private Long id;

    @Column(unique = true)
    @NotNull(message = "Username cannot be null") // Field must not be null
    private String username;

    @NotNull(message = "Password cannot be null") // Field must not be null
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Hides sensitive fields from JSON output
    private String password;

    // --- Constructors ---
    
    // No-argument constructor required by JPA
    public Admin() {
    }

    // Parameterized constructor for easy initialization
    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}



