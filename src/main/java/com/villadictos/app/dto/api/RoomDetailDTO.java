package com.villadictos.app.dto.api;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for room detail endpoint response
 */
public record RoomDetailDTO(
        Long id,
        String number,
        String type,
        Integer capacity,
        Integer size,
        BigDecimal pricePerNight,
        String season,
        String description,
        List<String> amenities,
        List<String> images,
        Boolean available,
        Integer floor) {
    public static RoomDetailDTO fromHabitacion(
            com.villadictos.app.model.Habitacion h,
            String season,
            BigDecimal calculatedPrice,
            Boolean available,
            List<String> amenities,
            List<String> images) {
        return new RoomDetailDTO(
                h.getId(),
                h.getNumeroHabitacion(),
                h.getTipoHabitacion().getNombre(),
                h.getTipoHabitacion().getCapacidadPersonas(),
                calculateSize(h.getTipoHabitacion().getNombre()),
                calculatedPrice,
                season,
                h.getTipoHabitacion().getDescripcion(),
                amenities,
                images,
                available,
                h.getPlanta());
    }

    private static Integer calculateSize(String type) {
        // Approximate room sizes based on type
        return switch (type.toLowerCase()) {
            case "individual" -> 15;
            case "doble estándar" -> 25;
            case "doble superior" -> 30;
            case "suite junior" -> 40;
            case "suite premium" -> 55;
            default -> 25;
        };
    }
}
