package com.project.back_end.services;

import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Security Service component handling JWT lifecycle mechanics.
 * Manages crypto-signing keys, handles token encoding/decoding, and provides
 * cross-repository user verification to implement role-based access control.
 */
@Component // 1. Marks this class as a Spring-managed component bean for dependency injection
public class TokenService {

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    // Injects the secure signing phrase configured inside your application.properties file
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * 2. Constructor Injection for Dependencies.
     * Insulates state contexts and establishes reliable bean initialization criteria.
     */
    @Autowired
    public TokenService(AdminRepository adminRepository,
                        DoctorRepository doctorRepository,
                        PatientRepository patientRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    /**
     * 3. getSigningKey Method.
     * Transforms the plain secret string configuration into a secure HMAC-SHA SecretKey.
     * @return SecretKey object valid for signing and parsing cryptographic operations.
     */
    private SecretKey getSigningKey() {
        // Encapsulates the secret string parameters into standard byte matrices using UTF-8
        byte[] keyBytes = this.jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 4. generateToken Method.
     * Generates a secure JWT block containing issue timelines and a 7-day expiration boundary.
     * @param identifier Unique lookup identity parameter (username for Admin, email for others).
     * @return Formatted JWT token string.
     */
    public String generateToken(String identifier, String role) {
        long expirationTimeInMilliseconds = 7L * 24 * 60 * 60 * 1000; // 7 Days lifespan window
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTimeInMilliseconds);

        return Jwts.builder()
                .setSubject(identifier.trim())
                .claim("role", role) // Includes structural role metrics inside claims payload definitions
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Crypto signs token to make it tamper-proof
                .compact();
    }

    /**
     * 5. extractEmail / extractIdentifier Method.
     * Decodes and parses encrypted token elements to extract the underlying subject identity.
     * @param token Unchecked client bearer session token string.
     * @return String literal containing the user identifier subject, or null if parsing fails.
     */
    public String extractEmail(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        try {
            // Cleans and strips common header prefixes if forwarded by incoming client requests
            String cleanToken = token.trim();
            if (cleanToken.startsWith("Bearer ")) {
                cleanToken = cleanToken.substring(7);
            }

            // Validates token cryptographic structure and extracts underlying properties
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(cleanToken)
                    .getBody();

            return claims.getSubject();
        } catch (Exception parseException) {
            System.err.println("Token processing interception error: " + parseException.getMessage());
            return null; // Captures and suppresses signature adjustments, expirations, or tampering issues safely
        }
    }

    // Alias mapping method to support both naming requirements across older script specifications uniformly
    public String extractIdentifier(String token) {
        return extractEmail(token);
    }

    /**
     * 6. validateToken Method.
     * Decodes tokens and checks against the respective database repositories to verify the account is active.
     * @param token Unchecked client session token string.
     * @param user Target role identifier criteria ("admin", "doctor", "patient", or "loggedPatient").
     * @return true if the token is authentic, unexpired, and maps to an active account record; false otherwise.
     */
    public boolean validateToken(String token, String user) {
        String identifier = extractEmail(token);
        
        // If token expiration, truncation, or decryption fails, the extracted identifier returns null
        if (identifier == null || user == null || user.trim().isEmpty()) {
            return false;
        }

        String targetRole = user.trim().toLowerCase();

        try {
            // Divert conditional repository inspection rules based on target workspace role criteria
            switch (targetRole) {
                case "admin":
                    // Check AdminRepository to ensure admin account profile remains fully registered
                    return adminRepository.findByUsername(identifier) != null;

                case "doctor":
                    // Check DoctorRepository matching extracted email tracking parameters
                    return doctorRepository.findByEmail(identifier) != null;

                case "patient":
                case "loggedpatient":
                    // Check PatientRepository matching extracted email parameters boundaries
                    return patientRepository.findByEmail(identifier) != null;

                default:
                    System.err.println("Token authorization aborted: Encounted unmapped authentication role context: " + user);
                    return false;
            }
        } catch (Exception queryException) {
            System.err.println("Security context synchronization failure during validateToken processing: " + queryException.getMessage());
            return false; // Gracefully handles connection drops by falling back to non-authorized state
        }
    }
}
