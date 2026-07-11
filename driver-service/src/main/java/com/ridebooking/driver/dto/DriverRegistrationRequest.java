package com.ridebooking.driver.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for driver registration.
 * Contains personal info + nested vehicle info.
 *
 * @Valid on vehicleRequest tells Spring to also validate
 * the nested VehicleRequest object.
 */
public record DriverRegistrationRequest(
    @NotBlank(message = "Full name is required")
    String fullName,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Phone number is required")
    String phoneNumber,

    @NotBlank(message = "License number is required")
    String licenseNumber,

    @Valid
    VehicleRequest vehicle
) {}
