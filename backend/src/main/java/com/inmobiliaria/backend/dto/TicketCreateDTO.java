package com.inmobiliaria.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos del formulario de reporte de incidentes. La prioridad es opcional: si falta, el servidor la calcula. */
public class TicketCreateDTO {

    @NotBlank(message = "El tipo de incidencia es obligatorio")
    @Size(max = 60, message = "El tipo no puede superar 60 caracteres")
    private String tipo;

    @NotBlank(message = "El módulo afectado es obligatorio")
    @Size(max = 60, message = "El módulo no puede superar 60 caracteres")
    private String moduloAfectado;

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 5, max = 150, message = "El título debe tener entre 5 y 150 caracteres")
    @Pattern(regexp = "^[^<>]*$", message = "El título no puede contener etiquetas HTML ni scripts")
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
    @Pattern(regexp = "^[^<>]*$", message = "La descripción no puede contener etiquetas HTML ni scripts")
    private String descripcion;

    @NotBlank(message = "El impacto es obligatorio")
    @Pattern(regexp = "^(Alto|Medio|Bajo)$", message = "El impacto debe ser Alto, Medio o Bajo")
    private String impacto;

    @NotBlank(message = "La urgencia es obligatoria")
    @Pattern(regexp = "^(Alta|Media|Baja)$", message = "La urgencia debe ser Alta, Media o Baja")
    private String urgencia;

    /** Opcional: si el usuario no la elige, el servidor la calcula con la matriz impacto x urgencia. */
    @Pattern(regexp = "^P[1-4]$", message = "La prioridad debe ser P1, P2, P3 o P4")
    private String prioridad;

    public TicketCreateDTO() {}

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getModuloAfectado() { return moduloAfectado; }
    public void setModuloAfectado(String moduloAfectado) { this.moduloAfectado = moduloAfectado; }
    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getImpacto() { return impacto; }
    public void setImpacto(String impacto) { this.impacto = impacto; }
    public String getUrgencia() { return urgencia; }
    public void setUrgencia(String urgencia) { this.urgencia = urgencia; }
}
