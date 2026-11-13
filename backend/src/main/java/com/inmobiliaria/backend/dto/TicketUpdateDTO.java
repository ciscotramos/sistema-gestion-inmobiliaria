package com.inmobiliaria.backend.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Atención de un ticket por el administrador: cambio de estado, asignación y solución. */
public class TicketUpdateDTO {

    @Pattern(regexp = "^(Abierto|En Proceso|Resuelto|Cerrado)$", message = "Estado no válido")
    private String estado;

    private Integer asignadoAId;

    private Integer equipoAsignadoId;

    @Size(max = 2000, message = "La solución no puede superar 2000 caracteres")
    @Pattern(regexp = "^[^<>]*$", message = "La solución no puede contener etiquetas HTML ni scripts")
    private String solucion;

    public TicketUpdateDTO() {}

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Integer getAsignadoAId() { return asignadoAId; }
    public void setAsignadoAId(Integer asignadoAId) { this.asignadoAId = asignadoAId; }
    public Integer getEquipoAsignadoId() { return equipoAsignadoId; }
    public void setEquipoAsignadoId(Integer equipoAsignadoId) { this.equipoAsignadoId = equipoAsignadoId; }
    public String getSolucion() { return solucion; }
    public void setSolucion(String solucion) { this.solucion = solucion; }
}
