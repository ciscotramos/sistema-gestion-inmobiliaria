package com.inmobiliaria.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class PropiedadImagenCreateDTO {

    @NotBlank(message = "La URL de la imagen es obligatoria")
    private String url;

    private Integer orden = 0;

    private Boolean esPrincipal = false;

    public PropiedadImagenCreateDTO() {}

    public PropiedadImagenCreateDTO(String url, Integer orden, Boolean esPrincipal) {
        this.url = url;
        this.orden = (orden != null) ? orden : 0;
        this.esPrincipal = (esPrincipal != null) ? esPrincipal : false;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public Boolean getEsPrincipal() {
        return esPrincipal;
    }

    public void setEsPrincipal(Boolean esPrincipal) {
        this.esPrincipal = esPrincipal;
    }
}
