package com.villadictos.app.controller.api;

import com.villadictos.app.dto.api.BookingRedirectDTO;
import com.villadictos.app.exception.InvalidBookingException;
import com.villadictos.app.exception.RoomNotFoundException;
import com.villadictos.app.exception.RoomNotAvailableException;
import com.villadictos.app.model.Habitacion;
import com.villadictos.app.repository.HabitacionRepository;
import com.villadictos.app.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "Booking redirect endpoint")
public class BookingController {

    private final HabitacionRepository habitacionRepository;
    private final RoomService roomService;

    public BookingController(HabitacionRepository habitacionRepository, RoomService roomService) {
        this.habitacionRepository = habitacionRepository;
        this.roomService = roomService;
    }

    @GetMapping("/start")
    @Operation(summary = "Start booking", description = "Validate room availability and get redirect URL to booking tunnel")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking can proceed - redirect URL provided"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorDTO"))),
            @ApiResponse(responseCode = "404", description = "Room not found", content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorDTO"))),
            @ApiResponse(responseCode = "409", description = "Room not available for selected dates", content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorDTO")))
    })
    public ResponseEntity<BookingRedirectDTO> startBooking(
            @Parameter(description = "Room ID", required = true) @RequestParam Long roomId,

            @Parameter(description = "Check-in date (YYYY-MM-DD)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,

            @Parameter(description = "Check-out date (YYYY-MM-DD)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,

            @Parameter(description = "Number of guests", required = true) @RequestParam Integer guests) {

        // Validate dates
        if (checkOut.isBefore(checkIn) || checkOut.equals(checkIn)) {
            throw new InvalidBookingException("Check-out date must be after check-in date");
        }

        // Find the room
        Habitacion room = habitacionRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));

        // Check availability
        if (!roomService.isRoomAvailable(roomId, checkIn, checkOut)) {
            throw new RoomNotAvailableException(roomId, checkIn, checkOut);
        }

        // Validate capacity
        if (guests > room.getTipoHabitacion().getCapacidadPersonas()) {
            throw new InvalidBookingException(
                    "Room capacity is " + room.getTipoHabitacion().getCapacidadPersonas() +
                            " guests, but " + guests + " requested");
        }

        // Return redirect DTO
        BookingRedirectDTO response = BookingRedirectDTO.create(room, checkIn, checkOut, guests);
        return ResponseEntity.ok(response);
    }
}
