package com.ridebooking.driver.controller;

import com.ridebooking.common.constant.ServiceConstants;
import com.ridebooking.common.dto.ApiResponse;
import com.ridebooking.driver.dto.*;
import com.ridebooking.driver.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for driver profile management.
 */
@RestController
@RequestMapping(ServiceConstants.DRIVER_BASE_PATH)
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    /**
     * POST /api/v1/drivers/register
     *
     * Register a new driver with their vehicle information.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<DriverResponse>> registerDriver(
            @Valid @RequestBody DriverRegistrationRequest request) {

        DriverResponse response = driverService.registerDriver(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Driver registered", response));
    }

    /**
     * GET /api/v1/drivers/{driverId}
     *
     * Get driver details by ID.
     */
    @GetMapping("/{driverId}")
    public ResponseEntity<ApiResponse<DriverResponse>> getDriver(
            @PathVariable String driverId) {

        DriverResponse response = driverService.getDriver(driverId);
        return ResponseEntity.ok(ApiResponse.success("Driver found", response));
    }

    /**
     * PATCH /api/v1/drivers/{driverId}/status
     *
     * Update a driver's status (ACTIVE, SUSPENDED, OFFLINE).
     */
    @PatchMapping("/{driverId}/status")
    public ResponseEntity<ApiResponse<DriverResponse>> updateStatus(
            @PathVariable String driverId,
            @Valid @RequestBody DriverStatusRequest request) {

        DriverResponse response = driverService.updateStatus(driverId, request);
        return ResponseEntity.ok(ApiResponse.success("Status updated", response));
    }

    /**
     * POST /api/v1/drivers/{driverId}/toggle-availability
     *
     * Toggle whether the driver is accepting new rides.
     */
    @PostMapping("/{driverId}/toggle-availability")
    public ResponseEntity<ApiResponse<DriverResponse>> toggleAvailability(
            @PathVariable String driverId) {

        DriverResponse response = driverService.toggleAvailability(driverId);
        String message = response.isAvailable() ? "Driver is now online" : "Driver is now offline";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    /**
     * GET /api/v1/drivers/test
     *
     * Health check endpoint.
     */
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity.ok(ApiResponse.success("Driver Service is running"));
    }
}
