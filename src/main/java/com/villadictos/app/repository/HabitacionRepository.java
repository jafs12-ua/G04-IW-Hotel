package com.villadictos.app.repository;

import com.villadictos.app.model.Habitacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {

    /**
     * Find all rooms that are not reserved or blocked during the given date range
     */
    @Query("""
                SELECT h FROM Habitacion h
                WHERE h.id NOT IN (
                    SELECT r.habitacion.id FROM Reserva r
                    WHERE r.habitacion IS NOT NULL
                    AND r.estado IN ('pendiente', 'confirmada')
                    AND r.fechaInicio < :checkOut
                    AND r.fechaFin > :checkIn
                )
                AND h.id NOT IN (
                    SELECT b.habitacion.id FROM Bloqueo b
                    WHERE b.habitacion IS NOT NULL
                    AND b.fechaInicio < :checkOut
                    AND b.fechaFin > :checkIn
                )
            """)
    Page<Habitacion> findAvailableByDateRange(
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            Pageable pageable);

    /**
     * Find available rooms by room type during the given date range
     */
    @Query("""
                SELECT h FROM Habitacion h
                WHERE h.tipoHabitacion.id = :tipoId
                AND h.id NOT IN (
                    SELECT r.habitacion.id FROM Reserva r
                    WHERE r.habitacion IS NOT NULL
                    AND r.estado IN ('pendiente', 'confirmada')
                    AND r.fechaInicio < :checkOut
                    AND r.fechaFin > :checkIn
                )
                AND h.id NOT IN (
                    SELECT b.habitacion.id FROM Bloqueo b
                    WHERE b.habitacion IS NOT NULL
                    AND b.fechaInicio < :checkOut
                    AND b.fechaFin > :checkIn
                )
            """)
    List<Habitacion> findAvailableByTypeAndDateRange(
            @Param("tipoId") Long tipoId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    /**
     * Find available rooms with price filter
     */
    @Query("""
                SELECT h FROM Habitacion h
                WHERE h.tipoHabitacion.precioBaseNoche BETWEEN :minPrice AND :maxPrice
                AND h.id NOT IN (
                    SELECT r.habitacion.id FROM Reserva r
                    WHERE r.habitacion IS NOT NULL
                    AND r.estado IN ('pendiente', 'confirmada')
                    AND r.fechaInicio < :checkOut
                    AND r.fechaFin > :checkIn
                )
                AND h.id NOT IN (
                    SELECT b.habitacion.id FROM Bloqueo b
                    WHERE b.habitacion IS NOT NULL
                    AND b.fechaInicio < :checkOut
                    AND b.fechaFin > :checkIn
                )
            """)
    Page<Habitacion> findAvailableByDateRangeAndPriceRange(
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    /**
     * Check if a specific room is available for given dates
     */
    @Query("""
                SELECT COUNT(h) > 0 FROM Habitacion h
                WHERE h.id = :roomId
                AND h.id NOT IN (
                    SELECT r.habitacion.id FROM Reserva r
                    WHERE r.habitacion IS NOT NULL
                    AND r.estado IN ('pendiente', 'confirmada')
                    AND r.fechaInicio < :checkOut
                    AND r.fechaFin > :checkIn
                )
                AND h.id NOT IN (
                    SELECT b.habitacion.id FROM Bloqueo b
                    WHERE b.habitacion IS NOT NULL
                    AND b.fechaInicio < :checkOut
                    AND b.fechaFin > :checkIn
                )
            """)
    boolean isRoomAvailable(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    List<Habitacion> findByDestacadaTrue();
}
