package com.inmobiliaria.backend.controller;

import com.inmobiliaria.backend.dto.*;
import com.inmobiliaria.backend.model.Usuario;
import com.inmobiliaria.backend.service.PropiedadImagenService;
import com.inmobiliaria.backend.service.PropiedadService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/propiedades")
public class PropiedadController {

    private final PropiedadService propiedadService;
    private final PropiedadImagenService propiedadImagenService;

    public PropiedadController(
            PropiedadService propiedadService,
            PropiedadImagenService propiedadImagenService
    ) {
        this.propiedadService = propiedadService;
        this.propiedadImagenService = propiedadImagenService;
    }

    @GetMapping
    public ResponseEntity<Page<PropiedadResponseDTO>> listar(
            @RequestParam(required = false) String distrito,
            @RequestParam(required = false) String operacion,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer agenteId,
            @RequestParam(required = false) Boolean destacada,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = "asc".equalsIgnoreCase(sortDir) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), sort);

        Page<PropiedadResponseDTO> resultado = propiedadService.listarPropiedades(
                distrito, operacion, tipo, precioMin, precioMax, estado, agenteId, destacada, activo, search, pageable
        );
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropiedadResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(propiedadService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<PropiedadResponseDTO> crear(
            @Valid @RequestBody PropiedadCreateDTO dto,
            @AuthenticationPrincipal Usuario usuarioActual
    ) {
        PropiedadResponseDTO creada = propiedadService.crearPropiedad(dto, usuarioActual);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<PropiedadResponseDTO> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody PropiedadUpdateDTO dto,
            @AuthenticationPrincipal Usuario usuarioActual
    ) {
        PropiedadResponseDTO actualizada = propiedadService.actualizarPropiedad(id, dto, usuarioActual);
        return ResponseEntity.ok(actualizada);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<PropiedadResponseDTO> cambiarEstado(
            @PathVariable Integer id,
            @Valid @RequestBody PropiedadEstadoDTO dto,
            @AuthenticationPrincipal Usuario usuarioActual
    ) {
        PropiedadResponseDTO actualizada = propiedadService.cambiarEstado(id, dto.getEstado(), usuarioActual);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactivar(
            @PathVariable Integer id,
            @AuthenticationPrincipal Usuario usuarioActual
    ) {
        propiedadService.desactivarPropiedad(id, usuarioActual);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // ENDPOINTS DE IMÁGENES
    // ==========================================

    @GetMapping("/{propiedadId}/imagenes")
    public ResponseEntity<List<PropiedadImagenResponseDTO>> listarImagenes(@PathVariable Integer propiedadId) {
        return ResponseEntity.ok(propiedadImagenService.listarImagenes(propiedadId));
    }

    @PostMapping("/{propiedadId}/imagenes")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<PropiedadImagenResponseDTO> agregarImagen(
            @PathVariable Integer propiedadId,
            @Valid @RequestBody PropiedadImagenCreateDTO dto,
            @AuthenticationPrincipal Usuario usuarioActual
    ) {
        PropiedadImagenResponseDTO guardada = propiedadImagenService.agregarImagen(propiedadId, dto, usuarioActual);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    @DeleteMapping("/{propiedadId}/imagenes/{imagenId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<Void> eliminarImagen(
            @PathVariable Integer propiedadId,
            @PathVariable Integer imagenId,
            @AuthenticationPrincipal Usuario usuarioActual
    ) {
        propiedadImagenService.eliminarImagen(propiedadId, imagenId, usuarioActual);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{propiedadId}/imagenes/{imagenId}/principal")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<PropiedadImagenResponseDTO> marcarPrincipal(
            @PathVariable Integer propiedadId,
            @PathVariable Integer imagenId,
            @AuthenticationPrincipal Usuario usuarioActual
    ) {
        PropiedadImagenResponseDTO actualizada = propiedadImagenService.marcarPrincipal(propiedadId, imagenId, usuarioActual);
        return ResponseEntity.ok(actualizada);
    }
}
