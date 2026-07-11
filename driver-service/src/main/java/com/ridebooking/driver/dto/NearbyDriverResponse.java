package com.ridebooking.driver.dto;

import com.ridebooking.driver.entity.Vehicle;

/**
 * Lightweight DTO for nearby driver search results.
 * Includes the driver's basic info, vehicle, current location,
 * and distance from the search point.
 */
public record NearbyDriverResponse(
    String driverId,
    String fullName,
    Double latitude,
    Double longitude,
    Double distanceKm,  // Distance from the search point in kilometers
    Integer etaMinutes, // Estimated time of arrival in minutes
    Vehicle vehicle,
    Double rating
) {}
