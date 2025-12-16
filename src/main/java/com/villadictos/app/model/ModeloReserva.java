package com.villadictos.app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "modelos_reserva")
public class ModeloReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio_adicional_noche", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioAdicionalNoche = BigDecimal.ZERO;

    public ModeloReserva() {
    }

    public ModeloReserva(String nombre, String descripcion, BigDecimal precioAdicionalNoche) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioAdicionalNoche = precioAdicionalNoche;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecioAdicionalNoche() {
        return precioAdicionalNoche;
    }

    public void setPrecioAdicionalNoche(BigDecimal precioAdicionalNoche) {
        this.precioAdicionalNoche = precioAdicionalNoche;
    }
}
