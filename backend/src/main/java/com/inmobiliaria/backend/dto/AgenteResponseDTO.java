package com.inmobiliaria.backend.dto;

import com.inmobiliaria.backend.model.Usuario;

import java.time.LocalDateTime;

public class AgenteResponseDTO {

    private Integer id;
    private String nombre;
    private String email;
    private String rol;
    private String telefono;
    private String cargo;
    private String estado;
    private LocalDateTime creadoEn;

    public AgenteResponseDTO() {}

    public AgenteResponseDTO(Usuario u) {
        this.id = u.getId();
        this.nombre = u.getNombre();
        this.email = u.getEmail();
        this.rol = u.getRol();
        this.telefono = u.getTelefono();
        this.cargo = u.getCargo();
        this.estado = u.getEstado();
        this.creadoEn = u.getCreadoEn();
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}
