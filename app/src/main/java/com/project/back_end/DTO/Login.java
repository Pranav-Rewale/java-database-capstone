package com.project.back_end.DTO;

/**
 * Data Transfer Object capturing incoming authentication payloads from client forms.
 * Maps universal credential strings to safely support Admin usernames alongside 
 * Doctor and Patient emails within a singular DTO structure.
 */
public class Login {

    private String identifier; // Holds username for Admins, or email for Patients/Doctors
    private String password;   // Holds raw password string for authentication checking

    /**
     * Default no-argument constructor.
     * Required by JSON processing frameworks like Jackson to instantiate 
     * the request object via reflection during deserialization.
     */
    public Login() {
    }

    /**
     * Parameterized constructor for clean programmatic object initialization.
     * @param identifier The unique account lookup parameter (email or username).
     * @param password   The plain text authentication token string.
     */
    public Login(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }

    // --- Standard Getters and Setters ---

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
