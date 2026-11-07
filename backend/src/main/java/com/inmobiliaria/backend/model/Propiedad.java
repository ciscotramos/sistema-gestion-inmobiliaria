package com.inmobiliaria.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entidad JPA mapeada a la tabla 'propiedades'.
 * Inmuebles comercializados (Venta / Alquiler).
 */
@Entity
@Table(name = "propiedades")
public class Propiedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, length = 20)
    private String operacion; // 'Venta' o 'Alquiler'

    @Column(nullable = false, length = 40)
    private String tipo; // 'Casa', 'Departamento', 'Terreno', 'Oficina', 'Local Comercial'

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false, length = 3)
    private String moneda = "USD"; // 'USD' o 'PEN'

    @Column
    private Integer dormitorios;

    @Column
    private Integer banos;

    @Column(name = "area_construida", precision = 10, scale = 2)
    private BigDecimal areaConstruida;

    @Column(name = "area_total", precision = 10, scale = 2)
    private BigDecimal areaTotal;

    @Column(length = 255)
    private String direccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distrito_id", nullable = false)
    private UbicacionDistrito distrito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_id", nullable = false)
    private Usuario agente;

    @Column(nullable = false, length = 20)
    private String estado = "Disponible"; // 'Disponible', 'Reservado', 'Vendido', 'Alquilado', 'Inactivo'

    @Column(nullable = false)
    private Boolean destacada = false;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", insertable = false, updatable = false)
    private LocalDateTime actualizadoEn;

    @OneToMany(mappedBy = "propiedad", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orden ASC, id ASC")
    private List<PropiedadImagen> imagenes = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "propiedad_caracteristicas",
        joinColumns = @JoinColumn(name = "propiedad_id"),
        inverseJoinColumns = @JoinColumn(name = "caracteristica_id")
    )
    private Set<Caracteristica> caracteristicas = new HashSet<>();

    public Propiedad() {}

    public Propiedad(
            String titulo,
            String descripcion,
            String operacion,
            String tipo,
            BigDecimal precio,
            String moneda,
            Integer dormitorios,
            Integer banos,
            BigDecimal areaConstruida,
            BigDecimal areaTotal,
            String direccion,
            UbicacionDistrito distrito,
            Usuario agente,
            String estado,
            Boolean destacada,
            Boolean activo
    ) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.operacion = operacion;
        this.tipo = tipo;
        this.precio = precio;
        this.moneda = (moneda != null && !moneda.isBlank()) ? moneda : "USD";
        this.dormitorios = dormitorios;
        this.banos = banos;
        this.areaConstruida = areaConstruida;
        this.areaTotal = areaTotal;
        this.direccion = direccion;
        this.distrito = distrito;
        this.agente = agente;
        this.estado = (estado != null && !estado.isBlank()) ? estado : "Disponible";
        this.destacada = (destacada != null) ? destacada : false;
        this.activo = (activo != null) ? activo : true;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getOperacion() {
        return operacion;
    }

    public void setOperacion(String operacion) {
        this.operacion = operacion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public Integer getDormitorios() {
        return dormitorios;
    }

    public void setDormitorios(Integer dormitorios) {
        this.dormitorios = dormitorios;
    }

    public Integer getBanos() {
        return banos;
    }

    public void setBanos(Integer banos) {
        this.banos = banos;
    }

    public BigDecimal getAreaConstruida() {
        return areaConstruida;
    }

    public void setAreaConstruida(BigDecimal areaConstruida) {
        this.areaConstruida = areaConstruida;
    }

    public BigDecimal getAreaTotal() {
        return areaTotal;
    }

    public void setAreaTotal(BigDecimal areaTotal) {
        this.areaTotal = areaTotal;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public UbicacionDistrito getDistrito() {
        return distrito;
    }

    public void setDistrito(UbicacionDistrito distrito) {
        this.distrito = distrito;
    }

    public Usuario getAgente() {
        return agente;
    }

    public void setAgente(Usuario agente) {
        this.agente = agente;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Boolean getDestacada() {
        return destacada;
    }

    public void setDestacada(Boolean destacada) {
        this.destacada = destacada;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }

    public List<PropiedadImagen> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<PropiedadImagen> imagenes) {
        this.imagenes = imagenes;
    }

    public Set<Caracteristica> getCaracteristicas() {
        return caracteristicas;
    }

    public void setCaracteristicas(Set<Caracteristica> caracteristicas) {
        this.caracteristicas = caracteristicas;
    }
}
