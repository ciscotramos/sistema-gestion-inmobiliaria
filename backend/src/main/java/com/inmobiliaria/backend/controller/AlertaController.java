package com.inmobiliaria.backend.controller;

import com.inmobiliaria.backend.monitoring.AlertmanagerPayload;
import com.inmobiliaria.backend.service.AlertaTicketService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Receptor webhook de Alertmanager. Lo protege MonitoringTokenFilter (token Bearer),
 * por lo que no es una ruta para usuarios del sistema y se oculta de Swagger.
 */
@Hidden
@RestController
@RequestMapping("/api/monitoreo")
public class AlertaController {

    private final AlertaTicketService alertaTicketService;

    public AlertaController(AlertaTicketService alertaTicketService) {
        this.alertaTicketService = alertaTicketService;
    }

    @PostMapping("/alertas")
    public ResponseEntity<AlertaTicketService.Resultado> recibir(@RequestBody AlertmanagerPayload payload) {
        return ResponseEntity.ok(alertaTicketService.procesar(payload));
    }
}
