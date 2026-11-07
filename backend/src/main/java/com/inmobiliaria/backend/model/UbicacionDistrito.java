package com.inmobiliaria.backend.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "ubicaciones_distritos",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_distrito_prov_dep",
        columnNames = {"nombre", "provincia", "departamento"}
    )
)
public class UbicacionDistrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 80)
    private String provincia = "Lima";

    @Column(nullable = false, length = 80)
    private String departamento = "Lima";

    public UbicacionDistrito() {}

    public UbicacionDistrito(String nombre, String provincia, String departamento) {
        this.nombre = nombre;
        this.provincia = (provincia != null && !provincia.isBlank()) ? provincia : "Lima";
        this.departamento = (departamento != null && !departamento.isBlank()) ? departamento : "Lima";
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
