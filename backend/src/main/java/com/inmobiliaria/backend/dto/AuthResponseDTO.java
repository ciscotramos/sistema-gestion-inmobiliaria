package com.inmobiliaria.backend.dto;

public class AuthResponseDTO {

    private String token;
    private String rol;
    private String nombre;
    private String email;

    public AuthResponseDTO() {}

    public AuthResponseDTO(String token, String rol, String nombre, String email) {
        this.token = token;
        this.rol = rol;
        this.nombre = nombre;
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
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
}
