package com.inmobiliaria.backend.service;

import com.inmobiliaria.backend.dto.DistritoCreateDTO;
import com.inmobiliaria.backend.dto.DistritoResponseDTO;
import com.inmobiliaria.backend.exception.DuplicateResourceException;
import com.inmobiliaria.backend.exception.ResourceNotFoundException;
import com.inmobiliaria.backend.model.UbicacionDistrito;
import com.inmobiliaria.backend.repository.UbicacionDistritoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UbicacionDistritoService {

    private final UbicacionDistritoRepository distritoRepository;

    public UbicacionDistritoService(UbicacionDistritoRepository distritoRepository) {
        this.distritoRepository = distritoRepository;
    }

    @Transactional(readOnly = true)
    public List<DistritoResponseDTO> listarTodos() {
        return distritoRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(DistritoResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public DistritoResponseDTO obtenerPorId(Integer id) {
        UbicacionDistrito distrito = distritoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Distrito no encontrado con ID: " + id));
        return new DistritoResponseDTO(distrito);
    }

    @Transactional
    public DistritoResponseDTO crear(DistritoCreateDTO dto) {
        String nombre = dto.getNombre().trim();
        String provincia = (dto.getProvincia() != null && !dto.getProvincia().isBlank()) ? dto.getProvincia().trim() : "Lima";
        String departamento = (dto.getDepartamento() != null && !dto.getDepartamento().isBlank()) ? dto.getDepartamento().trim() : "Lima";

        if (distritoRepository.existsByNombreIgnoreCaseAndProvinciaIgnoreCaseAndDepartamentoIgnoreCase(nombre, provincia, departamento)) {
            throw new DuplicateResourceException(
                    String.format("El distrito '%s' ya se encuentra registrado para %s, %s", nombre, provincia, departamento)
            );
        }

        UbicacionDistrito nuevoDistrito = new UbicacionDistrito(nombre, provincia, departamento);
        UbicacionDistrito guardado = distritoRepository.save(nuevoDistrito);

        return new DistritoResponseDTO(guardado);
    }
}
