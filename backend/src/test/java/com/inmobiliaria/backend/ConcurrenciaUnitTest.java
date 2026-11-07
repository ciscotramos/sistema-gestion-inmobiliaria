package com.inmobiliaria.backend;

import com.inmobiliaria.backend.dto.ErrorResponseDTO;
import com.inmobiliaria.backend.dto.PropiedadUpdateDTO;
import com.inmobiliaria.backend.exception.GlobalExceptionHandler;
import com.inmobiliaria.backend.exception.PropiedadNoDisponibleException;
import com.inmobiliaria.backend.model.Propiedad;
import com.inmobiliaria.backend.model.Usuario;
import com.inmobiliaria.backend.repository.CaracteristicaRepository;
import com.inmobiliaria.backend.repository.PropiedadImagenRepository;
import com.inmobiliaria.backend.repository.PropiedadRepository;
import com.inmobiliaria.backend.repository.UbicacionDistritoRepository;
import com.inmobiliaria.backend.repository.UsuarioRepository;
import com.inmobiliaria.backend.service.PropiedadService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Bloqueo optimista (@Version) y su traducción a HTTP 409. Sin base de datos. */
class ConcurrenciaUnitTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/propiedades/10");

    @Test
    void conflictoOptimista_seTraduceA409() {
        ResponseEntity<ErrorResponseDTO> resp = handler.handleOptimisticLockingFailure(
                new ObjectOptimisticLockingFailureException(Propiedad.class, 10), request);

        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
        assertEquals(409, resp.getBody().getStatus());
    }

    @Test
    void bloqueoPesimistaNoObtenido_seTraduceA409() {
        ResponseEntity<ErrorResponseDTO> resp = handler.handlePessimisticLockingFailure(
                new CannotAcquireLockException("timeout"), request);

        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
    }

    @Test
    void propiedadNoDisponible_seTraduceA409() {
        ResponseEntity<ErrorResponseDTO> resp = handler.handlePropiedadNoDisponible(
                new PropiedadNoDisponibleException("ya vendida"), request);

        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
        assertEquals("ya vendida", resp.getBody().getMessage());
    }

    @Test
    void actualizarConVersionVieja_lanzaConflictoOptimista() {
        PropiedadRepository propiedadRepository = mock(PropiedadRepository.class);
        PropiedadService service = new PropiedadService(
                propiedadRepository,
                mock(UbicacionDistritoRepository.class),
                mock(UsuarioRepository.class),
                mock(CaracteristicaRepository.class),
                mock(PropiedadImagenRepository.class));

        Propiedad propiedad = new Propiedad();
        propiedad.setId(10);
        propiedad.setVersion(3);
        when(propiedadRepository.findById(10)).thenReturn(Optional.of(propiedad));

        Usuario admin = new Usuario();
        admin.setId(1);
        admin.setRol("ADMIN");

        PropiedadUpdateDTO dto = new PropiedadUpdateDTO();
        dto.setVersion(2); // el cliente leyó la versión 2, pero otro usuario ya guardó la 3

        assertThrows(OptimisticLockingFailureException.class,
                () -> service.actualizarPropiedad(10, dto, admin));
    }
}
