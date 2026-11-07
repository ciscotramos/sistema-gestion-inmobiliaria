package com.inmobiliaria.backend.service;

import com.inmobiliaria.backend.dto.CaracteristicaCreateDTO;
import com.inmobiliaria.backend.dto.CaracteristicaResponseDTO;
import com.inmobiliaria.backend.exception.DuplicateResourceException;
import com.inmobiliaria.backend.exception.ResourceNotFoundException;
import com.inmobiliaria.backend.model.Caracteristica;
import com.inmobiliaria.backend.repository.CaracteristicaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CaracteristicaService {

    private final CaracteristicaRepository caracteristicaRepository;

    public CaracteristicaService(CaracteristicaRepository caracteristicaRepository) {
        this.caracteristicaRepository = caracteristicaRepository;
    }

    @Transactional(readOnly = true)
    public List<CaracteristicaResponseDTO> listarTodas() {
        return caracteristicaRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(CaracteristicaResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public CaracteristicaResponseDTO obtenerPorId(Integer id) {
        Caracteristica caracteristica = caracteristicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Característica no encontrada con ID: " + id));
        return new CaracteristicaResponseDTO(caracteristica);
    }

    @Transactional
    public CaracteristicaResponseDTO crear(CaracteristicaCreateDTO dto) {
        String nombre = dto.getNombre().trim();
        String categoria = (dto.getCategoria() != null && !dto.getCategoria().isBlank()) ? dto.getCategoria().trim() : "General";

        if (caracteristicaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new DuplicateResourceException(
                    String.format("La característica '%s' ya se encuentra registrada", nombre)
            );
        }

        Caracteristica nueva = new Caracteristica(nombre, categoria);
        Caracteristica guardada = caracteristicaRepository.save(nueva);

        return new CaracteristicaResponseDTO(guardada);
    }

    @Transactional
    public void eliminar(Integer id) {
        Caracteristica caracteristica = caracteristicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Característica no encontrada con ID: " + id));
        caracteristicaRepository.delete(caracteristica);
    }
}
