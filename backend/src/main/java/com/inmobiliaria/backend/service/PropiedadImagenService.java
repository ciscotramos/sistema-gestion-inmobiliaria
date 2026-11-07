package com.inmobiliaria.backend.service;

import com.inmobiliaria.backend.dto.PropiedadImagenCreateDTO;
import com.inmobiliaria.backend.dto.PropiedadImagenResponseDTO;
import com.inmobiliaria.backend.exception.ResourceNotFoundException;
import com.inmobiliaria.backend.model.Propiedad;
import com.inmobiliaria.backend.model.PropiedadImagen;
import com.inmobiliaria.backend.model.Usuario;
import com.inmobiliaria.backend.repository.PropiedadImagenRepository;
import com.inmobiliaria.backend.repository.PropiedadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PropiedadImagenService {

    private final PropiedadRepository propiedadRepository;
    private final PropiedadImagenRepository imagenRepository;
    private final PropiedadService propiedadService;

    public PropiedadImagenService(
            PropiedadRepository propiedadRepository,
            PropiedadImagenRepository imagenRepository,
            PropiedadService propiedadService
    ) {
        this.propiedadRepository = propiedadRepository;
        this.imagenRepository = imagenRepository;
        this.propiedadService = propiedadService;
    }

    public List<PropiedadImagenResponseDTO> listarImagenes(Integer propiedadId) {
        if (!propiedadRepository.existsById(propiedadId)) {
            throw new ResourceNotFoundException("Propiedad no encontrada con ID: " + propiedadId);
        }
        return imagenRepository.findByPropiedadIdOrderByOrdenAscIdAsc(propiedadId).stream()
                .map(PropiedadImagenResponseDTO::new)
                .toList();
    }

    @Transactional
    public PropiedadImagenResponseDTO agregarImagen(Integer propiedadId, PropiedadImagenCreateDTO dto, Usuario usuarioActual) {
        Propiedad propiedad = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada con ID: " + propiedadId));

        propiedadService.validarPropiedadDeAgente(propiedad, usuarioActual);

        boolean esPrincipal = Boolean.TRUE.equals(dto.getEsPrincipal());
        if (esPrincipal) {
            // Desmarcar otras imágenes como principal
            List<PropiedadImagen> existentes = imagenRepository.findByPropiedadIdOrderByOrdenAscIdAsc(propiedadId);
            for (PropiedadImagen img : existentes) {
                if (Boolean.TRUE.equals(img.getEsPrincipal())) {
                    img.setEsPrincipal(false);
                    imagenRepository.save(img);
                }
            }
        }

        PropiedadImagen nuevaImagen = new PropiedadImagen(
                propiedad,
                dto.getUrl().trim(),
                dto.getOrden() != null ? dto.getOrden() : 0,
                esPrincipal
        );

        PropiedadImagen guardada = imagenRepository.save(nuevaImagen);
        return new PropiedadImagenResponseDTO(guardada);
    }

    @Transactional
    public void eliminarImagen(Integer propiedadId, Integer imagenId, Usuario usuarioActual) {
        Propiedad propiedad = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada con ID: " + propiedadId));

        propiedadService.validarPropiedadDeAgente(propiedad, usuarioActual);

        PropiedadImagen imagen = imagenRepository.findByPropiedadIdAndId(propiedadId, imagenId)
                .orElseThrow(() -> new ResourceNotFoundException("Imagen no encontrada con ID: " + imagenId + " para la propiedad: " + propiedadId));

        imagenRepository.delete(imagen);
    }

    @Transactional
    public PropiedadImagenResponseDTO marcarPrincipal(Integer propiedadId, Integer imagenId, Usuario usuarioActual) {
        Propiedad propiedad = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada con ID: " + propiedadId));

        propiedadService.validarPropiedadDeAgente(propiedad, usuarioActual);

        PropiedadImagen objetivo = imagenRepository.findByPropiedadIdAndId(propiedadId, imagenId)
                .orElseThrow(() -> new ResourceNotFoundException("Imagen no encontrada con ID: " + imagenId + " para la propiedad: " + propiedadId));

        List<PropiedadImagen> todas = imagenRepository.findByPropiedadIdOrderByOrdenAscIdAsc(propiedadId);
        for (PropiedadImagen img : todas) {
            if (!img.getId().equals(imagenId) && Boolean.TRUE.equals(img.getEsPrincipal())) {
                img.setEsPrincipal(false);
                imagenRepository.save(img);
            }
        }

        objetivo.setEsPrincipal(true);
        PropiedadImagen guardada = imagenRepository.save(objetivo);
        return new PropiedadImagenResponseDTO(guardada);
    }
}
