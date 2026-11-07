package com.inmobiliaria.backend.controller;

import com.inmobiliaria.backend.dto.AlquilerCreateDTO;
import com.inmobiliaria.backend.dto.OperacionResponseDTO;
import com.inmobiliaria.backend.dto.VentaCreateDTO;
import com.inmobiliaria.backend.model.Usuario;
import com.inmobiliaria.backend.service.OperacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/operaciones")
@Tag(name = "Operaciones comerciales", description = "Registro de ventas y alquileres con bloqueo pesimista")
public class OperacionController {

    private final OperacionService operacionService;

    public OperacionController(OperacionService operacionService) {
        this.operacionService = operacionService;
    }

    @PostMapping("/ventas")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    @Operation(summary = "Registrar una venta (409 si la propiedad ya no está disponible)")
    public ResponseEntity<OperacionResponseDTO> registrarVenta(
            @Valid @RequestBody VentaCreateDTO dto,
            @AuthenticationPrincipal Usuario usuarioActual
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(operacionService.registrarVenta(dto, usuarioActual));
    }

    @PostMapping("/alquileres")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    @Operation(summary = "Registrar un alquiler (409 si la propiedad ya no está disponible)")
    public ResponseEntity<OperacionResponseDTO> registrarAlquiler(
            @Valid @RequestBody AlquilerCreateDTO dto,
            @AuthenticationPrincipal Usuario usuarioActual
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(operacionService.registrarAlquiler(dto, usuarioActual));
    }
}
