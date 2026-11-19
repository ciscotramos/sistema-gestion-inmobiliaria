package com.inmobiliaria.backend.service;

import com.inmobiliaria.backend.dto.TicketCreateDTO;
import com.inmobiliaria.backend.dto.TicketResponseDTO;
import com.inmobiliaria.backend.dto.TicketUpdateDTO;
import com.inmobiliaria.backend.exception.ResourceNotFoundException;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Gestión de incidentes alineada con ITIL: registro, priorización (impacto x urgencia),
 * cálculo del límite SLA, atención con estados controlados y cierre.
 */
@Service
@Transactional(readOnly = true)
public class TicketService {

    /** Las 7 tipologías del catálogo de servicios (docs/solicitudes-servicio.md). */
    public static final List<String> TIPOS = List.of(
            "Solicitud de información",
            "Creación y gestión de usuarios",
            "Problemas de autenticación",
            "Incidencias en propiedades",
            "Incidencias en operaciones comerciales",
            "Solicitudes de modificación",
            "Errores de funcionamiento");

    /** Módulos del sistema que se pueden reportar como afectados (campo 'Módulo afectado' del formulario). */
    public static final List<String> MODULOS = List.of(
            "Autenticación y usuarios",
            "Clientes",
            "Propiedades",
            "Alquileres",
            "Operaciones",
            "Comisiones",
            "Reportes y dashboard",
            "Base de datos e infraestructura",
            "Otro");

    /** Transiciones permitidas del ciclo de vida del ticket. */
    private static final Map<String, Set<String>> TRANSICIONES = Map.of(
            EstadoTicket.ABIERTO, Set.of(EstadoTicket.EN_PROCESO),
            EstadoTicket.EN_PROCESO, Set.of(EstadoTicket.RESUELTO),
            EstadoTicket.RESUELTO, Set.of(EstadoTicket.CERRADO, EstadoTicket.EN_PROCESO),
            EstadoTicket.CERRADO, Set.of());

    private final TicketRepository ticketRepository;
    private final PrioridadRepository prioridadRepository;
    private final EstadoTicketRepository estadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EquipoAsignadoRepository equipoRepository;

    public TicketService(
            TicketRepository ticketRepository,
            PrioridadRepository prioridadRepository,
            EstadoTicketRepository estadoRepository,
            UsuarioRepository usuarioRepository,
            EquipoAsignadoRepository equipoRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.prioridadRepository = prioridadRepository;
        this.estadoRepository = estadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.equipoRepository = equipoRepository;
    }

    /**
     * Matriz de priorización ITIL: puntaje de impacto (3/2/1) + urgencia (3/2/1).
     * 6 = P1 Crítico, 5 = P2 Alto, 4 = P3 Normal, 3 o menos = P4 Bajo.
     */
    public static String calcularNivel(String impacto, String urgencia) {
        int total = puntaje(impacto, "Alto", "Medio", "Bajo") + puntaje(urgencia, "Alta", "Media", "Baja");
        if (total >= 6) return "P1";
        if (total == 5) return "P2";
        if (total == 4) return "P3";
        return "P4";
    }

    private static int puntaje(String valor, String alto, String medio, String bajo) {
        if (alto.equals(valor)) return 3;
        if (medio.equals(valor)) return 2;
        if (bajo.equals(valor)) return 1;
        throw new TicketInvalidoException("Valor no válido: " + valor);
    }

    public static boolean transicionPermitida(String actual, String nuevo) {
        return TRANSICIONES.getOrDefault(actual, Set.of()).contains(nuevo);
    }

    @Transactional(rollbackFor = Exception.class)
    public TicketResponseDTO registrar(TicketCreateDTO dto, Usuario reportante) {
        if (!TIPOS.contains(dto.getTipo())) {
            throw new TicketInvalidoException("Tipo de incidencia no válido");
        }
        if (!MODULOS.contains(dto.getModuloAfectado())) {
            throw new TicketInvalidoException("Módulo afectado no válido");
        }
        // Prioridad elegida por el usuario (P1-P4); si no la envía, se usa la matriz ITIL impacto x urgencia.
        String nivel = dto.getPrioridad() != null && !dto.getPrioridad().isBlank()
                ? dto.getPrioridad()
                : calcularNivel(dto.getImpacto(), dto.getUrgencia());
        Prioridad prioridad = prioridadRepository.findByNivel(nivel)
                .orElseThrow(() -> new ResourceNotFoundException("Prioridad no configurada"));
        EstadoTicket abierto = estadoRepository.findByNombre(EstadoTicket.ABIERTO)
                .orElseThrow(() -> new ResourceNotFoundException("Estado 'Abierto' no configurado"));

        LocalDateTime ahora = LocalDateTime.now();
        Ticket ticket = new Ticket();
        ticket.setCodigo("TMP-" + UUID.randomUUID().toString().substring(0, 12));
        ticket.setTipo(dto.getTipo());
        ticket.setModuloAfectado(dto.getModuloAfectado());
        ticket.setTitulo(dto.getTitulo().trim());
        ticket.setDescripcion(dto.getDescripcion().trim());
        ticket.setImpacto(dto.getImpacto());
        ticket.setUrgencia(dto.getUrgencia());
        ticket.setPrioridad(prioridad);
        ticket.setEstado(abierto);
        ticket.setReportadoPor(reportante);
        ticket.setFechaRegistro(ahora);
        ticket.setFechaLimiteSla(ahora.plusHours(prioridad.getTiempoSlaHoras()));

        Ticket guardado = ticketRepository.saveAndFlush(ticket);
        guardado.setCodigo(String.format("INC-%05d", guardado.getId()));
        return new TicketResponseDTO(ticketRepository.save(guardado), ahora);
    }

