package com.inmobiliaria.backend.service;

import com.inmobiliaria.backend.dto.TicketCreateDTO;
import com.inmobiliaria.backend.dto.TicketResponseDTO;
import com.inmobiliaria.backend.exception.ResourceNotFoundException;
import com.inmobiliaria.backend.model.EstadoTicket;
import com.inmobiliaria.backend.model.Usuario;
import com.inmobiliaria.backend.monitoring.AlertmanagerPayload;
import com.inmobiliaria.backend.monitoring.AlertmanagerPayload.Alerta;
import com.inmobiliaria.backend.repository.TicketRepository;
import com.inmobiliaria.backend.repository.UsuarioRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Convierte las alertas de Prometheus/Alertmanager en tickets del módulo de incidentes
 * (Taller Sesión 30, Ficha 04): alerta crítica -> ticket P1 con SLA de 2 h.
 * Una alerta que sigue activa no genera un segundo ticket mientras el primero siga abierto.
 */
@Service
public class AlertaTicketService {

    private static final Logger log = LoggerFactory.getLogger(AlertaTicketService.class);

    /** Evita que un payload enorme cree cientos de tickets de golpe. */
    static final int MAX_ALERTAS_POR_ENVIO = 20;

    /** Impacto y urgencia coherentes con la matriz ITIL para cada prioridad (docs/gestion-incidentes). */
    private static final Map<String, String[]> IMPACTO_URGENCIA = Map.of(
            "P1", new String[]{"Alto", "Alta"},
            "P2", new String[]{"Alto", "Media"},
            "P3", new String[]{"Medio", "Media"},
            "P4", new String[]{"Medio", "Baja"});

    private final TicketService ticketService;
    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;
    private final MeterRegistry meterRegistry;

    public AlertaTicketService(TicketService ticketService,
                               TicketRepository ticketRepository,
                               UsuarioRepository usuarioRepository,
                               MeterRegistry meterRegistry) {
        this.ticketService = ticketService;
        this.ticketRepository = ticketRepository;
        this.usuarioRepository = usuarioRepository;
        this.meterRegistry = meterRegistry;
    }

    /** Resultado del procesamiento, útil para la respuesta HTTP y para la evidencia del taller. */
    public record Resultado(int recibidas, int resueltas, int duplicadas, List<String> ticketsCreados) {}

    public Resultado procesar(AlertmanagerPayload payload) {
        List<Alerta> alertas = payload == null || payload.alerts() == null ? List.of() : payload.alerts();
        List<String> creados = new ArrayList<>();
        int resueltas = 0;
        int duplicadas = 0;
        Usuario reportante = null;

        for (Alerta alerta : alertas.stream().limit(MAX_ALERTAS_POR_ENVIO).toList()) {
            if (!"firing".equalsIgnoreCase(alerta.status())) {
                resueltas++; // el cierre del ticket lo confirma una persona (ciclo de vida ITIL)
                continue;
            }
            TicketCreateDTO dto = aTicket(alerta);
            if (ticketRepository.existsByTituloAndEstado_NombreIn(
                    dto.getTitulo(), List.of(EstadoTicket.ABIERTO, EstadoTicket.EN_PROCESO))) {
                duplicadas++;
                continue;
            }
            if (reportante == null) {
                reportante = usuarioRepository.findByRol("ADMIN").stream().findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("No hay un administrador que figure como reportante"));
            }
            TicketResponseDTO ticket = ticketService.registrar(dto, reportante);
            creados.add(ticket.getCodigo());
            log.warn("Alerta {} convertida en ticket {} ({})", alerta.etiqueta("alertname"), ticket.getCodigo(), dto.getPrioridad());
        }
        meterRegistry.counter("inmobiliaria_tickets_por_alerta_total").increment(creados.size());
        return new Resultado(alertas.size(), resueltas, duplicadas, creados);
    }

    /** Traduce una alerta de Prometheus al formulario de incidentes. Público para poder probarlo sin base de datos. */
    public static TicketCreateDTO aTicket(Alerta alerta) {
        String nivel = alerta.etiqueta("priority");
        if (nivel == null || !IMPACTO_URGENCIA.containsKey(nivel)) {
            // Sin etiqueta "priority" válida: crítica = P1, cualquier otra severidad = P3
            nivel = "critical".equalsIgnoreCase(alerta.etiqueta("severity")) ? "P1" : "P3";
        }
        String nombre = limpiar(alerta.etiqueta("alertname"), "AlertaSinNombre");
        String resumen = limpiar(alerta.anotacion("summary"), "Alerta automática de monitoreo");
        String detalle = limpiar(alerta.anotacion("description"), "Sin descripción en la regla de alerta.");
        String modulo = alerta.etiqueta("modulo");

        TicketCreateDTO dto = new TicketCreateDTO();
        dto.setTipo("Errores de funcionamiento");
        dto.setModuloAfectado(modulo != null && TicketService.MODULOS.contains(modulo) ? modulo : "Base de datos e infraestructura");
        dto.setTitulo(recortar("[ALERTA] " + nombre + ": " + resumen, 150));
        dto.setDescripcion(recortar(detalle
                + " | Origen: Prometheus/Alertmanager"
                + " | Severidad: " + limpiar(alerta.etiqueta("severity"), "n/d")
                + " | Instancia: " + limpiar(alerta.etiqueta("instance"), "n/d")
                + " | Inicio de la alerta: " + limpiar(alerta.startsAt(), "n/d"), 2000));
        dto.setImpacto(IMPACTO_URGENCIA.get(nivel)[0]);
        dto.setUrgencia(IMPACTO_URGENCIA.get(nivel)[1]);
        dto.setPrioridad(nivel);
        return dto;
    }

    /** Quita < y > (el formulario rechaza HTML) y espacios sobrantes. */
    private static String limpiar(String texto, String porDefecto) {
        if (texto == null || texto.isBlank()) {
            return porDefecto;
        }
        return texto.replace("<", "").replace(">", "").replaceAll("\\s+", " ").trim();
    }

    private static String recortar(String texto, int max) {
        return texto.length() <= max ? texto : texto.substring(0, max - 1) + "…";
    }
}
