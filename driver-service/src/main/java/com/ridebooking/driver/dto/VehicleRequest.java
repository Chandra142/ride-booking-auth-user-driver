package com.ridebooking.driver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record VehicleRequest(
    @NotBlank(message = "Vehicle number is required")
    String vehicleNumber,

    @NotBlank(message = "Vehicle model is required")
    String model,

    @NotBlank(message = "Vehicle color is required")
    String color,

    @NotBlank(message = "Ride type is required")
    String rideType,

    @Positive(message = "Seating capacity must be positive")
    Integer seatingCapacity
) {}
