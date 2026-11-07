package com.inmobiliaria.backend.dto;

import com.inmobiliaria.backend.model.Caracteristica;

public class CaracteristicaResponseDTO {

    private Integer id;
    private String nombre;
    private String categoria;

    public CaracteristicaResponseDTO() {}

    public CaracteristicaResponseDTO(Integer id, String nombre, String categoria) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
    }

    public CaracteristicaResponseDTO(Caracteristica entity) {
        this.id = entity.getId();
        this.nombre = entity.getNombre();
        this.categoria = entity.getCategoria();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
