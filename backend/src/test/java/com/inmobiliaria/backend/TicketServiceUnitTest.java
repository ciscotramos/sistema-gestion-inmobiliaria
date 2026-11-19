package com.inmobiliaria.backend;

import com.inmobiliaria.backend.dto.TicketCreateDTO;
import com.inmobiliaria.backend.dto.TicketResponseDTO;
import com.inmobiliaria.backend.dto.TicketUpdateDTO;
import com.inmobiliaria.backend.exception.TicketInvalidoException;
import com.inmobiliaria.backend.model.EquipoAsignado;
import com.inmobiliaria.backend.model.EstadoTicket;
import com.inmobiliaria.backend.model.Prioridad;
import com.inmobiliaria.backend.model.Ticket;
import com.inmobiliaria.backend.model.Usuario;
import com.inmobiliaria.backend.repository.EquipoAsignadoRepository;
import com.inmobiliaria.backend.repository.EstadoTicketRepository;
import com.inmobiliaria.backend.repository.PrioridadRepository;
import com.inmobiliaria.backend.repository.TicketRepository;
import com.inmobiliaria.backend.repository.UsuarioRepository;
import com.inmobiliaria.backend.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** Gestión de incidentes: matriz de prioridad, SLA, ciclo de vida y visibilidad. Sin base de datos. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TicketServiceUnitTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private PrioridadRepository prioridadRepository;
    @Mock private EstadoTicketRepository estadoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private EquipoAsignadoRepository equipoRepository;

    private TicketService service;
    private Usuario agente;
    private Usuario admin;

    @BeforeEach
    void setUp() {
        service = new TicketService(ticketRepository, prioridadRepository, estadoRepository, usuarioRepository, equipoRepository);
        agente = usuario(2, "AGENTE");
        admin = usuario(1, "ADMIN");
        when(prioridadRepository.findByNivel("P1")).thenReturn(Optional.of(prioridad("P1", "Crítico", 2)));
        when(prioridadRepository.findByNivel("P2")).thenReturn(Optional.of(prioridad("P2", "Alto", 8)));
        when(prioridadRepository.findByNivel("P3")).thenReturn(Optional.of(prioridad("P3", "Normal", 24)));
        when(prioridadRepository.findByNivel("P4")).thenReturn(Optional.of(prioridad("P4", "Bajo", 72)));
        for (String e : List.of("Abierto", "En Proceso", "Resuelto", "Cerrado")) {
            when(estadoRepository.findByNombre(e)).thenReturn(Optional.of(estado(e)));
        }
        when(ticketRepository.saveAndFlush(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(7);
            return t;
        });
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static Usuario usuario(int id, String rol) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombre("Usuario " + id);
        u.setRol(rol);
        u.setEstado("Activo");
        return u;
    }

    private static Prioridad prioridad(String nivel, String nombre, int horas) {
        Prioridad p = new Prioridad();
        p.setNivel(nivel);
        p.setNombre(nombre);
        p.setTiempoSlaHoras(horas);
        return p;
    }

    private static EstadoTicket estado(String nombre) {
        EstadoTicket e = new EstadoTicket();
        e.setNombre(nombre);
        return e;
    }

    private static TicketCreateDTO dto(String impacto, String urgencia) {
        TicketCreateDTO d = new TicketCreateDTO();
        d.setTipo("Errores de funcionamiento");
        d.setModuloAfectado("Clientes");
        d.setTitulo("Error 500 al registrar cliente");
        d.setDescripcion("Al guardar un cliente nuevo la API responde 500.");
        d.setImpacto(impacto);
        d.setUrgencia(urgencia);
        return d;
    }

    private Ticket ticketAbierto() {
        TicketResponseDTO r = service.registrar(dto("Alto", "Alta"), agente);
        Ticket t = new Ticket();
        t.setId(r.getId());
        t.setCodigo(r.getCodigo());
        t.setPrioridad(prioridad("P1", "Crítico", 2));
        t.setEstado(estado("Abierto"));
        t.setReportadoPor(agente);
        t.setFechaRegistro(LocalDateTime.now());
        t.setFechaLimiteSla(LocalDateTime.now().plusHours(2));
        when(ticketRepository.findById(7)).thenReturn(Optional.of(t));
        return t;
    }

    @Test
    void matrizDePrioridad_impactoPorUrgencia() {
        assertEquals("P1", TicketService.calcularNivel("Alto", "Alta"));
        assertEquals("P2", TicketService.calcularNivel("Alto", "Media"));
        assertEquals("P2", TicketService.calcularNivel("Medio", "Alta"));
        assertEquals("P3", TicketService.calcularNivel("Medio", "Media"));
        assertEquals("P3", TicketService.calcularNivel("Alto", "Baja"));
        assertEquals("P3", TicketService.calcularNivel("Bajo", "Alta"));
        assertEquals("P4", TicketService.calcularNivel("Medio", "Baja"));
        assertEquals("P4", TicketService.calcularNivel("Bajo", "Media"));
        assertEquals("P4", TicketService.calcularNivel("Bajo", "Baja"));
    }

    @Test
    void valorDeImpactoInvalido_sePideCorregir() {
        assertThrows(TicketInvalidoException.class, () -> TicketService.calcularNivel("Enorme", "Alta"));
    }

    @Test
    void registrar_generaCodigoPrioridadYLimiteSla() {
        TicketResponseDTO r = service.registrar(dto("Alto", "Alta"), agente);

        assertEquals("INC-00007", r.getCodigo());
        assertEquals("P1", r.getPrioridad());
        assertEquals("Abierto", r.getEstado());
        assertEquals(Duration.ofHours(2), Duration.between(r.getFechaRegistro(), r.getFechaLimiteSla()));
        assertFalse(r.isSlaVencido());
    }

    @Test
    void registrar_rechazaUnTipoQueNoEstaEnElCatalogo() {
        TicketCreateDTO d = dto("Alto", "Alta");
        d.setTipo("Tipo inventado");
        assertThrows(TicketInvalidoException.class, () -> service.registrar(d, agente));
    }

    @Test
    void registrar_guardaElModuloAfectado() {
        TicketResponseDTO r = service.registrar(dto("Alto", "Alta"), agente);
        assertEquals("Clientes", r.getModuloAfectado());
    }

    @Test
    void registrar_rechazaUnModuloQueNoEstaEnElCatalogo() {
        TicketCreateDTO d = dto("Alto", "Alta");
        d.setModuloAfectado("Modulo inventado");
        assertThrows(TicketInvalidoException.class, () -> service.registrar(d, agente));
    }

    @Test
    void registrar_respetaLaPrioridadElegidaPorElUsuario() {
        TicketCreateDTO d = dto("Bajo", "Baja"); // la matriz daría P4
        d.setPrioridad("P1");
        TicketResponseDTO r = service.registrar(d, agente);

        assertEquals("P1", r.getPrioridad());
        assertEquals(Duration.ofHours(2), Duration.between(r.getFechaRegistro(), r.getFechaLimiteSla()));
    }

    @Test
    void registrar_sinPrioridadElegidaUsaLaMatrizItil() {
        TicketResponseDTO r = service.registrar(dto("Bajo", "Baja"), agente);
        assertEquals("P4", r.getPrioridad());
    }

    @Test
    void atender_asignarUnEquipoPermitePonerEnProceso() {
        ticketAbierto();
        EquipoAsignado dba = new EquipoAsignado();
        dba.setId(2);
        dba.setNombre("Especialista DB");
        dba.setNivelSoporte(2);
        when(equipoRepository.findById(2)).thenReturn(Optional.of(dba));

        TicketUpdateDTO u = new TicketUpdateDTO();
        u.setEquipoAsignadoId(2);
        u.setEstado("En Proceso");
        TicketResponseDTO r = service.atender(7, u);

        assertEquals("En Proceso", r.getEstado());
        assertEquals("Especialista DB", r.getEquipoAsignado());
        assertEquals(2, r.getEquipoNivel());
    }

    @Test
    void atender_equipoInexistenteDaNoEncontrado() {
        ticketAbierto();
        when(equipoRepository.findById(99)).thenReturn(Optional.empty());
        TicketUpdateDTO u = new TicketUpdateDTO();
        u.setEquipoAsignadoId(99);
        assertThrows(com.inmobiliaria.backend.exception.ResourceNotFoundException.class, () -> service.atender(7, u));
    }

    @Test
    void cicloDeVida_soloPermiteLasTransicionesDefinidas() {
        assertTrue(TicketService.transicionPermitida("Abierto", "En Proceso"));
        assertTrue(TicketService.transicionPermitida("En Proceso", "Resuelto"));
        assertTrue(TicketService.transicionPermitida("Resuelto", "Cerrado"));
        assertTrue(TicketService.transicionPermitida("Resuelto", "En Proceso"));
        assertFalse(TicketService.transicionPermitida("Abierto", "Cerrado"));
        assertFalse(TicketService.transicionPermitida("Abierto", "Resuelto"));
        assertFalse(TicketService.transicionPermitida("Cerrado", "Abierto"));
    }

    @Test
    void atender_noPermitePonerEnProcesoSinResponsable() {
        ticketAbierto();
        TicketUpdateDTO u = new TicketUpdateDTO();
        u.setEstado("En Proceso");
        assertThrows(TicketInvalidoException.class, () -> service.atender(7, u));
    }

    @Test
    void atender_noPermiteResolverSinSolucion() {
        Ticket t = ticketAbierto();
        t.setEstado(estado("En Proceso"));
        t.setAsignadoA(admin);
        TicketUpdateDTO u = new TicketUpdateDTO();
        u.setEstado("Resuelto");
        assertThrows(TicketInvalidoException.class, () -> service.atender(7, u));
    }

    @Test
    void atender_flujoCompletoRegistraResolucion() {
        Ticket t = ticketAbierto();
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(admin));

        TicketUpdateDTO enProceso = new TicketUpdateDTO();
        enProceso.setAsignadoAId(1);
        enProceso.setEstado("En Proceso");
        assertEquals("En Proceso", service.atender(7, enProceso).getEstado());

        TicketUpdateDTO resolver = new TicketUpdateDTO();
        resolver.setEstado("Resuelto");
        resolver.setSolucion("Se corrigió la validación del DTO y se desplegó el parche.");
        TicketResponseDTO r = service.atender(7, resolver);

        assertEquals("Resuelto", r.getEstado());
        assertNotNull(r.getFechaResolucion());
        assertFalse(r.isSlaVencido());
        assertNotNull(t.getSolucion());
    }

    @Test
    void ticketResueltoFueraDelLimite_marcaSlaVencido() {
        Ticket t = ticketAbierto();
        t.setFechaLimiteSla(LocalDateTime.now().minusHours(1));
        assertTrue(new TicketResponseDTO(t, LocalDateTime.now()).isSlaVencido());
    }

    @Test
    void listar_elAgenteSoloVeLosSuyosYElAdminTodos() {
        service.listar(agente);
        service.listar(admin);
        org.mockito.Mockito.verify(ticketRepository).findByReportadoPorIdOrderByFechaRegistroDescIdDesc(2);
        org.mockito.Mockito.verify(ticketRepository).findAllByOrderByFechaRegistroDescIdDesc();
    }

    @Test
    void obtener_unAgenteNoPuedeVerElTicketDeOtro() {
        Ticket t = ticketAbierto();
        t.setReportadoPor(usuario(99, "AGENTE"));
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> service.obtener(7, agente));
    }
}
