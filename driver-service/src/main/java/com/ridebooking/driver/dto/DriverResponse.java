package com.ridebooking.driver.dto;

import com.ridebooking.driver.entity.Driver;
import com.ridebooking.driver.entity.DriverStatus;
import com.ridebooking.driver.entity.Vehicle;

/**
 * DTO for returning driver data to the client.
 * Never expose the entity directly.
 */
public record DriverResponse(
    String driverId,
    String fullName,
    String email,
    String phoneNumber,
    String licenseNumber,
    Vehicle vehicle,
    DriverStatus status,
    Double rating,
    Integer totalRides,
    Boolean isAvailable
) {
    public static DriverResponse fromEntity(Driver driver) {
        return new DriverResponse(
            driver.getId(),
            driver.getFullName(),
            driver.getEmail(),
            driver.getPhoneNumber(),
            driver.getLicenseNumber(),
            driver.getVehicle(),
            driver.getStatus(),
            driver.getRating(),
            driver.getTotalRides(),
            driver.getIsAvailable()
        );
    }
}
