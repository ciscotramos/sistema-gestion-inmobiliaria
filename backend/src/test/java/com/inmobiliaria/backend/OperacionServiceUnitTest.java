package com.inmobiliaria.backend;

import com.inmobiliaria.backend.dto.AlquilerCreateDTO;
import com.inmobiliaria.backend.dto.OperacionResponseDTO;
import com.inmobiliaria.backend.dto.VentaCreateDTO;
import com.inmobiliaria.backend.exception.PropiedadNoDisponibleException;
import com.inmobiliaria.backend.exception.ResourceNotFoundException;
import com.inmobiliaria.backend.model.Operacion;
import com.inmobiliaria.backend.model.Propiedad;
import com.inmobiliaria.backend.model.Usuario;
import com.inmobiliaria.backend.repository.AlquilerRepository;
import com.inmobiliaria.backend.repository.OperacionRepository;
import com.inmobiliaria.backend.repository.PropiedadRepository;
import com.inmobiliaria.backend.service.OperacionService;
import com.inmobiliaria.backend.service.PropiedadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Pruebas unitarias (sin base de datos) de las reglas de venta y alquiler. */
@ExtendWith(MockitoExtension.class)
class OperacionServiceUnitTest {

    @Mock private PropiedadRepository propiedadRepository;
    @Mock private OperacionRepository operacionRepository;
    @Mock private AlquilerRepository alquilerRepository;
    @Mock private PropiedadService propiedadService;

    private OperacionService service;
    private Usuario agente;

    @BeforeEach
    void setUp() {
        service = new OperacionService(propiedadRepository, operacionRepository, alquilerRepository, propiedadService);
        agente = new Usuario();
        agente.setId(2);
        agente.setRol("AGENTE");
    }

    private Propiedad propiedad(String operacion, String estado, boolean activo) {
        Propiedad p = new Propiedad();
        p.setId(10);
        p.setOperacion(operacion);
        p.setEstado(estado);
        p.setActivo(activo);
        p.setPrecio(new BigDecimal("485000.00"));
        p.setMoneda("USD");
        p.setAgente(agente);
        return p;
    }

    private VentaCreateDTO ventaDto() {
        VentaCreateDTO dto = new VentaCreateDTO();
        dto.setClienteId(7);
        dto.setPropiedadId(10);
        return dto;
    }

    @Test
    void registrarVenta_marcaLaPropiedadComoVendidaYUsaSuPrecio() {
        Propiedad p = propiedad("Venta", "Disponible", true);
        when(propiedadRepository.findByIdForUpdate(10)).thenReturn(Optional.of(p));
        when(operacionRepository.save(any(Operacion.class))).thenAnswer(inv -> inv.getArgument(0));

        OperacionResponseDTO resp = service.registrarVenta(ventaDto(), agente);

        assertEquals("Vendido", p.getEstado());
        assertEquals("Concretada", resp.getEstado());
        assertEquals(new BigDecimal("485000.00"), resp.getMonto());
        verify(propiedadRepository).save(p);
    }

    @Test
    void registrarVenta_siYaFueVendida_lanzaConflictoYNoGuardaNada() {
        Propiedad p = propiedad("Venta", "Vendido", true);
        when(propiedadRepository.findByIdForUpdate(10)).thenReturn(Optional.of(p));

        assertThrows(PropiedadNoDisponibleException.class, () -> service.registrarVenta(ventaDto(), agente));

        verify(operacionRepository, never()).save(any());
    }

    @Test
    void registrarVenta_siLaPropiedadEsDeAlquiler_lanzaConflicto() {
        Propiedad p = propiedad("Alquiler", "Disponible", true);
        when(propiedadRepository.findByIdForUpdate(10)).thenReturn(Optional.of(p));

        assertThrows(PropiedadNoDisponibleException.class, () -> service.registrarVenta(ventaDto(), agente));
    }

    @Test
    void registrarVenta_siNoEsSuPropiedad_lanzaAccessDenied() {
        Propiedad p = propiedad("Venta", "Disponible", true);
        when(propiedadRepository.findByIdForUpdate(10)).thenReturn(Optional.of(p));
        doThrow(new AccessDeniedException("no le pertenece"))
                .when(propiedadService).validarPropiedadDeAgente(p, agente);

        assertThrows(AccessDeniedException.class, () -> service.registrarVenta(ventaDto(), agente));

        verify(operacionRepository, never()).save(any());
    }

    @Test
    void registrarVenta_siLaPropiedadNoExiste_lanzaNotFound() {
        when(propiedadRepository.findByIdForUpdate(10)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.registrarVenta(ventaDto(), agente));
    }

    @Test
    void registrarAlquiler_marcaLaPropiedadComoAlquilada() {
        Propiedad p = propiedad("Alquiler", "Disponible", true);
        when(propiedadRepository.findByIdForUpdate(10)).thenReturn(Optional.of(p));
        when(alquilerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AlquilerCreateDTO dto = new AlquilerCreateDTO();
        dto.setClienteId(7);
        dto.setPropiedadId(10);
        dto.setFechaInicio(LocalDate.of(2026, 12, 1));
        dto.setFechaFin(LocalDate.of(2027, 11, 30));

        OperacionResponseDTO resp = service.registrarAlquiler(dto, agente);

        assertEquals("Alquilado", p.getEstado());
        assertEquals("Activo", resp.getEstado());
        assertEquals(LocalDate.of(2027, 11, 30), resp.getFechaFin());
    }

    @Test
    void alquilerDto_rechazaFechaFinAnteriorALaDeInicio() {
        AlquilerCreateDTO dto = new AlquilerCreateDTO();
        dto.setFechaInicio(LocalDate.of(2026, 12, 1));
        dto.setFechaFin(LocalDate.of(2026, 11, 1));

        assertEquals(false, dto.isRangoFechasValido());
    }
}
