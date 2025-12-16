package com.villadictos.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "habitaciones")
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_habitacion", nullable = false, unique = true, length = 20)
    private String numeroHabitacion;

    @Column(nullable = false)
    private Integer planta;

    @Column(length = 100)
    private String vistas;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_tipo", nullable = false)
    private TipoHabitacion tipoHabitacion;

    @Column(nullable = false)
    private Boolean destacada = false;

    public Habitacion() {
    }

    public Habitacion(String numeroHabitacion, Integer planta, String vistas, TipoHabitacion tipoHabitacion,
            Boolean destacada) {
        this.numeroHabitacion = numeroHabitacion;
        this.planta = planta;
        this.vistas = vistas;
        this.tipoHabitacion = tipoHabitacion;
        this.destacada = destacada;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroHabitacion() {
        return numeroHabitacion;
    }

    public void setNumeroHabitacion(String numeroHabitacion) {
        this.numeroHabitacion = numeroHabitacion;
    }

    public Integer getPlanta() {
        return planta;
    }

    public void setPlanta(Integer planta) {
        this.planta = planta;
    }

    public String getVistas() {
        return vistas;
    }

    public void setVistas(String vistas) {
        this.vistas = vistas;
    }

    public TipoHabitacion getTipoHabitacion() {
        return tipoHabitacion;
    }

    public void setTipoHabitacion(TipoHabitacion tipoHabitacion) {
        this.tipoHabitacion = tipoHabitacion;
    }

    public Boolean getDestacada() {
        return destacada;
    }

    public void setDestacada(Boolean destacada) {
        this.destacada = destacada;
    }
}
