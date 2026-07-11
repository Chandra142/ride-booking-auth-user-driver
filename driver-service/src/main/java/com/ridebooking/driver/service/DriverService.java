package com.ridebooking.driver.service;

import com.ridebooking.driver.dto.*;
import com.ridebooking.driver.entity.Driver;
import com.ridebooking.driver.entity.DriverStatus;
import com.ridebooking.driver.entity.Vehicle;
import com.ridebooking.driver.repository.DriverRepository;
import org.springframework.stereotype.Service;

/**
 * ============================================================
 * DRIVER SERVICE — BUSINESS LOGIC
 * ============================================================
 *
 * Manages driver profiles — registration, status updates,
 * and driver details.
 *
 * Location-related operations (update, nearby search) are
 * in LocationService to keep concerns separated.
 */
@Service
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    /**
     * ============================================================
     * REGISTER A NEW DRIVER
     * ============================================================
     *
     * Flow:
     * 1. Check if email is already registered
     * 2. Create Driver entity with Vehicle embedded
     * 3. Set status to PENDING_VERIFICATION
     * 4. Save to database
     *
     * @param request Driver registration details including vehicle info
     * @return DriverResponse with the saved driver
     */
    public DriverResponse registerDriver(DriverRegistrationRequest request) {

        // Step 1: Check for duplicate email
        if (driverRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered as a driver");
        }

        // Step 2: Create and populate Vehicle (embedded object)
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleNumber(request.vehicle().vehicleNumber());
        vehicle.setModel(request.vehicle().model());
        vehicle.setColor(request.vehicle().color());
        vehicle.setRideType(request.vehicle().rideType().toUpperCase());
        vehicle.setSeatingCapacity(request.vehicle().seatingCapacity());

        // Step 3: Create and populate Driver entity
        Driver driver = new Driver();
        driver.setFullName(request.fullName().trim());
        driver.setEmail(request.email().toLowerCase().trim());
        driver.setPhoneNumber(request.phoneNumber().trim());
        driver.setLicenseNumber(request.licenseNumber().toUpperCase().trim());
        driver.setVehicle(vehicle);

        /*
         * New drivers start as PENDING_VERIFICATION.
         * For testing, we auto-approve to ACTIVE.
         * In production, this would require admin review.
         */
        driver.setStatus(DriverStatus.ACTIVE); // Auto-approve for testing
        driver.setIsAvailable(true);

        // Step 4: Save to database
        Driver savedDriver = driverRepository.save(driver);

        return DriverResponse.fromEntity(savedDriver);
    }

    /**
     * Get driver details by ID.
     */
    public DriverResponse getDriver(String driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + driverId));
        return DriverResponse.fromEntity(driver);
    }

    /**
     * Update driver's online/offline status.
     *
     * @param driverId The driver's UUID
     * @param statusRequest Contains the new status
     * @return Updated driver info
     */
    public DriverResponse updateStatus(String driverId, DriverStatusRequest statusRequest) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + driverId));

        DriverStatus newStatus = DriverStatus.valueOf(statusRequest.status().toUpperCase());
        driver.setStatus(newStatus);

        // If going offline, also set isAvailable to false
        if (newStatus == DriverStatus.OFFLINE) {
            driver.setIsAvailable(false);
        } else if (newStatus == DriverStatus.ACTIVE) {
            driver.setIsAvailable(true);
        }

        Driver savedDriver = driverRepository.save(driver);
        return DriverResponse.fromEntity(savedDriver);
    }

    /**
     * Toggle driver availability (online/offline for ride acceptance).
     */
    public DriverResponse toggleAvailability(String driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + driverId));

        driver.setIsAvailable(!driver.getIsAvailable());

        Driver savedDriver = driverRepository.save(driver);
        return DriverResponse.fromEntity(savedDriver);
    }
}
