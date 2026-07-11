package com.ridebooking.driver.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ============================================================
 * DRIVER ENTITY
 * ============================================================
 *
 * Represents a driver registered with the system.
 * Contains both personal info (name, license) and their vehicle.
 *
 * Driver Status Flow:
 *   PENDING_VERIFICATION → ACTIVE → (can go back to) SUSPENDED
 *
 * A driver must be ACTIVE to accept rides.
 */
@Entity
@Table(name = "drivers")
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String licenseNumber;

    /*
     * Vehicle is embedded — its fields are stored directly
     * as columns in the "drivers" table.
     */
    @Embedded
    private Vehicle vehicle;

    /*
     * Driver status. Initially PENDING_VERIFICATION until
     * an admin approves (or we auto-approve for testing).
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DriverStatus status = DriverStatus.PENDING_VERIFICATION;

    /*
     * Current average rating (1.0 - 5.0).
     * Initially 0.0 until the driver completes their first ride.
     */
    private Double rating = 0.0;

    /*
     * Number of rides completed.
     */
    private Integer totalRides = 0;

    /*
     * Is the driver currently available to accept new rides?
     * This is toggled by the driver themselves (going online/offline).
     */
    @Column(nullable = false)
    private Boolean isAvailable = true;

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

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public DriverStatus getStatus() { return status; }
    public void setStatus(DriverStatus status) { this.status = status; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getTotalRides() { return totalRides; }
    public void setTotalRides(Integer totalRides) { this.totalRides = totalRides; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
