package com.project.back_end.models;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Entity // Declares the class as a database-mapped entity [cite: 25]
public class Doctor {

    @Id // Marks the primary key [cite: 25]
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 3, max = 100) // String length constraint [cite: 25]
    private String name;

    @NotNull
    @Size(min = 3, max = 50)
    private String specialty;

    @NotNull
    @Email // Validates format rules [cite: 17, 25]
    private String email;

    @NotNull
    @Size(min = 6)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Hides sensitive fields from JSON output [cite: 25]
    private String password;

    @NotNull
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must match international E.164 format")
    private String phone;

    @ElementCollection // Stores list of strings in a separate table [cite: 72]
    @JsonProperty("work_schedule") // Customizes field name in API responses
    private List<String> availableTimes;

    // --- Enhanced Fields ---

    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 60, message = "Experience exceeds realistic limits")
    private int yearsOfExperience;

    @Size(max = 255)
    private String clinicAddress;

    @Min(value = 1, message = "Rating must be at least 1 star")
    @Max(value = 5, message = "Rating cannot exceed 5 stars")
    private double rating;

    // --- Constructors ---
    public Doctor() {}

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public List<String> getAvailableTimes() { return availableTimes; }
    public void setAvailableTimes(List<String> availableTimes) { this.availableTimes = availableTimes; }

    public int getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(int yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }

    public String getClinicAddress() { return clinicAddress; }
    public void setClinicAddress(String clinicAddress) { this.clinicAddress = clinicAddress; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
}
