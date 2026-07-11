package com.ridebooking.driver.dto;

import jakarta.validation.constraints.NotBlank;

public record DriverStatusRequest(
    @NotBlank(message = "Status is required")
    String status  // ACTIVE, SUSPENDED, OFFLINE
) {}
