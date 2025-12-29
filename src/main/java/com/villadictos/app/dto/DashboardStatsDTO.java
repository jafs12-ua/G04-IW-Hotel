package com.villadictos.app.dto;

import java.math.BigDecimal;

/**
 * DTO para las estadísticas del dashboard de recepción
 */
public class DashboardStatsDTO {
    private long totalHabitaciones;
    private long habitacionesLibres;
    private long habitacionesOcupadas;
    private long habitacionesBloqueadas;
    private String temporadaActual;
    private BigDecimal factorPrecio;
    private long checkInsHoy;
    private long checkOutsHoy;
    private long reservasPendientes;
    private long totalClientes;

    public DashboardStatsDTO() {
    }

    // Getters y Setters
    public long getTotalHabitaciones() {
        return totalHabitaciones;
    }

    public void setTotalHabitaciones(long totalHabitaciones) {
        this.totalHabitaciones = totalHabitaciones;
    }

    public long getHabitacionesLibres() {
        return habitacionesLibres;
    }

    public void setHabitacionesLibres(long habitacionesLibres) {
        this.habitacionesLibres = habitacionesLibres;
    }

    public long getHabitacionesOcupadas() {
        return habitacionesOcupadas;
    }

    public void setHabitacionesOcupadas(long habitacionesOcupadas) {
        this.habitacionesOcupadas = habitacionesOcupadas;
    }

    public long getHabitacionesBloqueadas() {
        return habitacionesBloqueadas;
    }

    public void setHabitacionesBloqueadas(long habitacionesBloqueadas) {
        this.habitacionesBloqueadas = habitacionesBloqueadas;
    }

    public String getTemporadaActual() {
        return temporadaActual;
    }

    public void setTemporadaActual(String temporadaActual) {
        this.temporadaActual = temporadaActual;
    }

    public BigDecimal getFactorPrecio() {
        return factorPrecio;
    }

    public void setFactorPrecio(BigDecimal factorPrecio) {
        this.factorPrecio = factorPrecio;
    }

    public long getCheckInsHoy() {
        return checkInsHoy;
    }

    public void setCheckInsHoy(long checkInsHoy) {
        this.checkInsHoy = checkInsHoy;
    }

    public long getCheckOutsHoy() {
        return checkOutsHoy;
    }

    public void setCheckOutsHoy(long checkOutsHoy) {
        this.checkOutsHoy = checkOutsHoy;
    }

    public long getReservasPendientes() {
        return reservasPendientes;
    }

    public void setReservasPendientes(long reservasPendientes) {
        this.reservasPendientes = reservasPendientes;
    }

    public long getTotalClientes() {
        return totalClientes;
    }

    public void setTotalClientes(long totalClientes) {
        this.totalClientes = totalClientes;
    }
}
