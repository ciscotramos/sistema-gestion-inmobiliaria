package com.inmobiliaria.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Respuesta común para ventas (monto/fecha) y alquileres (monto mensual/periodo). */
public class OperacionResponseDTO {

    private Integer id;
    private String tipo;
    private Integer clienteId;
    private Integer propiedadId;
    private BigDecimal monto;
    private String moneda;
    private String estado;
    private LocalDate fecha;
    private LocalDate fechaFin;
    private String estadoPropiedad;

    public OperacionResponseDTO() {}

    public OperacionResponseDTO(Integer id, String tipo, Integer clienteId, Integer propiedadId,
                                BigDecimal monto, String moneda, String estado,
                                LocalDate fecha, LocalDate fechaFin, String estadoPropiedad) {
        this.id = id;
        this.tipo = tipo;
        this.clienteId = clienteId;
        this.propiedadId = propiedadId;
        this.monto = monto;
        this.moneda = moneda;
        this.estado = estado;
        this.fecha = fecha;
        this.fechaFin = fechaFin;
        this.estadoPropiedad = estadoPropiedad;
    }

    public Integer getId() { return id; }
    public String getTipo() { return tipo; }
    public Integer getClienteId() { return clienteId; }
    public Integer getPropiedadId() { return propiedadId; }
    public BigDecimal getMonto() { return monto; }
    public String getMoneda() { return moneda; }
    public String getEstado() { return estado; }
    public LocalDate getFecha() { return fecha; }
    public LocalDate getFechaFin() { return fechaFin; }
    public String getEstadoPropiedad() { return estadoPropiedad; }
}
