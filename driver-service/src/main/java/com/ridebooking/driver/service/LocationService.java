package com.ridebooking.driver.service;

import com.ridebooking.driver.dto.LocationUpdateRequest;
import com.ridebooking.driver.dto.NearbyDriverResponse;
import com.ridebooking.driver.entity.Driver;
import com.ridebooking.driver.entity.DriverLocation;
import com.ridebooking.driver.entity.DriverStatus;
import com.ridebooking.driver.repository.DriverLocationRepository;
import com.ridebooking.driver.repository.DriverRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ============================================================
 * LOCATION SERVICE
 * ============================================================
 *
 * Handles everything related to driver GPS locations:
 * 1. Update driver location (write to Redis + PostgreSQL)
 * 2. Find nearby drivers (read from Redis, filter by distance)
 * 3. Calculate distance and ETA (Haversine formula)
 *
 * WHY REDIS FOR LOCATIONS?
 * Imagine 1000 drivers updating their location every 3 seconds.
 * That's 333 writes/second to PostgreSQL — fine for small scale,
 * but at 10,000 drivers it becomes 3,333 writes/second.
 *
 * Redis handles millions of writes/second because:
 * - It's single-threaded (no lock contention)
 * - Data is in memory (no disk I/O for writes)
 * - Simple data model (just key-value)
 *
 * The PostgreSQL table (driver_locations) stores HISTORY for
 * analytics, not for real-time queries.
 */
