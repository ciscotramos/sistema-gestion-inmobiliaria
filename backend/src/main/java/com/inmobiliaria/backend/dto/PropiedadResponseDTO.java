package com.inmobiliaria.backend.dto;

import com.inmobiliaria.backend.model.Propiedad;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PropiedadResponseDTO {

    private Integer id;
    private String titulo;
    private String descripcion;
    private String operacion;
    private String tipo;
    private BigDecimal precio;
    private String moneda;
    private Integer dormitorios;
    private Integer banos;
    private BigDecimal areaConstruida;
    private BigDecimal areaTotal;
    private String direccion;
    private DistritoResponseDTO distrito;
    private Integer distritoId;
    private String ubicacion;
    private UsuarioResumenDTO agente;
    private Integer agenteId;
    private String estado;
    private Boolean destacada;
    private Boolean activo;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
    private List<CaracteristicaResponseDTO> caracteristicas = new ArrayList<>();
    private List<PropiedadImagenResponseDTO> imagenes = new ArrayList<>();
    private List<String> imagenesUrls = new ArrayList<>();
    private String imagenPrincipal;

    public PropiedadResponseDTO() {}

    public PropiedadResponseDTO(Propiedad propiedad) {
        if (propiedad != null) {
            this.id = propiedad.getId();
            this.titulo = propiedad.getTitulo();
            this.descripcion = propiedad.getDescripcion();
            this.operacion = propiedad.getOperacion();
            this.tipo = propiedad.getTipo();
            this.precio = propiedad.getPrecio();
            this.moneda = propiedad.getMoneda();
            this.dormitorios = propiedad.getDormitorios();
            this.banos = propiedad.getBanos();
            this.areaConstruida = propiedad.getAreaConstruida();
            this.areaTotal = propiedad.getAreaTotal();
            this.direccion = propiedad.getDireccion();

            if (propiedad.getDistrito() != null) {
                this.distrito = new DistritoResponseDTO(propiedad.getDistrito());
                this.distritoId = propiedad.getDistrito().getId();
                this.ubicacion = normalizarUbicacion(
                        propiedad.getDireccion(),
                        propiedad.getDistrito().getNombre(),
                        propiedad.getDistrito().getProvincia()
                );
            } else {
                this.ubicacion = (propiedad.getDireccion() != null) ? propiedad.getDireccion().trim() : "";
            }

            if (propiedad.getAgente() != null) {
                this.agente = new UsuarioResumenDTO(propiedad.getAgente());
                this.agenteId = propiedad.getAgente().getId();
            }

            this.estado = propiedad.getEstado();
            this.destacada = propiedad.getDestacada();
            this.activo = propiedad.getActivo();
            this.creadoEn = propiedad.getCreadoEn();
            this.actualizadoEn = propiedad.getActualizadoEn();

            if (propiedad.getCaracteristicas() != null) {
                this.caracteristicas = propiedad.getCaracteristicas().stream()
                        .map(CaracteristicaResponseDTO::new)
                        .toList();
            }

            if (propiedad.getImagenes() != null) {
                this.imagenes = propiedad.getImagenes().stream()
                        .map(PropiedadImagenResponseDTO::new)
                        .toList();

                this.imagenesUrls = propiedad.getImagenes().stream()
                        .map(img -> img.getUrl())
                        .toList();

                this.imagenPrincipal = propiedad.getImagenes().stream()
                        .filter(img -> Boolean.TRUE.equals(img.getEsPrincipal()))
                        .map(img -> img.getUrl())
                        .findFirst()
                        .orElse(this.imagenesUrls.isEmpty() ? null : this.imagenesUrls.get(0));
            }
        }
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

    public DistritoResponseDTO getDistrito() {
        return distrito;
    }

    public void setDistrito(DistritoResponseDTO distrito) {
        this.distrito = distrito;
    }

    public Integer getDistritoId() {
        return distritoId;
    }

    public void setDistritoId(Integer distritoId) {
        this.distritoId = distritoId;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public UsuarioResumenDTO getAgente() {
        return agente;
    }

    public void setAgente(UsuarioResumenDTO agente) {
        this.agente = agente;
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

    public List<CaracteristicaResponseDTO> getCaracteristicas() {
        return caracteristicas;
    }

    public void setCaracteristicas(List<CaracteristicaResponseDTO> caracteristicas) {
        this.caracteristicas = caracteristicas;
    }

    public List<PropiedadImagenResponseDTO> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<PropiedadImagenResponseDTO> imagenes) {
        this.imagenes = imagenes;
    }

    public List<String> getImagenesUrls() {
        return imagenesUrls;
    }

    public void setImagenesUrls(List<String> imagenesUrls) {
        this.imagenesUrls = imagenesUrls;
    }

    public String getImagenPrincipal() {
        return imagenPrincipal;
    }

    public void setImagenPrincipal(String imagenPrincipal) {
        this.imagenPrincipal = imagenPrincipal;
    }

    private String normalizarUbicacion(String direccionRaw, String distrito, String provincia) {
        if (distrito == null || distrito.isBlank()) {
            return direccionRaw != null ? direccionRaw.trim() : "";
        }
        String dist = distrito.trim();
        String prov = (provincia != null && !provincia.isBlank()) ? provincia.trim() : "Lima";
        String sufijoEstandar = dist + ", " + prov + ", Perú";

        if (direccionRaw == null || direccionRaw.isBlank()) {
            return sufijoEstandar;
        }

        String limpia = direccionRaw.trim();
        boolean cambio;
        do {
            cambio = false;
            String antes = limpia;
            limpia = limpia.replaceAll("(?i)(,\\s*Perú)+$", "");
            limpia = limpia.replaceAll("(?i)(,\\s*" + Pattern.quote(prov) + ")+$", "");
            limpia = limpia.replaceAll("(?i)(,\\s*" + Pattern.quote(dist) + ")+$", "");
            limpia = limpia.replaceAll("(?i)(,\\s*Lima)+$", "");
            limpia = limpia.replaceAll("[,\\s]+$", "").trim();
            if (!limpia.equalsIgnoreCase(antes)) {
                cambio = true;
            }
        } while (cambio);

        if (limpia.isBlank()) {
            return sufijoEstandar;
        }

        return limpia + ", " + sufijoEstandar;
    }
}
