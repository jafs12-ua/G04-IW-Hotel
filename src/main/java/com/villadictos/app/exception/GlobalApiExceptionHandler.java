package com.villadictos.app.exception;

import com.villadictos.app.dto.api.ApiErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler for API endpoints
 */
@RestControllerAdvice(basePackages = "com.villadictos.app.controller.api")
public class GlobalApiExceptionHandler {

        @ExceptionHandler(RoomNotFoundException.class)
        public ResponseEntity<ApiErrorDTO> handleRoomNotFound(RoomNotFoundException ex, HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiErrorDTO.of("Room not found", ex.getMessage(), 404, request.getRequestURI()));
        }

        @ExceptionHandler(RoomNotAvailableException.class)
        public ResponseEntity<ApiErrorDTO> handleRoomNotAvailable(RoomNotAvailableException ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiErrorDTO.of("Room not available", ex.getMessage(), 409,
                                                request.getRequestURI()));
        }

        @ExceptionHandler(InvalidBookingException.class)
        public ResponseEntity<ApiErrorDTO> handleInvalidBooking(InvalidBookingException ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiErrorDTO.of("Invalid booking", ex.getMessage(), 400, request.getRequestURI()));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorDTO> handleValidation(MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                String message = ex.getBindingResult().getFieldErrors().stream()
                                .map(FieldError::getDefaultMessage)
                                .collect(Collectors.joining(", "));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiErrorDTO.of("Validation error", message, 400, request.getRequestURI()));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorDTO> handleGeneric(Exception ex, HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiErrorDTO.of("Internal error", "An unexpected error occurred", 500,
                                                request.getRequestURI()));
        }
}
