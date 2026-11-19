package com.inmobiliaria.backend.monitoring;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Protege los dos puntos de entrada de la observabilidad con un token Bearer compartido:
 * <ul>
 *   <li>{@code /actuator/prometheus}: Prometheus lo envía al leer las métricas.</li>
 *   <li>{@code /api/monitoreo/**}: Alertmanager lo envía al notificar una alerta.</li>
 * </ul>
 * Si no hay token configurado, ambos quedan cerrados (nunca abiertos por omisión).
 */
public class MonitoringTokenFilter extends OncePerRequestFilter {

    private final byte[] token;

    public MonitoringTokenFilter(String token) {
        this.token = token == null ? new byte[0] : token.trim().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !esRutaDeMonitoreo(request.getRequestURI());
    }

    static boolean esRutaDeMonitoreo(String uri) {
        return uri.equals("/actuator/prometheus") || uri.equals("/api/monitoreo") || uri.startsWith("/api/monitoreo/");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        if (tokenValido(request.getHeader("Authorization"))) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Token de monitoreo ausente o inválido\"}");
    }

    public boolean tokenValido(String authorization) {
        if (token.length == 0 || authorization == null || !authorization.startsWith("Bearer ")) {
            return false;
        }
        byte[] recibido = authorization.substring(7).trim().getBytes(StandardCharsets.UTF_8);
        // Comparación en tiempo constante para no filtrar información por la duración de la respuesta
        return MessageDigest.isEqual(recibido, token);
    }
}
