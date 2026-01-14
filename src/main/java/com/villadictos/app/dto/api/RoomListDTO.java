package com.villadictos.app.dto.api;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for room list endpoint response
 */
public record RoomListDTO(
        Long id,
        String number,
        String type,
        Integer capacity,
        BigDecimal pricePerNight,
        String season,
        Boolean available,
        List<String> amenities) {
    public static RoomListDTO fromHabitacion(
            com.villadictos.app.model.Habitacion h,
            String season,
            BigDecimal calculatedPrice,
            Boolean available,
            List<String> amenities) {
        return new RoomListDTO(
                h.getId(),
                h.getNumeroHabitacion(),
                h.getTipoHabitacion().getNombre(),
                h.getTipoHabitacion().getCapacidadPersonas(),
                calculatedPrice,
                season,
                available,
                amenities);
    }
}
