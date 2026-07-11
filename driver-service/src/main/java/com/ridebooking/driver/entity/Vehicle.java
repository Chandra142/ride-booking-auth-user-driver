package com.ridebooking.driver.entity;

import jakarta.persistence.Embeddable;

/**
 * ============================================================
 * VEHICLE — EMBEDDABLE
 * ============================================================
 *
 * @Embeddable means this class is NOT a separate table.
 * Its fields are stored AS COLUMNS in the Driver table.
 *
 * Example:
 *   DRIVERS table has columns:
 *   | id | name | ... | vehicle_number | vehicle_model | vehicle_color |
 *
 * This is called "composition" in JPA — the Vehicle fields
 * are part of the Driver row, not a foreign key to another table.
 *
 * WHY EMBEDDABLE vs. @Entity?
 * - A driver always has exactly one vehicle
 * - The vehicle has no independent existence (it doesn't make
 *   sense to query "all vehicles" without their drivers)
 * - Embeddable = simpler queries, no join needed
 */
@Embeddable
public class Vehicle {

    private String vehicleNumber;
    private String model;
    private String color;
    private String rideType;  // ECONOMY, PREMIUM, XL
    private Integer seatingCapacity;

    // ========================
    // Getters and Setters
    // ========================

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getRideType() { return rideType; }
    public void setRideType(String rideType) { this.rideType = rideType; }

    public Integer getSeatingCapacity() { return seatingCapacity; }
    public void setSeatingCapacity(Integer seatingCapacity) { this.seatingCapacity = seatingCapacity; }
}
