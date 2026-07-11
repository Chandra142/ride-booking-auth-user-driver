package com.ridebooking.user.repository;

import com.ridebooking.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /**
     * Find a profile by the user ID (the UUID from Auth Service).
     * This is the primary way we look up profiles — not by the
     * auto-increment internal ID.
     */
    Optional<UserProfile> findByUserId(String userId);

    /**
     * Check if a profile already exists for this user.
     * Used to decide between "create" and "update" operations.
     */
    boolean existsByUserId(String userId);
}
