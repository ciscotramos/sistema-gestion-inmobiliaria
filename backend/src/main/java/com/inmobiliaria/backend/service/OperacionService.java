package com.inmobiliaria.backend.service;

import com.inmobiliaria.backend.dto.AlquilerCreateDTO;
import com.inmobiliaria.backend.dto.OperacionResponseDTO;
import com.inmobiliaria.backend.dto.VentaCreateDTO;
import com.inmobiliaria.backend.exception.PropiedadNoDisponibleException;
import com.inmobiliaria.backend.exception.ResourceNotFoundException;
import com.inmobiliaria.backend.model.Alquiler;
import com.inmobiliaria.backend.model.Operacion;
import com.inmobiliaria.backend.model.Propiedad;
import com.inmobiliaria.backend.model.Usuario;
import com.inmobiliaria.backend.repository.AlquilerRepository;
import com.inmobiliaria.backend.repository.OperacionRepository;
import com.inmobiliaria.backend.repository.PropiedadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Registro de ventas y alquileres.
 *
 * Cada operación bloquea la fila de la propiedad con PESSIMISTIC_WRITE
 * (SELECT ... FOR UPDATE) para que, si dos asesores confirman al mismo tiempo,
 * solo uno lo logre; el segundo espera, ve el nuevo estado y recibe HTTP 409.
 */
@Service
@Transactional(readOnly = true)
public class OperacionService {

    private final PropiedadRepository propiedadRepository;
    private final OperacionRepository operacionRepository;
    private final AlquilerRepository alquilerRepository;
    private final PropiedadService propiedadService;

    public OperacionService(
            PropiedadRepository propiedadRepository,
            OperacionRepository operacionRepository,
            AlquilerRepository alquilerRepository,
            PropiedadService propiedadService
    ) {
        this.propiedadRepository = propiedadRepository;
        this.operacionRepository = operacionRepository;
        this.alquilerRepository = alquilerRepository;
        this.propiedadService = propiedadService;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public OperacionResponseDTO registrarVenta(VentaCreateDTO dto, Usuario usuarioActual) {
        Propiedad propiedad = bloquearPropiedad(dto.getPropiedadId());
        propiedadService.validarPropiedadDeAgente(propiedad, usuarioActual);
        validarDisponible(propiedad, "Venta");

        Operacion venta = new Operacion();
        venta.setClienteId(dto.getClienteId());
        venta.setPropiedadId(propiedad.getId());
        venta.setAgenteId(propiedad.getAgente().getId());
        venta.setTipo("Venta");
        venta.setMonto(dto.getMonto() != null ? dto.getMonto() : propiedad.getPrecio());
        venta.setMoneda(propiedad.getMoneda());
        venta.setFecha(LocalDate.now());
        venta.setEstado("Concretada");
        venta.setObservaciones(dto.getObservaciones());
        Operacion guardada = operacionRepository.save(venta);

        propiedad.setEstado("Vendido");
        propiedadRepository.save(propiedad);

        return new OperacionResponseDTO(guardada.getId(), "Venta", guardada.getClienteId(),
                propiedad.getId(), guardada.getMonto(), guardada.getMoneda(), guardada.getEstado(),
                guardada.getFecha(), null, propiedad.getEstado());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public OperacionResponseDTO registrarAlquiler(AlquilerCreateDTO dto, Usuario usuarioActual) {
        Propiedad propiedad = bloquearPropiedad(dto.getPropiedadId());
        propiedadService.validarPropiedadDeAgente(propiedad, usuarioActual);
        validarDisponible(propiedad, "Alquiler");

        Alquiler alquiler = new Alquiler();
        alquiler.setClienteId(dto.getClienteId());
        alquiler.setPropiedadId(propiedad.getId());
        alquiler.setFechaInicio(dto.getFechaInicio());
        alquiler.setFechaFin(dto.getFechaFin());
        BigDecimal mensual = dto.getMontoMensual() != null ? dto.getMontoMensual() : propiedad.getPrecio();
        alquiler.setMontoMensual(mensual);
        alquiler.setMoneda(propiedad.getMoneda());
        alquiler.setEstado("Activo");
        alquiler.setCondiciones(dto.getCondiciones());
        Alquiler guardado = alquilerRepository.save(alquiler);

        propiedad.setEstado("Alquilado");
        propiedadRepository.save(propiedad);

        return new OperacionResponseDTO(guardado.getId(), "Alquiler", guardado.getClienteId(),
                propiedad.getId(), guardado.getMontoMensual(), guardado.getMoneda(), guardado.getEstado(),
                guardado.getFechaInicio(), guardado.getFechaFin(), propiedad.getEstado());
    }

    private Propiedad bloquearPropiedad(Integer propiedadId) {
        return propiedadRepository.findByIdForUpdate(propiedadId)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada con ID: " + propiedadId));
    }

    private void validarDisponible(Propiedad propiedad, String operacionEsperada) {
        if (!Boolean.TRUE.equals(propiedad.getActivo()) || !"Disponible".equals(propiedad.getEstado())) {
            throw new PropiedadNoDisponibleException(
                    "La propiedad " + propiedad.getId() + " ya no está disponible (estado actual: "
                            + propiedad.getEstado() + ").");
        }
        if (!operacionEsperada.equals(propiedad.getOperacion())) {
            throw new PropiedadNoDisponibleException(
                    "La propiedad " + propiedad.getId() + " está publicada para " + propiedad.getOperacion()
                            + ", no para " + operacionEsperada + ".");
        }
    }
}
