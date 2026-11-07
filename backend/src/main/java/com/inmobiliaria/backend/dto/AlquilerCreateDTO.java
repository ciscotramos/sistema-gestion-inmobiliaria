package com.inmobiliaria.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public class AlquilerCreateDTO {

    @NotNull(message = "El cliente es obligatorio")
    private Integer clienteId;

    @NotNull(message = "La propiedad es obligatoria")
    private Integer propiedadId;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    /** Si no se envía, se usa el precio publicado de la propiedad. */
    @DecimalMin(value = "0.00", inclusive = true, message = "El monto mensual debe ser mayor o igual a 0")
    private BigDecimal montoMensual;

    @Size(max = 2000, message = "Las condiciones no pueden exceder los 2000 caracteres")
    @Pattern(regexp = "^[^<>]*$", message = "Las condiciones contienen caracteres no permitidos")
    private String condiciones;

    @JsonIgnore
    @AssertTrue(message = "La fecha de fin debe ser posterior a la fecha de inicio")
    public boolean isRangoFechasValido() {
        return fechaInicio == null || fechaFin == null || fechaFin.isAfter(fechaInicio);
    }

    public Integer getClienteId() { return clienteId; }
    public void setClienteId(Integer clienteId) { this.clienteId = clienteId; }
    public Integer getPropiedadId() { return propiedadId; }
    public void setPropiedadId(Integer propiedadId) { this.propiedadId = propiedadId; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public BigDecimal getMontoMensual() { return montoMensual; }
    public void setMontoMensual(BigDecimal montoMensual) { this.montoMensual = montoMensual; }
    public String getCondiciones() { return condiciones; }
    public void setCondiciones(String condiciones) { this.condiciones = condiciones; }
}
