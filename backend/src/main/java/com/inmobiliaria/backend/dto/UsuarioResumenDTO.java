package com.inmobiliaria.backend.dto;

import com.inmobiliaria.backend.model.Usuario;

public class UsuarioResumenDTO {

    private Integer id;
    private String nombre;
    private String email;
    private String telefono;
    private String cargo;
    private String fotoUrl;

    public UsuarioResumenDTO() {}

    public UsuarioResumenDTO(Usuario usuario) {
        if (usuario != null) {
            this.id = usuario.getId();
            this.nombre = usuario.getNombre();
            this.email = usuario.getEmail();
            this.telefono = usuario.getTelefono();
            this.cargo = usuario.getCargo();
            this.fotoUrl = usuario.getFotoUrl();
        }
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

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }
}
