package com.ridebooking.driver.entity;

/**
 * Enum representing the possible states of a driver account.
 *
 * WHY ENUM INSTEAD OF STRING?
 * - Type safety: "ACTIVE" is a valid status, "active" or "actve" is not
 * - The compiler catches typos at compile time
 * - IDE autocomplete makes it easier to use
 */
public enum DriverStatus {
    PENDING_VERIFICATION,
    ACTIVE,
    SUSPENDED,
    OFFLINE
}
