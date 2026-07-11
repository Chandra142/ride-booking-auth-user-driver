package com.ridebooking.driver.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ============================================================
 * DRIVER LOCATION ENTITY
 * ============================================================
 *
 * Stores the location history of drivers in PostgreSQL.
 * Every time a driver updates their location, we store it here
 * for history/analytics purposes.
 *
 * For REAL-TIME location lookups (finding nearby drivers),
 * we use Redis (see LocationService and RedisConfig).
 *
 * TWO-TIER STORAGE STRATEGY:
 * 1. Redis: Current location (fast reads/writes, no history)
 *    Key: "driver:location:{driverId}" → Value: "lat,lng,timestamp"
 * 2. PostgreSQL: Location history (permanent storage for analytics)
 *    Table: driver_locations — every update creates a new row
 *
 * This combines the speed of Redis with the durability of PostgreSQL.
 */
@Entity
@Table(name = "driver_locations")
public class DriverLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String driverId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    // ========================
    // Getters and Setters
    // ========================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
