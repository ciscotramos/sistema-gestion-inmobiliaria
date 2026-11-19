package com.inmobiliaria.backend.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Simulador de incidentes para el Reto de la Ficha 04: provoca errores 500 o lentitud controlada
 * sin tener que detener MySQL. Está APAGADO por defecto (MONITORING_SIMULATION=false responde 404)
 * y, aun encendido, exige el token de monitoreo (MonitoringTokenFilter). Nunca activarlo en producción.
 */
@Hidden
@RestController
@RequestMapping("/api/monitoreo")
public class SimulacionController {

    static final long MAX_DEMORA_MS = 5000;

    private final boolean habilitado;

    public SimulacionController(@Value("${app.monitoring.simulation-enabled:false}") boolean habilitado) {
        this.habilitado = habilitado;
    }

    @PostMapping("/simular")
    public ResponseEntity<Map<String, Object>> simular(@RequestParam String tipo,
                                                       @RequestParam(defaultValue = "1500") long ms) throws InterruptedException {
        if (!habilitado) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return switch (tipo) {
            case "error500" -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("simulado", true, "tipo", tipo));
            case "lento" -> {
                Thread.sleep(Math.max(0, Math.min(ms, MAX_DEMORA_MS)));
                yield ResponseEntity.ok(Map.of("simulado", true, "tipo", tipo, "demoraMs", Math.min(ms, MAX_DEMORA_MS)));
            }
            default -> ResponseEntity.badRequest().body(Map.of("error", "tipo debe ser error500 o lento"));
        };
    }
}
