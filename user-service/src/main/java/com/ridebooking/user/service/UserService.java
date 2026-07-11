package com.ridebooking.user.service;

import com.ridebooking.user.dto.UserProfileRequest;
import com.ridebooking.user.dto.UserProfileResponse;
import com.ridebooking.user.entity.UserProfile;
import com.ridebooking.user.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

/**
 * ============================================================
 * USER SERVICE — BUSINESS LOGIC
 * ============================================================
 *
 * Manages user profile data. The userId comes from the
 * X-User-Id header (set by Gateway/Auth after JWT validation),
 * NOT from the request body.
 *
 * WHY THIS PATTERN?
 * - Prevents a malicious user from passing someone else's userId
 *   in the request body to access their profile
 * - The userId is TRUSTED because it comes from the auth layer
 * - The service is simpler — no need to verify ownership
 */
@Service
public class UserService {

    private final UserProfileRepository userProfileRepository;

    public UserService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * Get or create the user's profile.
     *
     * When a user first accesses their profile after registration,
     * no profile record exists yet in this service. We create one
     * using the fullName from the registration (which we'd get via
     * an inter-service call in production). For now, if it doesn't
     * exist, we create an empty profile.
     *
     * @param userId The authenticated user's ID (from X-User-Id header)
     * @return The user's profile
     */
    public UserProfileResponse getProfile(String userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user: " + userId));
        return UserProfileResponse.fromEntity(profile);
    }

    /**
     * Create or update the user's profile.
     *
     * If the profile exists, update the fields. If not, create it.
     *
     * @param userId The authenticated user's ID (from X-User-Id header)
     * @param request The fields to update
     * @return The updated profile
     */
    public UserProfileResponse updateProfile(String userId, UserProfileRequest request) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // First time — create profile with just the userId
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUserId(userId);
                    return newProfile;
                });

        // Only update fields that were actually provided
        if (request.fullName() != null) {
            profile.setFullName(request.fullName());
        }

        if (request.phoneNumber() != null) {
            profile.setPhoneNumber(request.phoneNumber());
        }

        if (request.profilePictureUrl() != null) {
            profile.setProfilePictureUrl(request.profilePictureUrl());
        }

        if (request.homeAddress() != null) {
            profile.setHomeAddress(request.homeAddress());
        }

        if (request.workAddress() != null) {
            profile.setWorkAddress(request.workAddress());
        }

        UserProfile saved = userProfileRepository.save(profile);
        return UserProfileResponse.fromEntity(saved);
    }
}