@Service
public class LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationService.class);

    /*
     * Redis key prefix for driver locations.
     * Full key: "driver:location:DRV-001"
     * Value format: "latitude,longitude,timestamp"
     */
    private static final String REDIS_KEY_PREFIX = "driver:location:";

    /**
     * TTL (Time To Live) for Redis location entries: 60 seconds.
     * If a driver doesn't update their location for 60 seconds,
     * the entry expires automatically. This handles the case
     * where a driver goes offline — we don't need to explicitly
     * delete stale locations.
     */
    private static final long LOCATION_TTL_SECONDS = 60;

    private final RedisTemplate<String, String> redisTemplate;
    private final DriverRepository driverRepository;
    private final DriverLocationRepository driverLocationRepository;

    public LocationService(RedisTemplate<String, String> redisTemplate,
                           DriverRepository driverRepository,
                           DriverLocationRepository driverLocationRepository) {
        this.redisTemplate = redisTemplate;
        this.driverRepository = driverRepository;
        this.driverLocationRepository = driverLocationRepository;
    }

    /**
     * ============================================================
     * UPDATE DRIVER LOCATION
     * ============================================================
     *
     * Called every few seconds by the driver's app.
     * 1. Save current location to Redis (for fast nearby search)
     * 2. Save to PostgreSQL (for history/analytics)
     * 3. Return the updated location
     *
     * @param request Contains driverId, latitude, longitude
     */
    public void updateLocation(LocationUpdateRequest request) {
        // Step 1: Save to Redis (real-time)
        String redisKey = REDIS_KEY_PREFIX + request.driverId();
        String redisValue = request.latitude() + "," + request.longitude() + "," + System.currentTimeMillis();

        redisTemplate.opsForValue().set(redisKey, redisValue, LOCATION_TTL_SECONDS, TimeUnit.SECONDS);

        // Step 2: Save to PostgreSQL (history)
        DriverLocation locationHistory = new DriverLocation();
        locationHistory.setDriverId(request.driverId());
        locationHistory.setLatitude(request.latitude());
        locationHistory.setLongitude(request.longitude());

        driverLocationRepository.save(locationHistory);

        log.debug("Location updated for driver: {} at {},{}",
                request.driverId(), request.latitude(), request.longitude());
    }

    /**
     * ============================================================
     * GET DRIVER CURRENT LOCATION
     * ============================================================
     *
     * Reads the driver's most recent location from Redis.
     *
     * @param driverId The driver's UUID
     * @return Array of [latitude, longitude], or null if not found
     */
    public Double[] getDriverLocation(String driverId) {
        String redisKey = REDIS_KEY_PREFIX + driverId;
        String value = redisTemplate.opsForValue().get(redisKey);

        if (value == null) {
            return null;
        }

        String[] parts = value.split(",");
        return new Double[]{Double.parseDouble(parts[0]), Double.parseDouble(parts[1])};
    }

    /**
     * ============================================================
     * FIND NEARBY DRIVERS
     * ============================================================
     *
     * This is the core matching algorithm:
     * 1. Get all ACTIVE, available drivers
     * 2. Get their current locations from Redis
     * 3. Calculate distance from the search point
     * 4. Filter by radius
     * 5. Sort by distance (closest first)
     * 6. Calculate ETA for each
     *
     * @param latitude  Search point latitude
     * @param longitude Search point longitude
     * @param radiusKm  Search radius in kilometers
     * @return List of nearby drivers sorted by distance
     */
    public List<NearbyDriverResponse> findNearbyDrivers(double latitude, double longitude, double radiusKm) {

        // Step 1: Get all active, available drivers
        List<Driver> activeDrivers = driverRepository
                .findByStatusAndIsAvailable(DriverStatus.ACTIVE, true);

        List<NearbyDriverResponse> nearbyDrivers = new ArrayList<>();

        // Step 2: For each driver, check if they're within range
        for (Driver driver : activeDrivers) {

            // Get the driver's current location from Redis
            Double[] location = getDriverLocation(driver.getId());

            if (location == null) {
                // Driver has no recent location — skip
                continue;
            }

            double driverLat = location[0];
            double driverLng = location[1];

            // Step 3: Calculate distance using Haversine formula
            double distanceKm = calculateDistance(latitude, longitude, driverLat, driverLng);

            // Step 4: Filter by radius
            if (distanceKm <= radiusKm) {
                // Step 5: Calculate ETA (assuming average speed of 30 km/h)
                int etaMinutes = calculateETA(distanceKm);

                nearbyDrivers.add(new NearbyDriverResponse(
                        driver.getId(),
                        driver.getFullName(),
                        driverLat,
                        driverLng,
                        Math.round(distanceKm * 10.0) / 10.0, // Round to 1 decimal
                        etaMinutes,
                        driver.getVehicle(),
                        driver.getRating()
                ));
            }
        }

        // Sort by distance (closest first)
        nearbyDrivers.sort((a, b) -> Double.compare(a.distanceKm(), b.distanceKm()));

        return nearbyDrivers;
    }

    /**
     * ============================================================
     * HAVERSINE FORMULA — Calculate distance between two GPS points
     * ============================================================
     *
     * Latitude and longitude are angles on a sphere, not (x,y) coordinates.
     * The Haversine formula calculates the great-circle distance between
     * two points on a sphere given their latitudes and longitudes.
     *
     * WHY NOT PYTHAGOREAN THEOREM?
     * The Earth is (approximately) a sphere. At latitude 40°N, one degree
     * of longitude is about 85 km. At the equator, it's 111 km. Using
     * simple flat-earth math would give wrong results.
     *
     * FORMULA:
     *   a = sin²(Δlat/2) + cos(lat1) · cos(lat2) · sin²(Δlon/2)
     *   c = 2 · atan2(√a, √(1-a))
     *   d = R · c   (where R = Earth's radius = 6371 km)
     *
     * @param lat1  Point 1 latitude (degrees)
     * @param lon1  Point 1 longitude (degrees)
     * @param lat2  Point 2 latitude (degrees)
     * @param lon2  Point 2 longitude (degrees)
     * @return Distance in kilometers
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Earth's radius in kilometers
        final double EARTH_RADIUS_KM = 6371.0;

        // Convert degrees to radians (Java's Math functions use radians)
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        // Haversine formula
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                 + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                 * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * ============================================================
     * ETA CALCULATION
     * ============================================================
     *
     * Calculates estimated time of arrival based on distance.
     *
     * This is a MOCK implementation that assumes average city
     * speed of 30 km/h. In production, you'd use Google Maps
     * Distance Matrix API which accounts for:
     * - Real road routes (not straight-line distance)
     * - Current traffic conditions
     * - Speed limits
     * - One-way streets, turns, etc.
     *
     * @param distanceKm Distance in kilometers
     * @return Estimated time in minutes
     */
    public int calculateETA(double distanceKm) {
        // Average city driving speed: 30 km/h
        // ETA = (distance / speed) * 60 (convert hours to minutes)
        return (int) Math.ceil((distanceKm / 30.0) * 60);
    }
}
