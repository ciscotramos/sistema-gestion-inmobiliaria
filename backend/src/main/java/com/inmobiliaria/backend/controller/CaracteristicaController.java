package com.inmobiliaria.backend.controller;

import com.inmobiliaria.backend.dto.CaracteristicaCreateDTO;
import com.inmobiliaria.backend.dto.CaracteristicaResponseDTO;
import com.inmobiliaria.backend.service.CaracteristicaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/caracteristicas")
public class CaracteristicaController {

    private final CaracteristicaService caracteristicaService;

    public CaracteristicaController(CaracteristicaService caracteristicaService) {
        this.caracteristicaService = caracteristicaService;
    }

    @GetMapping
    public ResponseEntity<List<CaracteristicaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(caracteristicaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CaracteristicaResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(caracteristicaService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CaracteristicaResponseDTO> crear(@Valid @RequestBody CaracteristicaCreateDTO dto) {
        CaracteristicaResponseDTO creada = caracteristicaService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        caracteristicaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
