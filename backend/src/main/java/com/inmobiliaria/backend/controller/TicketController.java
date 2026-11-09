package com.inmobiliaria.backend.controller;

import com.inmobiliaria.backend.dto.TicketCreateDTO;
import com.inmobiliaria.backend.dto.TicketResponseDTO;
import com.inmobiliaria.backend.dto.TicketUpdateDTO;
import com.inmobiliaria.backend.model.Usuario;
import com.inmobiliaria.backend.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Incidentes", description = "Reporte, priorización (ITIL) y seguimiento de tickets con SLA")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/catalogo")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    @Operation(summary = "Tipos, impactos, urgencias, estados y prioridades con su SLA")
    public Map<String, Object> catalogo() {
        return ticketService.catalogo();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    @Operation(summary = "Reportar un incidente (la prioridad y el límite SLA los calcula el servidor)")
    public ResponseEntity<TicketResponseDTO> registrar(
            @Valid @RequestBody TicketCreateDTO dto,
            @AuthenticationPrincipal Usuario usuarioActual
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.registrar(dto, usuarioActual));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    @Operation(summary = "Listar tickets (el administrador ve todos; el agente solo los suyos)")
    public List<TicketResponseDTO> listar(@AuthenticationPrincipal Usuario usuarioActual) {
        return ticketService.listar(usuarioActual);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    @Operation(summary = "Detalle de un ticket")
    public TicketResponseDTO obtener(@PathVariable Integer id, @AuthenticationPrincipal Usuario usuarioActual) {
        return ticketService.obtener(id, usuarioActual);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atender un ticket: asignar, cambiar de estado y registrar la solución")
    public TicketResponseDTO atender(@PathVariable Integer id, @Valid @RequestBody TicketUpdateDTO dto) {
        return ticketService.atender(id, dto);
    }
}
