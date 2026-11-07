package com.inmobiliaria.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA mapeada a la tabla 'propiedad_imagenes'.
 * Galería multimedia de fotografías asociadas al inmueble.
 */
@Entity
@Table(name = "propiedad_imagenes")
public class PropiedadImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propiedad_id", nullable = false)
    private Propiedad propiedad;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(nullable = false)
    private Integer orden = 0;

    @Column(name = "es_principal", nullable = false)
    private Boolean esPrincipal = false;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private LocalDateTime creadoEn;

    public PropiedadImagen() {}

    public PropiedadImagen(Propiedad propiedad, String url, Integer orden, Boolean esPrincipal) {
        this.propiedad = propiedad;
        this.url = url;
        this.orden = (orden != null) ? orden : 0;
        this.esPrincipal = (esPrincipal != null) ? esPrincipal : false;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Propiedad getPropiedad() {
        return propiedad;
    }

    public void setPropiedad(Propiedad propiedad) {
        this.propiedad = propiedad;
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
