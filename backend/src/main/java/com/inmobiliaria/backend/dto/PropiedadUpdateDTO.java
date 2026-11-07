package com.inmobiliaria.backend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class PropiedadUpdateDTO {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede exceder los 200 caracteres")
    private String titulo;

    private String descripcion;

    @NotBlank(message = "La operación es obligatoria")
    @Pattern(regexp = "^(Venta|Alquiler)$", message = "La operación debe ser 'Venta' o 'Alquiler'")
    private String operacion;

    @NotBlank(message = "El tipo de propiedad es obligatorio")
    @Pattern(
        regexp = "^(Casa|Departamento|Terreno|Oficina|Local Comercial)$",
        message = "El tipo debe ser: Casa, Departamento, Terreno, Oficina o Local Comercial"
    )
    private String tipo;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.00", inclusive = true, message = "El precio debe ser mayor o igual a 0")
    private BigDecimal precio;

    @Pattern(regexp = "^(USD|PEN)$", message = "La moneda debe ser 'USD' o 'PEN'")
    private String moneda;

    @Min(value = 0, message = "Los dormitorios deben ser mayor o igual a 0")
    private Integer dormitorios;

    @Min(value = 0, message = "Los baños deben ser mayor o igual a 0")
    private Integer banos;

    @DecimalMin(value = "0.00", inclusive = true, message = "El área construida debe ser mayor o igual a 0")
    private BigDecimal areaConstruida;

    @DecimalMin(value = "0.00", inclusive = true, message = "El área total debe ser mayor o igual a 0")
    private BigDecimal areaTotal;

    @Size(max = 255, message = "La dirección no puede exceder los 255 caracteres")
    private String direccion;

    @NotNull(message = "El distrito es obligatorio")
    private Integer distritoId;

    private Integer agenteId;

    @Pattern(
        regexp = "^(Disponible|Reservado|Vendido|Alquilado|Inactivo)$",
        message = "El estado debe ser: Disponible, Reservado, Vendido, Alquilado o Inactivo"
    )
    private String estado;

    private Boolean destacada;

    private Boolean activo;

    private Set<Integer> caracteristicasIds = new HashSet<>();

    public PropiedadUpdateDTO() {}

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

    public Integer getDistritoId() {
        return distritoId;
    }

    public void setDistritoId(Integer distritoId) {
        this.distritoId = distritoId;
    }

    public Integer getAgenteId() {
        return agenteId;
    }

    public void setAgenteId(Integer agenteId) {
        this.agenteId = agenteId;
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

    public Set<Integer> getCaracteristicasIds() {
        return caracteristicasIds;
    }

    public void setCaracteristicasIds(Set<Integer> caracteristicasIds) {
        this.caracteristicasIds = caracteristicasIds;
    }
}
