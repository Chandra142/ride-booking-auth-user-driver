package com.ridebooking.driver.controller;

import com.ridebooking.common.constant.ServiceConstants;
import com.ridebooking.common.dto.ApiResponse;
import com.ridebooking.driver.dto.LocationUpdateRequest;
import com.ridebooking.driver.dto.NearbyDriverResponse;
import com.ridebooking.driver.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for location-related operations.
 *
 * Separated from DriverController because location tracking
 * is a distinct concern with different data and different
 * consumers (drivers update, riders query).
 */
@RestController
@RequestMapping(ServiceConstants.DRIVER_BASE_PATH)
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * PUT /api/v1/drivers/location
     *
     * Update a driver's current GPS location.
     * Called frequently (every 2-3 seconds) by the driver's mobile app.
     */
    @PutMapping("/location")
    public ResponseEntity<ApiResponse<String>> updateLocation(
            @Valid @RequestBody LocationUpdateRequest request) {

        locationService.updateLocation(request);
        return ResponseEntity.ok(ApiResponse.success("Location updated"));
    }

    /**
     * GET /api/v1/drivers/nearby?latitude=12.9716&longitude=77.5946&radius=5
     *
     * Find nearby drivers within a given radius (in kilometers).
     * Used by Ride Service to match riders with nearby drivers.
     *
     * @param latitude  Rider's current latitude
     * @param longitude Rider's current longitude
     * @param radius    Search radius in kilometers (default: 5)
     * @return List of nearby drivers sorted by distance (closest first)
     */
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<NearbyDriverResponse>>> findNearbyDrivers(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5") double radius) {

        List<NearbyDriverResponse> nearbyDrivers =
                locationService.findNearbyDrivers(latitude, longitude, radius);

        return ResponseEntity.ok(
                ApiResponse.success("Found " + nearbyDrivers.size() + " nearby drivers", nearbyDrivers));
    }

    /**
     * GET /api/v1/drivers/{driverId}/location
     *
     * Get a specific driver's current location.
     */
    @GetMapping("/{driverId}/location")
    public ResponseEntity<ApiResponse<Double[]>> getDriverLocation(
            @PathVariable String driverId) {

        Double[] location = locationService.getDriverLocation(driverId);
        if (location == null) {
            return ResponseEntity.ok(ApiResponse.error("Location not available"));
        }
        return ResponseEntity.ok(ApiResponse.success("Location found", location));
    }

    /**
     * POST /api/v1/drivers/location/distance
     *
     * Calculate distance between two GPS points.
     * Useful for testing and for the frontend.
     */
    @PostMapping("/location/distance")
    public ResponseEntity<ApiResponse<DistanceResponse>> calculateDistance(
            @RequestBody DistanceRequest request) {

        double distanceKm = locationService.calculateDistance(
                request.from().latitude(),
                request.from().longitude(),
                request.to().latitude(),
                request.to().longitude()
        );

        int etaMinutes = locationService.calculateETA(distanceKm);

        return ResponseEntity.ok(ApiResponse.success("Distance calculated",
                new DistanceResponse(
                        Math.round(distanceKm * 10.0) / 10.0,
                        etaMinutes
                )));
    }

    /**
     * Inner records for the distance calculation endpoint.
     */
    public record DistanceRequest(LocationPoint from, LocationPoint to) {}
    public record LocationPoint(double latitude, double longitude) {}
    public record DistanceResponse(double distanceKm, int etaMinutes) {}
}
