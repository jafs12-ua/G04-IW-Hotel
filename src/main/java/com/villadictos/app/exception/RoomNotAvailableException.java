package com.villadictos.app.exception;

import java.time.LocalDate;

public class RoomNotAvailableException extends RuntimeException {
    public RoomNotAvailableException(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        super(String.format("Room %d is not available from %s to %s", roomId, checkIn, checkOut));
    }
}