    public List<TicketResponseDTO> listar(Usuario actual) {
        LocalDateTime ahora = LocalDateTime.now();
        List<Ticket> tickets = esAdmin(actual)
                ? ticketRepository.findAllByOrderByFechaRegistroDescIdDesc()
                : ticketRepository.findByReportadoPorIdOrderByFechaRegistroDescIdDesc(actual.getId());
        return tickets.stream().map(t -> new TicketResponseDTO(t, ahora)).toList();
    }

    public TicketResponseDTO obtener(Integer id, Usuario actual) {
        Ticket ticket = buscar(id);
        if (!esAdmin(actual) && !ticket.getReportadoPor().getId().equals(actual.getId())) {
            throw new AccessDeniedException("No puede ver tickets de otros usuarios");
        }
        return new TicketResponseDTO(ticket, LocalDateTime.now());
    }

    @Transactional(rollbackFor = Exception.class)
    public TicketResponseDTO atender(Integer id, TicketUpdateDTO dto) {
        Ticket ticket = buscar(id);
        LocalDateTime ahora = LocalDateTime.now();

        if (dto.getAsignadoAId() != null) {
            Usuario asignado = usuarioRepository.findById(dto.getAsignadoAId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario asignado no encontrado"));
            if (!asignado.isEnabled()) {
                throw new TicketInvalidoException("No se puede asignar a un usuario inactivo");
            }
            ticket.setAsignadoA(asignado);
        }
        if (dto.getEquipoAsignadoId() != null) {
            EquipoAsignado equipo = equipoRepository.findById(dto.getEquipoAsignadoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Equipo de soporte no encontrado"));
            ticket.setEquipoAsignado(equipo);
        }
        if (dto.getSolucion() != null && !dto.getSolucion().isBlank()) {
            ticket.setSolucion(dto.getSolucion().trim());
        }
        if (dto.getEstado() != null && !dto.getEstado().equals(ticket.getEstado().getNombre())) {
            cambiarEstado(ticket, dto.getEstado(), ahora);
        }
        return new TicketResponseDTO(ticketRepository.save(ticket), ahora);
    }

    private void cambiarEstado(Ticket ticket, String nuevo, LocalDateTime ahora) {
        String actual = ticket.getEstado().getNombre();
        if (!transicionPermitida(actual, nuevo)) {
            throw new TicketInvalidoException("Transición no permitida: " + actual + " → " + nuevo);
        }
        if (EstadoTicket.RESUELTO.equals(nuevo)
                && (ticket.getSolucion() == null || ticket.getSolucion().isBlank())) {
            throw new TicketInvalidoException("Para resolver el ticket debe registrar la solución aplicada");
        }
        if (EstadoTicket.EN_PROCESO.equals(nuevo) && ticket.getAsignadoA() == null && ticket.getEquipoAsignado() == null) {
            throw new TicketInvalidoException("Asigne el ticket a un responsable o a un equipo de soporte antes de ponerlo en proceso");
        }
        ticket.setEstado(estadoRepository.findByNombre(nuevo)
                .orElseThrow(() -> new ResourceNotFoundException("Estado no configurado: " + nuevo)));
        if (EstadoTicket.RESUELTO.equals(nuevo)) {
            ticket.setFechaResolucion(ahora);
        } else if (EstadoTicket.EN_PROCESO.equals(nuevo)) {
            ticket.setFechaResolucion(null); // reabierto
        }
    }

    private Ticket buscar(Integer id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado: " + id));
    }

    private boolean esAdmin(Usuario usuario) {
        return "ADMIN".equals(usuario.getRol());
    }

    public Map<String, Object> catalogo() {
        return Map.of(
                "tipos", TIPOS,
                "modulos", MODULOS,
                "equipos", equipoRepository.findAllByOrderByNivelSoporteAsc().stream()
                        .map(e -> Map.of("id", e.getId(), "nombre", e.getNombre(), "nivel", e.getNivelSoporte()))
                        .toList(),
                "impactos", List.of("Alto", "Medio", "Bajo"),
                "urgencias", List.of("Alta", "Media", "Baja"),
                "estados", estadoRepository.findAll().stream().map(EstadoTicket::getNombre).toList(),
                "prioridades", prioridadRepository.findAll().stream()
                        .map(p -> Map.of("nivel", p.getNivel(), "nombre", p.getNombre(),
                                "tiempoSlaHoras", p.getTiempoSlaHoras()))
                        .toList());
    }
}
