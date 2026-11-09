package com.inmobiliaria.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos del formulario de reporte de incidentes. La prioridad no se envía: el servidor la calcula. */
public class TicketCreateDTO {

    @NotBlank(message = "El tipo de incidencia es obligatorio")
    @Size(max = 60, message = "El tipo no puede superar 60 caracteres")
    private String tipo;

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

    public TicketCreateDTO() {}

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getImpacto() { return impacto; }
    public void setImpacto(String impacto) { this.impacto = impacto; }
    public String getUrgencia() { return urgencia; }
    public void setUrgencia(String urgencia) { this.urgencia = urgencia; }
}
