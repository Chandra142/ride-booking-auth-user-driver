package com.ridebooking.common.dto;

/**
 * ============================================================
 * LOCATION DATA TRANSFER OBJECT
 * ============================================================
 *
 * Represents a GPS coordinate pair (latitude, longitude).
 * Used by rides, drivers, and locations so they all share
 * the same shape instead of each service defining its own.
 */
public class LocationDTO {

    private Double latitude;
    private Double longitude;

    public LocationDTO() {}

    public LocationDTO(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
