package com.inmobiliaria.backend.dto;

import com.inmobiliaria.backend.model.UbicacionDistrito;

public class DistritoResponseDTO {

    private Integer id;
    private String nombre;
    private String provincia;
    private String departamento;

    public DistritoResponseDTO() {}

    public DistritoResponseDTO(Integer id, String nombre, String provincia, String departamento) {
        this.id = id;
        this.nombre = nombre;
        this.provincia = provincia;
        this.departamento = departamento;
    }

    public DistritoResponseDTO(UbicacionDistrito entity) {
        this.id = entity.getId();
        this.nombre = entity.getNombre();
        this.provincia = entity.getProvincia();
        this.departamento = entity.getDepartamento();
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
