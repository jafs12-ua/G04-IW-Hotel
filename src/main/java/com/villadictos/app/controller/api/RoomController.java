package com.villadictos.app.controller.api;

import com.villadictos.app.dto.api.PageResponseDTO;
import com.villadictos.app.dto.api.RoomDetailDTO;
import com.villadictos.app.dto.api.RoomListDTO;
import com.villadictos.app.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/rooms")
@Tag(name = "Rooms", description = "Room management endpoints")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    @Operation(summary = "List available rooms", description = "Get a paginated list of rooms with optional filters")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List retrieved successfully")
    })
    public ResponseEntity<PageResponseDTO<RoomListDTO>> listRooms(
            @Parameter(description = "Check-in date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,

            @Parameter(description = "Check-out date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,

            @Parameter(description = "Minimum price per night") @RequestParam(required = false) BigDecimal minPrice,

            @Parameter(description = "Maximum price per night") @RequestParam(required = false) BigDecimal maxPrice,

            @Parameter(description = "Season filter (ALTA, MEDIA, BAJA)") @RequestParam(required = false) String season,

            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort by field (price, capacity, type)") @RequestParam(defaultValue = "price") String sort) {

        // Map sort parameter to entity field
        String sortField = switch (sort.toLowerCase()) {
            case "price" -> "tipoHabitacion.precioBaseNoche";
            case "capacity" -> "tipoHabitacion.capacidadPersonas";
            case "type" -> "tipoHabitacion.nombre";
            default -> "tipoHabitacion.precioBaseNoche";
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortField));
        Page<RoomListDTO> rooms = roomService.findAvailableRooms(
                checkIn, checkOut, minPrice, maxPrice, season, pageable);

        return ResponseEntity.ok(PageResponseDTO.from(rooms));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get room details", description = "Get detailed information about a specific room")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Room found"),
            @ApiResponse(responseCode = "404", description = "Room not found", content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorDTO")))
    })
    public ResponseEntity<RoomDetailDTO> getRoomDetail(
            @Parameter(description = "Room ID") @PathVariable Long id) {
        RoomDetailDTO room = roomService.findRoomById(id);
        return ResponseEntity.ok(room);
    }
}
