package com.inmobiliaria.backend.dto;

import com.inmobiliaria.backend.model.Ticket;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TicketResponseDTO {

    private Integer id;
    private String codigo;
    private String tipo;
    private String titulo;
    private String descripcion;
    private String impacto;
    private String urgencia;
    private String prioridad;
    private String prioridadNombre;
    private Integer tiempoSlaHoras;
    private String estado;
    private String reportadoPor;
    private Integer asignadoAId;
    private String asignadoA;
    private String solucion;
    private Instant fechaRegistro;
    private Instant fechaLimiteSla;
    private Instant fechaResolucion;
    private boolean slaVencido;

    public TicketResponseDTO() {}

    public TicketResponseDTO(Ticket t, LocalDateTime ahora) {
        this.id = t.getId();
        this.codigo = t.getCodigo();
        this.tipo = t.getTipo();
        this.titulo = t.getTitulo();
        this.descripcion = t.getDescripcion();
        this.impacto = t.getImpacto();
        this.urgencia = t.getUrgencia();
        this.prioridad = t.getPrioridad().getNivel();
        this.prioridadNombre = t.getPrioridad().getNombre();
        this.tiempoSlaHoras = t.getPrioridad().getTiempoSlaHoras();
        this.estado = t.getEstado().getNombre();
        this.reportadoPor = t.getReportadoPor().getNombre();
        if (t.getAsignadoA() != null) {
            this.asignadoAId = t.getAsignadoA().getId();
            this.asignadoA = t.getAsignadoA().getNombre();
        }
        this.solucion = t.getSolucion();
        this.fechaRegistro = aInstante(t.getFechaRegistro());
        this.fechaLimiteSla = aInstante(t.getFechaLimiteSla());
        this.fechaResolucion = aInstante(t.getFechaResolucion());
        LocalDateTime referencia = t.getFechaResolucion() != null ? t.getFechaResolucion() : ahora;
        this.slaVencido = referencia.isAfter(t.getFechaLimiteSla());
    }

    /**
     * Las fechas se generan con la hora del servidor; se envían como instantes UTC (sufijo Z)
     * para que cada navegador las muestre en su propia zona horaria.
     */
    private static Instant aInstante(LocalDateTime fecha) {
        return fecha == null ? null : fecha.atZone(ZoneId.systemDefault()).toInstant();
    }

    public Integer getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getImpacto() { return impacto; }
    public String getUrgencia() { return urgencia; }
    public String getPrioridad() { return prioridad; }
    public String getPrioridadNombre() { return prioridadNombre; }
    public Integer getTiempoSlaHoras() { return tiempoSlaHoras; }
    public String getEstado() { return estado; }
    public String getReportadoPor() { return reportadoPor; }
    public Integer getAsignadoAId() { return asignadoAId; }
    public String getAsignadoA() { return asignadoA; }
    public String getSolucion() { return solucion; }
    public Instant getFechaRegistro() { return fechaRegistro; }
    public Instant getFechaLimiteSla() { return fechaLimiteSla; }
    public Instant getFechaResolucion() { return fechaResolucion; }
    public boolean isSlaVencido() { return slaVencido; }
}
