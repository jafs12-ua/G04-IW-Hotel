package com.villadictos.app.exception;

public class FacilityNotFoundException extends RuntimeException {
    public FacilityNotFoundException(Long id) {
        super("Facility not found with ID: " + id);
    }
}
