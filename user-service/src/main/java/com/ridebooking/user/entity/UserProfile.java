package com.ridebooking.user.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ============================================================
 * USER PROFILE ENTITY
 * ============================================================
 *
 * This stores the user's public profile data — separate from
 * their login credentials (which live in Auth Service).
 *
 * WHY SEPARATE TABLES?
 * We have TWO databases/tables for a user:
 *   Auth Service:  user_credentials (id, email, password_hash, role)
 *   User Service:  user_profiles (id, user_id, full_name, phone, etc.)
 *
 * The "userId" here matches the "id" in Auth Service's user_credentials.
 * They're linked by a shared UUID, NOT by a foreign key constraint
 * (because they're in different services/databases).
 *
 * This is called "Database per Service" — a microservices pattern
 * where each service owns its own data. Services communicate via
 * APIs, not shared databases.
 */
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * This is the SAME userId from Auth Service (user_credentials.id).
     * It's set when a user first accesses their profile.
     * The value comes from the X-User-Id header (set by Gateway/Auth).
     */
    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String fullName;

    private String phoneNumber;

    private String profilePictureUrl;

    private String homeAddress;

    private String workAddress;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ========================
    // Getters and Setters
    // ========================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    public String getHomeAddress() { return homeAddress; }
    public void setHomeAddress(String homeAddress) { this.homeAddress = homeAddress; }

    public String getWorkAddress() { return workAddress; }
    public void setWorkAddress(String workAddress) { this.workAddress = workAddress; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
