package com.villadictos.app.repository;

import com.villadictos.app.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    /**
     * Find reservation by booking token
     */
    Optional<Reserva> findByBookingToken(String bookingToken);

    /**
     * Find reservations by user
     */
    List<Reserva> findByUsuarioIdOrderByFechaCreacionDesc(Long userId);

    /**
     * Check if there are conflicting reservations for a room
     */
    @Query("""
                SELECT COUNT(r) > 0 FROM Reserva r
                WHERE r.habitacion.id = :roomId
                AND r.estado IN ('pendiente', 'confirmada')
                AND r.fechaInicio < :checkOut
                AND r.fechaFin > :checkIn
            """)
    boolean existsConflictingReservation(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    /**
     * Find expired pending reservations
     */
    @Query("""
                SELECT r FROM Reserva r
                WHERE r.estado = 'pendiente'
                AND r.tokenExpiresAt < :now
            """)
    List<Reserva> findExpiredPendingReservations(@Param("now") LocalDateTime now);

    /**
     * Find reservations by status
     */
    List<Reserva> findByEstado(Reserva.EstadoReserva estado);

    /**
     * Find conflicting reservations for a sala by date range
     */
    @Query("""
                SELECT r FROM Reserva r
                WHERE r.sala.id = :salaId
                AND r.estado IN ('pendiente', 'confirmada')
                AND r.fechaInicio < :fechaFin
                AND r.fechaFin > :fechaInicio
            """)
    List<Reserva> findBySalaIdAndFechaRange(
            @Param("salaId") Long salaId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);
}
