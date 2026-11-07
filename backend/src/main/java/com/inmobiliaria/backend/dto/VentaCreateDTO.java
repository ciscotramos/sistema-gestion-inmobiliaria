package com.inmobiliaria.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class VentaCreateDTO {

    @NotNull(message = "El cliente es obligatorio")
    private Integer clienteId;

    @NotNull(message = "La propiedad es obligatoria")
    private Integer propiedadId;

    /** Si no se envía, se usa el precio publicado de la propiedad. */
    @DecimalMin(value = "0.00", inclusive = true, message = "El monto debe ser mayor o igual a 0")
    private BigDecimal monto;

    @Size(max = 2000, message = "Las observaciones no pueden exceder los 2000 caracteres")
    @Pattern(regexp = "^[^<>]*$", message = "Las observaciones contienen caracteres no permitidos")
    private String observaciones;

    public Integer getClienteId() { return clienteId; }
    public void setClienteId(Integer clienteId) { this.clienteId = clienteId; }
    public Integer getPropiedadId() { return propiedadId; }
    public void setPropiedadId(Integer propiedadId) { this.propiedadId = propiedadId; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
