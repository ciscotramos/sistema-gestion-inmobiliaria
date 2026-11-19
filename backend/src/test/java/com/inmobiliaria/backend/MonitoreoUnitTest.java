package com.inmobiliaria.backend;

import com.inmobiliaria.backend.dto.TicketCreateDTO;
import com.inmobiliaria.backend.dto.TicketResponseDTO;
import com.inmobiliaria.backend.model.Usuario;
import com.inmobiliaria.backend.monitoring.AlertmanagerPayload;
import com.inmobiliaria.backend.monitoring.AlertmanagerPayload.Alerta;
import com.inmobiliaria.backend.monitoring.MonitoringTokenFilter;
import com.inmobiliaria.backend.repository.TicketRepository;
import com.inmobiliaria.backend.repository.UsuarioRepository;
import com.inmobiliaria.backend.service.AlertaTicketService;
import com.inmobiliaria.backend.service.TicketService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Monitoreo (Sesión 30): token de Prometheus/Alertmanager y conversión alerta -> ticket. Sin base de datos. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MonitoreoUnitTest {

    @Mock private TicketService ticketService;
    @Mock private TicketRepository ticketRepository;
    @Mock private UsuarioRepository usuarioRepository;

    private AlertaTicketService servicio;

    @BeforeEach
    void setUp() {
        servicio = new AlertaTicketService(ticketService, ticketRepository, usuarioRepository, new SimpleMeterRegistry());
        Usuario admin = new Usuario();
        admin.setId(1);
        admin.setRol("ADMIN");
        when(usuarioRepository.findByRol("ADMIN")).thenReturn(List.of(admin));
        when(ticketRepository.existsByTituloAndEstado_NombreIn(anyString(), anyCollection())).thenReturn(false);
        TicketResponseDTO respuesta = org.mockito.Mockito.mock(TicketResponseDTO.class);
        when(respuesta.getCodigo()).thenReturn("INC-00042");
        when(ticketService.registrar(any(TicketCreateDTO.class), any(Usuario.class))).thenReturn(respuesta);
    }

    private static Alerta alerta(String estado, Map<String, String> etiquetas) {
        return new Alerta(estado, etiquetas,
                Map.of("summary", "Más del 5% de errores 5xx", "description", "La API devuelve errores 500 desde hace 1 minuto."),
                "2026-11-19T21:00:00Z", "abc123");
    }

    private static AlertmanagerPayload payload(Alerta... alertas) {
        return new AlertmanagerPayload("firing", "ticket-incidentes", List.of(alertas));
    }

    @Test
    void alertaCritica_seConvierteEnTicketP1ConSusDatos() {
        TicketCreateDTO dto = AlertaTicketService.aTicket(alerta("firing",
                Map.of("alertname", "TasaErroresCritica5xx", "severity", "critical", "priority", "P1", "instance", "backend:8080")));

        assertEquals("P1", dto.getPrioridad());
        assertEquals("Alto", dto.getImpacto());
        assertEquals("Alta", dto.getUrgencia());
        assertEquals("Errores de funcionamiento", dto.getTipo());
        assertEquals("Base de datos e infraestructura", dto.getModuloAfectado());
        assertTrue(dto.getTitulo().startsWith("[ALERTA] TasaErroresCritica5xx"));
        assertTrue(dto.getDescripcion().contains("backend:8080"));
        // Coherente con la matriz ITIL: la prioridad calculada por impacto x urgencia coincide con la de la alerta
        assertEquals("P1", TicketService.calcularNivel(dto.getImpacto(), dto.getUrgencia()));
    }

    @Test
    void cadaPrioridadTieneUnImpactoYUrgenciaCoherentesConLaMatriz() {
        for (String p : List.of("P1", "P2", "P3", "P4")) {
            TicketCreateDTO dto = AlertaTicketService.aTicket(alerta("firing", Map.of("alertname", "X", "priority", p)));
            assertEquals(p, TicketService.calcularNivel(dto.getImpacto(), dto.getUrgencia()), "matriz para " + p);
        }
    }

    @Test
    void sinEtiquetaDePrioridad_laSeveridadDecide() {
        assertEquals("P1", AlertaTicketService.aTicket(alerta("firing", Map.of("alertname", "A", "severity", "critical"))).getPrioridad());
        assertEquals("P3", AlertaTicketService.aTicket(alerta("firing", Map.of("alertname", "A", "severity", "warning"))).getPrioridad());
        assertEquals("P3", AlertaTicketService.aTicket(alerta("firing", Map.of("alertname", "A", "priority", "P9"))).getPrioridad());
    }

    @Test
    void textoDeLaAlerta_noPuedeInyectarHtmlNiSuperarLosLimites() {
        Alerta a = new Alerta("firing", Map.of("alertname", "Mala<script>"),
                Map.of("summary", "<img src=x onerror=alert(1)>", "description", "x".repeat(5000)), null, null);
        TicketCreateDTO dto = AlertaTicketService.aTicket(a);
        assertFalse(dto.getTitulo().contains("<") || dto.getTitulo().contains(">"));
        assertFalse(dto.getDescripcion().contains("<"));
        assertTrue(dto.getTitulo().length() <= 150);
        assertTrue(dto.getDescripcion().length() <= 2000);
        assertTrue(dto.getTitulo().length() >= 5 && dto.getDescripcion().length() >= 10);
    }

    @Test
    void alertaActiva_creaUnTicket_yLaResueltaNo() {
        AlertaTicketService.Resultado r = servicio.procesar(payload(
                alerta("firing", Map.of("alertname", "TasaErroresCritica5xx", "priority", "P1")),
                alerta("resolved", Map.of("alertname", "OtraAlerta", "priority", "P2"))));

        assertEquals(2, r.recibidas());
        assertEquals(1, r.resueltas());
        assertEquals(List.of("INC-00042"), r.ticketsCreados());
    }

    @Test
    void alertaQueSigueActiva_noDuplicaElTicketAbierto() {
        when(ticketRepository.existsByTituloAndEstado_NombreIn(anyString(), anyCollection())).thenReturn(true);

        AlertaTicketService.Resultado r = servicio.procesar(payload(alerta("firing", Map.of("alertname", "TasaErroresCritica5xx"))));

        assertEquals(1, r.duplicadas());
        assertTrue(r.ticketsCreados().isEmpty());
        verify(ticketService, never()).registrar(any(), any());
    }

    @Test
    void payloadVacioONulo_noFalla() {
        assertEquals(0, servicio.procesar(null).recibidas());
        assertEquals(0, servicio.procesar(new AlertmanagerPayload("firing", "x", null)).recibidas());
    }

    @Test
    void filtro_soloAceptaElTokenCorrecto() {
        MonitoringTokenFilter filtro = new MonitoringTokenFilter("secreto-largo");
        assertTrue(filtro.tokenValido("Bearer secreto-largo"));
        assertFalse(filtro.tokenValido("Bearer otro"));
        assertFalse(filtro.tokenValido("secreto-largo"));
        assertFalse(filtro.tokenValido(null));
    }

    @Test
    void filtro_sinTokenConfigurado_cierraLasRutas() {
        assertFalse(new MonitoringTokenFilter("").tokenValido("Bearer "));
        assertFalse(new MonitoringTokenFilter(null).tokenValido("Bearer cualquiera"));
    }

    @Test
    void filtro_respondeConUnauthorizedYSoloActuaEnSusRutas() throws Exception {
        MonitoringTokenFilter filtro = new MonitoringTokenFilter("secreto-largo");

        MockHttpServletRequest sinToken = new MockHttpServletRequest("GET", "/actuator/prometheus");
        sinToken.setRequestURI("/actuator/prometheus");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        MockFilterChain cadena = new MockFilterChain();
        filtro.doFilter(sinToken, resp, cadena);
        assertEquals(401, resp.getStatus());
        assertNull(cadena.getRequest(), "no debe continuar la cadena sin token");

        MockHttpServletRequest conToken = new MockHttpServletRequest("POST", "/api/monitoreo/alertas");
        conToken.setRequestURI("/api/monitoreo/alertas");
        conToken.addHeader("Authorization", "Bearer secreto-largo");
        MockHttpServletResponse resp2 = new MockHttpServletResponse();
        MockFilterChain cadena2 = new MockFilterChain();
        filtro.doFilter(conToken, resp2, cadena2);
        assertEquals(200, resp2.getStatus());
        assertNotNull(cadena2.getRequest());

        MockHttpServletRequest otraRuta = new MockHttpServletRequest("GET", "/api/propiedades");
        otraRuta.setRequestURI("/api/propiedades");
        MockFilterChain cadena3 = new MockFilterChain();
        filtro.doFilter(otraRuta, new MockHttpServletResponse(), cadena3);
        assertNotNull(cadena3.getRequest(), "las demás rutas no pasan por este filtro");
    }
}
