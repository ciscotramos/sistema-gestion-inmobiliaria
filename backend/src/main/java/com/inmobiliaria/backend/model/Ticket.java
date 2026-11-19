package com.inmobiliaria.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Incidente o solicitud de servicio reportado por un usuario (tabla 'tickets'). */
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 60)
    private String tipo;

    @Column(name = "modulo_afectado", nullable = false, length = 60)
    private String moduloAfectado;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, length = 10)
    private String impacto;

    @Column(nullable = false, length = 10)
    private String urgencia;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "prioridad_id", nullable = false)
    private Prioridad prioridad;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoTicket estado;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "reportado_por", nullable = false)
    private Usuario reportadoPor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asignado_a")
    private Usuario asignadoA;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipo_asignado_id")
    private EquipoAsignado equipoAsignado;

    @Column(columnDefinition = "TEXT")
    private String solucion;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_limite_sla", nullable = false)
    private LocalDateTime fechaLimiteSla;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    public Ticket() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getModuloAfectado() { return moduloAfectado; }
    public void setModuloAfectado(String moduloAfectado) { this.moduloAfectado = moduloAfectado; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getImpacto() { return impacto; }
    public void setImpacto(String impacto) { this.impacto = impacto; }
    public String getUrgencia() { return urgencia; }
    public void setUrgencia(String urgencia) { this.urgencia = urgencia; }
    public Prioridad getPrioridad() { return prioridad; }
    public void setPrioridad(Prioridad prioridad) { this.prioridad = prioridad; }
    public EstadoTicket getEstado() { return estado; }
    public void setEstado(EstadoTicket estado) { this.estado = estado; }
    public Usuario getReportadoPor() { return reportadoPor; }
    public void setReportadoPor(Usuario reportadoPor) { this.reportadoPor = reportadoPor; }
    public Usuario getAsignadoA() { return asignadoA; }
    public void setAsignadoA(Usuario asignadoA) { this.asignadoA = asignadoA; }
    public EquipoAsignado getEquipoAsignado() { return equipoAsignado; }
    public void setEquipoAsignado(EquipoAsignado equipoAsignado) { this.equipoAsignado = equipoAsignado; }
    public String getSolucion() { return solucion; }
    public void setSolucion(String solucion) { this.solucion = solucion; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public LocalDateTime getFechaLimiteSla() { return fechaLimiteSla; }
    public void setFechaLimiteSla(LocalDateTime fechaLimiteSla) { this.fechaLimiteSla = fechaLimiteSla; }
    public LocalDateTime getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(LocalDateTime fechaResolucion) { this.fechaResolucion = fechaResolucion; }
}
