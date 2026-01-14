package com.villadictos.app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "servicios")
public class Servicio {

    public enum TipoServicio {
        POR_DIA("Por día"),
        POR_RESERVA("Por reserva"),
        POR_USO("Por uso");

        private final String valor;

        TipoServicio(String valor) {
            this.valor = valor;
        }

        public String getValor() {
            return valor;
        }

        public static TipoServicio fromString(String text) {
            for (TipoServicio t : TipoServicio.values()) {
                if (t.valor.equalsIgnoreCase(text)) {
                    return t;
                }
            }
            return POR_USO;
        }
    }

    @jakarta.persistence.Converter(autoApply = true)
    public static class TipoServicioConverter implements AttributeConverter<TipoServicio, String> {
        @Override
        public String convertToDatabaseColumn(TipoServicio tipo) {
            return tipo == null ? null : tipo.getValor();
        }

        @Override
        public TipoServicio convertToEntityAttribute(String dbData) {
            return dbData == null ? null : TipoServicio.fromString(dbData);
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Convert(converter = TipoServicioConverter.class)
    @Column(nullable = false)
    private TipoServicio tipo;

    public Servicio() {
    }

    public Servicio(String nombre, String descripcion, BigDecimal precio, TipoServicio tipo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.tipo = tipo;
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

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public TipoServicio getTipo() {
        return tipo;
    }

    public void setTipo(TipoServicio tipo) {
        this.tipo = tipo;
    }
}
