package com.inmobiliaria.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class PropiedadEstadoDTO {

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(
        regexp = "^(Disponible|Reservado|Vendido|Alquilado|Inactivo)$",
        message = "El estado debe ser: Disponible, Reservado, Vendido, Alquilado o Inactivo"
    )
    private String estado;

    public PropiedadEstadoDTO() {}

    public PropiedadEstadoDTO(String estado) {
        this.estado = estado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
