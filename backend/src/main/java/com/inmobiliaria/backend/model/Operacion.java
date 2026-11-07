package com.inmobiliaria.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Operación de venta registrada sobre una propiedad (tabla 'operaciones'). */
@Entity
@Table(name = "operaciones")
public class Operacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cliente_id", nullable = false)
    private Integer clienteId;

    @Column(name = "propiedad_id", nullable = false)
    private Integer propiedadId;

    @Column(name = "agente_id", nullable = false)
    private Integer agenteId;

    @Column(nullable = false, length = 20)
    private String tipo = "Venta";

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 3)
    private String moneda = "USD";

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 30)
    private String estado = "Concretada";

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private LocalDateTime creadoEn;

    public Operacion() {}

    public Integer getId() { return id; }
    public Integer getClienteId() { return clienteId; }
    public void setClienteId(Integer clienteId) { this.clienteId = clienteId; }
    public Integer getPropiedadId() { return propiedadId; }
    public void setPropiedadId(Integer propiedadId) { this.propiedadId = propiedadId; }
    public Integer getAgenteId() { return agenteId; }
    public void setAgenteId(Integer agenteId) { this.agenteId = agenteId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
}
