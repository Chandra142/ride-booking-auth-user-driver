package com.ridebooking.auth.repository;

import com.ridebooking.auth.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ============================================================
 * USER CREDENTIAL REPOSITORY
 * ============================================================
 *
 * Spring Data JpaRepository provides CRUD methods automatically:
 * - save(), findById(), findAll(), deleteById(), count(), etc.
 * You don't write implementations — Spring generates them at runtime.
 *
 * CUSTOM QUERY METHODS:
 * By declaring findByEmail(), Spring Data automatically generates
 * a query like: SELECT * FROM user_credentials WHERE email = ?
 * The method name IS the query definition.
 *
 * Optional<UserCredential> means the result might be empty
 * (user not found). This forces the caller to handle the
 * "not found" case instead of getting a null pointer exception.
 */
@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential, String> {

    /**
     * Find a user by their email address.
     * Used during login to look up the user and verify their password.
     */
    Optional<UserCredential> findByEmail(String email);

    /**
     * Check if an email is already registered.
     * Used during registration to prevent duplicate accounts.
     */
    boolean existsByEmail(String email);
}
