package com.villadictos.app.dto.api;

import java.time.LocalDate;

/**
 * DTO for booking redirect - to pass to booking tunnel
 */
public record BookingRedirectDTO(
                Long roomId,
                String roomNumber,
                String roomType,
                LocalDate checkIn,
                LocalDate checkOut,
                Integer guests,
                String redirectUrl) {
        public static BookingRedirectDTO create(
                        com.villadictos.app.model.Habitacion room,
                        LocalDate checkIn,
                        LocalDate checkOut,
                        Integer guests) {

                String redirectUrl = String.format(
                                "https://hotel.com/reservas/nueva?habitacion=%d&checkIn=%s&checkOut=%s&personas=%d",
                                room.getId(), checkIn, checkOut, guests);

                return new BookingRedirectDTO(
                                room.getId(),
                                room.getNumeroHabitacion(),
                                room.getTipoHabitacion().getNombre(),
                                checkIn,
                                checkOut,
                                guests,
                                redirectUrl);
        }
}
