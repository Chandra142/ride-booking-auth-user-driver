package com.ridebooking.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ============================================================
 * USER CREDENTIAL ENTITY
 * ============================================================
 *
 * This is a JPA entity — it maps to a database table called "user_credentials".
 * Each field becomes a column in that table.
 *
 * WHY THIS IS SEPARATE FROM USER PROFILE:
 * In our architecture, Auth Service stores ONLY what's needed for
 * authentication: email, password hash, and role. The actual user
 * profile data (name, phone, address, preferences) lives in User Service.
 *
 * Why split them?
 * - Security: The password hash lives in a separate database/service.
 *   If User Service is breached, passwords are NOT exposed.
 * - Different change rates: Auth logic changes rarely. Profile data
 *   changes often. Mixing them in one table creates migration headaches.
 * - Principle of Least Privilege: User Service doesn't need to know
 *   about password hashes. Auth Service doesn't need to store addresses.
 *
 * ENTITY vs DTO:
 * - Entity (@Entity): Maps to a database row. Never sent over the wire.
 * - DTO (Data Transfer Object): Sent as JSON to the client. Never stored
 *   in the database.
 * Never expose entities directly in REST endpoints — that leaks internal
 * structure and creates security holes.
 */
@Entity
@Table(name = "user_credentials")
public class UserCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    // ^^^ UUID generates a unique string ID like "a1b2c3d4-..."
    //     instead of sequential numbers. Sequential IDs are predictable
    //     and can be enumerated by attackers.
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;
    // ^^^ This stores the BCrypt HASH, not the plaintext password!
    //     BCrypt is a one-way hash — you can never reverse it back
    //     to the original password. When logging in, you hash the
    //     input and compare hashes.

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String role = "USER";
    // ^^^ Default role is "USER". Could be "ADMIN", "DRIVER", etc.
    //     In a real system, this controls what endpoints a user can access.

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * JPA lifecycle callback — sets createdAt before the entity
     * is first persisted to the database. This is automatic,
     * no need to set it in your code.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ========================
    // Getters and Setters
    // (Lombok's @Data could do this, but we write them explicitly
    //  for learning purposes)
    // ========================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
