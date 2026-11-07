package com.inmobiliaria.backend.dto;

import com.inmobiliaria.backend.model.PropiedadImagen;
import java.time.LocalDateTime;

public class PropiedadImagenResponseDTO {

    private Integer id;
    private Integer propiedadId;
    private String url;
    private Integer orden;
    private Boolean esPrincipal;
    private LocalDateTime creadoEn;

    public PropiedadImagenResponseDTO() {}

    public PropiedadImagenResponseDTO(PropiedadImagen imagen) {
        if (imagen != null) {
            this.id = imagen.getId();
            this.propiedadId = (imagen.getPropiedad() != null) ? imagen.getPropiedad().getId() : null;
            this.url = imagen.getUrl();
            this.orden = imagen.getOrden();
            this.esPrincipal = imagen.getEsPrincipal();
            this.creadoEn = imagen.getCreadoEn();
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPropiedadId() {
        return propiedadId;
    }

    public void setPropiedadId(Integer propiedadId) {
        this.propiedadId = propiedadId;
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

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}
