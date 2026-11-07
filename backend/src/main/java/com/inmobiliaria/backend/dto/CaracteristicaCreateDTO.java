package com.inmobiliaria.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CaracteristicaCreateDTO {

    @NotBlank(message = "El nombre de la característica es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[^<>]*$", message = "El nombre no puede contener etiquetas HTML ni scripts")
    private String nombre;

    @NotBlank(message = "La categoría es obligatoria")
    @Size(min = 2, max = 50, message = "La categoría debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[^<>]*$", message = "La categoría no puede contener etiquetas HTML ni scripts")
    private String categoria = "General";

    public CaracteristicaCreateDTO() {}

    public CaracteristicaCreateDTO(String nombre, String categoria) {
        this.nombre = nombre;
        this.categoria = (categoria != null && !categoria.isBlank()) ? categoria : "General";
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
}
