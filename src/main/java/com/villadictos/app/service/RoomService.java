package com.villadictos.app.service;

import com.villadictos.app.dto.api.RoomDetailDTO;
import com.villadictos.app.dto.api.RoomListDTO;
import com.villadictos.app.exception.RoomNotFoundException;
import com.villadictos.app.model.Habitacion;
import com.villadictos.app.repository.HabitacionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RoomService {

    private final HabitacionRepository habitacionRepository;
    private final SeasonService seasonService;

    public RoomService(HabitacionRepository habitacionRepository, SeasonService seasonService) {
        this.habitacionRepository = habitacionRepository;
        this.seasonService = seasonService;
    }

    /**
     * List available rooms with filters
     */
    public Page<RoomListDTO> findAvailableRooms(
            LocalDate checkIn,
            LocalDate checkOut,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String season,
            Pageable pageable) {

        Page<Habitacion> rooms;

        // Apply date and price filters
        if (checkIn != null && checkOut != null) {
            if (minPrice != null && maxPrice != null) {
                rooms = habitacionRepository.findAvailableByDateRangeAndPriceRange(
                        checkIn, checkOut, minPrice, maxPrice, pageable);
            } else {
                rooms = habitacionRepository.findAvailableByDateRange(checkIn, checkOut, pageable);
            }
        } else {
            rooms = habitacionRepository.findAll(pageable);
        }

        // Convert to DTOs with pricing
        LocalDate priceDate = checkIn != null ? checkIn : LocalDate.now();
        String currentSeason = seasonService.getSeasonType(priceDate);

        // Filter by season if specified
        List<Habitacion> filteredRooms = rooms.getContent();
        if (season != null && !season.isEmpty()) {
            // Only include if current season matches requested season
            if (!currentSeason.equalsIgnoreCase(season)) {
                filteredRooms = List.of();
            }
        }

        List<RoomListDTO> dtos = filteredRooms.stream()
                .map(room -> toRoomListDTO(room, currentSeason, checkIn, checkOut))
                .toList();

        return new PageImpl<>(dtos, pageable, rooms.getTotalElements());
    }

    /**
     * Get detailed room information
     */
    public RoomDetailDTO findRoomById(Long id) {
        Habitacion room = habitacionRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));

        LocalDate today = LocalDate.now();
        String season = seasonService.getSeasonType(today);
        BigDecimal price = seasonService.calculateSeasonPrice(
                room.getTipoHabitacion().getPrecioBaseNoche(), today);

        // Check availability for next 30 days
        boolean available = isRoomAvailable(id, today, today.plusDays(30));

        List<String> amenities = getAmenitiesForRoomType(room.getTipoHabitacion().getNombre());
        List<String> images = generateImageUrls(room);

        return RoomDetailDTO.fromHabitacion(room, season, price, available, amenities, images);
    }

    /**
     * Check if a specific room is available for given dates
     */
    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return habitacionRepository.isRoomAvailable(roomId, checkIn, checkOut);
    }

    /**
     * Calculate total price for a room stay
     */
    public BigDecimal calculateTotalPrice(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        Habitacion room = habitacionRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));

        BigDecimal pricePerNight = seasonService.calculateSeasonPrice(
                room.getTipoHabitacion().getPrecioBaseNoche(), checkIn);

        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        return pricePerNight.multiply(BigDecimal.valueOf(nights));
    }

    private RoomListDTO toRoomListDTO(Habitacion room, String season, LocalDate checkIn, LocalDate checkOut) {
        LocalDate priceDate = checkIn != null ? checkIn : LocalDate.now();
        BigDecimal price = seasonService.calculateSeasonPrice(
                room.getTipoHabitacion().getPrecioBaseNoche(), priceDate);

        boolean available = checkIn != null && checkOut != null
                ? isRoomAvailable(room.getId(), checkIn, checkOut)
                : true;

        List<String> amenities = getAmenitiesForRoomType(room.getTipoHabitacion().getNombre());

        return RoomListDTO.fromHabitacion(room, season, price, available, amenities);
    }

    private List<String> getAmenitiesForRoomType(String type) {
        return switch (type.toLowerCase()) {
            case "individual" -> Arrays.asList("WiFi", "TV", "Aire acondicionado");
            case "doble estándar" -> Arrays.asList("WiFi", "TV", "Aire acondicionado", "Minibar");
            case "doble superior" -> Arrays.asList("WiFi", "TV 4K", "Aire acondicionado", "Minibar", "Caja fuerte");
            case "suite junior" ->
                Arrays.asList("WiFi", "TV 4K", "Aire acondicionado", "Minibar", "Caja fuerte", "Salón");
            case "suite premium" -> Arrays.asList("WiFi", "TV 4K", "Aire acondicionado", "Minibar", "Jacuzzi", "Balcón",
                    "Servicio habitaciones 24h");
            default -> Arrays.asList("WiFi", "TV");
        };
    }

    private List<String> generateImageUrls(Habitacion room) {
        String baseUrl = "https://hotel.com/images/room-";
        return Arrays.asList(
                baseUrl + room.getNumeroHabitacion() + "-1.jpg",
                baseUrl + room.getNumeroHabitacion() + "-2.jpg");
    }
}
