package com.ridebooking.driver.repository;

import com.ridebooking.driver.entity.DriverLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for persisting location history.
 * Real-time locations are handled via Redis.
 */
@Repository
public interface DriverLocationRepository extends JpaRepository<DriverLocation, Long> {

    /**
     * Get the location history for a specific driver,
     * ordered from most recent to oldest.
     */
    List<DriverLocation> findByDriverIdOrderByTimestampDesc(String driverId);
}
