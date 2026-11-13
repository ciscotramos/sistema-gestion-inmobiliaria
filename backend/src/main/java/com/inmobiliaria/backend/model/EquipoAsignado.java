package com.inmobiliaria.backend.model;

import jakarta.persistence.*;

/** Equipo de soporte responsable de un ticket: Help Desk (N1), Especialista DB (N2), Desarrollo (N3). Tabla 'asignado_a'. */
@Entity
@Table(name = "asignado_a")
public class EquipoAsignado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    @Column(name = "nivel_soporte", nullable = false)
    private Integer nivelSoporte;

    public EquipoAsignado() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getNivelSoporte() { return nivelSoporte; }
    public void setNivelSoporte(Integer nivelSoporte) { this.nivelSoporte = nivelSoporte; }
}
