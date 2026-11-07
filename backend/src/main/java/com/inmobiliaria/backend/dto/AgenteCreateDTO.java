package com.inmobiliaria.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AgenteCreateDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 120, message = "El nombre debe tener entre 2 y 120 caracteres")
    @Pattern(regexp = "^[^<>]*$", message = "El nombre no puede contener etiquetas HTML ni scripts")
    private String nombre;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "Formato de correo electrónico inválido")
    @Size(max = 150, message = "El correo no debe exceder 150 caracteres")
    @Pattern(regexp = "^[^<>]*$", message = "El correo no puede contener etiquetas HTML ni scripts")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener al menos 6 caracteres")
    @Pattern(regexp = "^[^<>]*$", message = "La contraseña no puede contener etiquetas HTML ni scripts")
    private String password;

    @Pattern(regexp = "^[0-9+() -]*$", message = "Formato de teléfono inválido")
    private String telefono;

    @Size(max = 80, message = "El cargo no debe exceder 80 caracteres")
    @Pattern(regexp = "^[^<>]*$", message = "El cargo no puede contener etiquetas HTML ni scripts")
    private String cargo = "Asesor Inmobiliario";

    public AgenteCreateDTO() {}

    public AgenteCreateDTO(String nombre, String email, String password, String telefono, String cargo) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.telefono = telefono;
        this.cargo = (cargo != null && !cargo.isBlank()) ? cargo : "Asesor Inmobiliario";
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
}
