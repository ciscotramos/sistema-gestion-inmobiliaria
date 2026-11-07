package com.inmobiliaria.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class DistritoCreateDTO {

    @NotBlank(message = "El nombre del distrito es obligatorio")
    @Size(min = 2, max = 80, message = "El nombre debe tener entre 2 y 80 caracteres")
    @Pattern(regexp = "^[^<>]*$", message = "El nombre no puede contener etiquetas HTML ni scripts")
    private String nombre;

    @NotBlank(message = "La provincia es obligatoria")
    @Size(min = 2, max = 80, message = "La provincia debe tener entre 2 y 80 caracteres")
    @Pattern(regexp = "^[^<>]*$", message = "La provincia no puede contener etiquetas HTML ni scripts")
    private String provincia = "Lima";

    @NotBlank(message = "El departamento es obligatorio")
    @Size(min = 2, max = 80, message = "El departamento debe tener entre 2 y 80 caracteres")
    @Pattern(regexp = "^[^<>]*$", message = "El departamento no puede contener etiquetas HTML ni scripts")
    private String departamento = "Lima";

    public DistritoCreateDTO() {}

    public DistritoCreateDTO(String nombre, String provincia, String departamento) {
        this.nombre = nombre;
        this.provincia = (provincia != null && !provincia.isBlank()) ? provincia : "Lima";
        this.departamento = (departamento != null && !departamento.isBlank()) ? departamento : "Lima";
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }
}
