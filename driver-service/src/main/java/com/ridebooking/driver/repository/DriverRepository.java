package com.ridebooking.driver.repository;

import com.ridebooking.driver.entity.Driver;
import com.ridebooking.driver.entity.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, String> {

    /**
     * Find a driver by email (used during registration to prevent duplicates).
     */
    Optional<Driver> findByEmail(String email);

    /**
     * Check if email is already registered.
     */
    boolean existsByEmail(String email);

    /**
     * Find all active drivers who are currently available for rides.
     * This is the initial filter before we narrow down by location.
     */
    List<Driver> findByStatusAndIsAvailable(DriverStatus status, Boolean isAvailable);
}
