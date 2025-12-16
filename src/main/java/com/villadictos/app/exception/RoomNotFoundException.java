package com.villadictos.app.exception;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(Long id) {
        super("Room not found with ID: " + id);
    }
}
