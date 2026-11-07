package com.inmobiliaria.backend.controller;

import com.inmobiliaria.backend.dto.DistritoCreateDTO;
import com.inmobiliaria.backend.dto.DistritoResponseDTO;
import com.inmobiliaria.backend.service.UbicacionDistritoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/distritos")
public class UbicacionDistritoController {

    private final UbicacionDistritoService distritoService;

    public UbicacionDistritoController(UbicacionDistritoService distritoService) {
        this.distritoService = distritoService;
    }

    @GetMapping
    public ResponseEntity<List<DistritoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(distritoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DistritoResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(distritoService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DistritoResponseDTO> crear(@Valid @RequestBody DistritoCreateDTO dto) {
        DistritoResponseDTO creado = distritoService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}
