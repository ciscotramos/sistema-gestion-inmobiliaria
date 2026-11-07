package com.inmobiliaria.backend.service;

import com.inmobiliaria.backend.dto.*;
import com.inmobiliaria.backend.exception.ResourceNotFoundException;
import com.inmobiliaria.backend.model.*;
import com.inmobiliaria.backend.repository.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class PropiedadService {

    private final PropiedadRepository propiedadRepository;
    private final UbicacionDistritoRepository distritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CaracteristicaRepository caracteristicaRepository;
    private final PropiedadImagenRepository imagenRepository;

    public PropiedadService(
            PropiedadRepository propiedadRepository,
            UbicacionDistritoRepository distritoRepository,
            UsuarioRepository usuarioRepository,
            CaracteristicaRepository caracteristicaRepository,
            PropiedadImagenRepository imagenRepository
    ) {
        this.propiedadRepository = propiedadRepository;
        this.distritoRepository = distritoRepository;
        this.usuarioRepository = usuarioRepository;
        this.caracteristicaRepository = caracteristicaRepository;
        this.imagenRepository = imagenRepository;
    }

    public Page<PropiedadResponseDTO> listarPropiedades(
            String distrito,
            String operacion,
            String tipo,
            BigDecimal precioMin,
            BigDecimal precioMax,
            String estado,
            Integer agenteId,
            Boolean destacada,
            Boolean activo,
            String search,
            Pageable pageable
    ) {
        Specification<Propiedad> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Por defecto, solo propiedades activas a menos que se especifique lo contrario
            if (activo != null) {
                predicates.add(cb.equal(root.get("activo"), activo));
            } else {
                predicates.add(cb.equal(root.get("activo"), true));
            }

            if (distrito != null && !distrito.isBlank()) {
                Join<Propiedad, UbicacionDistrito> distJoin = root.join("distrito", JoinType.INNER);
                predicates.add(cb.equal(cb.lower(distJoin.get("nombre")), distrito.trim().toLowerCase()));
            }

            if (operacion != null && !operacion.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("operacion")), operacion.trim().toLowerCase()));
            }

            if (tipo != null && !tipo.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("tipo")), tipo.trim().toLowerCase()));
            }

            if (precioMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("precio"), precioMin));
            }

            if (precioMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("precio"), precioMax));
            }

            if (estado != null && !estado.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("estado")), estado.trim().toLowerCase()));
            }

            if (agenteId != null) {
                predicates.add(cb.equal(root.get("agente").get("id"), agenteId));
            }

            if (destacada != null) {
                predicates.add(cb.equal(root.get("destacada"), destacada));
            }

            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase() + "%";
                Predicate tituloPred = cb.like(cb.lower(root.get("titulo")), term);
                Predicate descPred = cb.like(cb.lower(root.get("descripcion")), term);
                Predicate dirPred = cb.like(cb.lower(root.get("direccion")), term);
                predicates.add(cb.or(tituloPred, descPred, dirPred));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return propiedadRepository.findAll(spec, pageable).map(PropiedadResponseDTO::new);
    }

    public PropiedadResponseDTO obtenerPorId(Integer id) {
        Propiedad propiedad = propiedadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada con ID: " + id));

        return new PropiedadResponseDTO(propiedad);
    }

    @Transactional
    public PropiedadResponseDTO crearPropiedad(PropiedadCreateDTO dto, Usuario usuarioActual) {
        UbicacionDistrito distrito = distritoRepository.findById(dto.getDistritoId())
                .orElseThrow(() -> new ResourceNotFoundException("Distrito no encontrado con ID: " + dto.getDistritoId()));

        Usuario agente;
        if ("ADMIN".equals(usuarioActual.getRol())) {
            if (dto.getAgenteId() != null) {
                agente = usuarioRepository.findById(dto.getAgenteId())
                        .orElseThrow(() -> new ResourceNotFoundException("Agente no encontrado con ID: " + dto.getAgenteId()));
            } else {
                agente = usuarioActual;
            }
        } else {
            // Un AGENTE siempre crea propiedades asignadas a sí mismo
            agente = usuarioActual;
        }

        Propiedad propiedad = new Propiedad(
                dto.getTitulo().trim(),
                dto.getDescripcion() != null ? dto.getDescripcion().trim() : null,
                dto.getOperacion(),
                dto.getTipo(),
                dto.getPrecio(),
                dto.getMoneda(),
                dto.getDormitorios(),
                dto.getBanos(),
                dto.getAreaConstruida(),
                dto.getAreaTotal(),
                dto.getDireccion() != null ? dto.getDireccion().trim() : null,
                distrito,
                agente,
                dto.getEstado() != null ? dto.getEstado() : "Disponible",
                dto.getDestacada() != null ? dto.getDestacada() : false,
                dto.getActivo() != null ? dto.getActivo() : true
        );

        if (dto.getCaracteristicasIds() != null && !dto.getCaracteristicasIds().isEmpty()) {
            List<Caracteristica> caracs = caracteristicaRepository.findAllById(dto.getCaracteristicasIds());
            propiedad.setCaracteristicas(new HashSet<>(caracs));
        }

        Propiedad guardada = propiedadRepository.save(propiedad);

        if (dto.getImagenes() != null && !dto.getImagenes().isEmpty()) {
            List<PropiedadImagen> listaImagenes = new ArrayList<>();
            for (int i = 0; i < dto.getImagenes().size(); i++) {
                String url = dto.getImagenes().get(i);
                if (url != null && !url.isBlank()) {
                    boolean esPrincipal = (i == 0);
                    PropiedadImagen img = new PropiedadImagen(guardada, url.trim(), i, esPrincipal);
                    listaImagenes.add(imagenRepository.save(img));
                }
            }
            guardada.setImagenes(listaImagenes);
        }

        return new PropiedadResponseDTO(guardada);
    }

    @Transactional
    public PropiedadResponseDTO actualizarPropiedad(Integer id, PropiedadUpdateDTO dto, Usuario usuarioActual) {
        Propiedad propiedad = propiedadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada con ID: " + id));

        // Validación de permisos: AGENTE solo puede modificar sus propiedades
        validarPropiedadDeAgente(propiedad, usuarioActual);

        if (dto.getTitulo() != null) propiedad.setTitulo(dto.getTitulo().trim());
        if (dto.getDescripcion() != null) propiedad.setDescripcion(dto.getDescripcion().trim());
        if (dto.getOperacion() != null) propiedad.setOperacion(dto.getOperacion());
        if (dto.getTipo() != null) propiedad.setTipo(dto.getTipo());
        if (dto.getPrecio() != null) propiedad.setPrecio(dto.getPrecio());
        if (dto.getMoneda() != null) propiedad.setMoneda(dto.getMoneda());
        if (dto.getDormitorios() != null) propiedad.setDormitorios(dto.getDormitorios());
        if (dto.getBanos() != null) propiedad.setBanos(dto.getBanos());
        if (dto.getAreaConstruida() != null) propiedad.setAreaConstruida(dto.getAreaConstruida());
        if (dto.getAreaTotal() != null) propiedad.setAreaTotal(dto.getAreaTotal());
        if (dto.getDireccion() != null) propiedad.setDireccion(dto.getDireccion().trim());
        if (dto.getEstado() != null) propiedad.setEstado(dto.getEstado());
        if (dto.getDestacada() != null) propiedad.setDestacada(dto.getDestacada());
        if (dto.getActivo() != null) propiedad.setActivo(dto.getActivo());

        if (dto.getDistritoId() != null) {
            UbicacionDistrito distrito = distritoRepository.findById(dto.getDistritoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Distrito no encontrado con ID: " + dto.getDistritoId()));
            propiedad.setDistrito(distrito);
        }

        // Solo ADMIN puede reasignar el agente responsable
        if ("ADMIN".equals(usuarioActual.getRol()) && dto.getAgenteId() != null) {
            Usuario nuevoAgente = usuarioRepository.findById(dto.getAgenteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Agente no encontrado con ID: " + dto.getAgenteId()));
            propiedad.setAgente(nuevoAgente);
        }

        if (dto.getCaracteristicasIds() != null) {
            List<Caracteristica> caracs = caracteristicaRepository.findAllById(dto.getCaracteristicasIds());
            propiedad.setCaracteristicas(new HashSet<>(caracs));
        }

        Propiedad actualizada = propiedadRepository.save(propiedad);
        return new PropiedadResponseDTO(actualizada);
    }

    @Transactional
    public PropiedadResponseDTO cambiarEstado(Integer id, String nuevoEstado, Usuario usuarioActual) {
        Propiedad propiedad = propiedadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada con ID: " + id));

        validarPropiedadDeAgente(propiedad, usuarioActual);

        propiedad.setEstado(nuevoEstado);
        Propiedad actualizada = propiedadRepository.save(propiedad);
        return new PropiedadResponseDTO(actualizada);
    }

    @Transactional
    public void desactivarPropiedad(Integer id, Usuario usuarioActual) {
        Propiedad propiedad = propiedadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada con ID: " + id));

        // Desactivación lógica
        propiedad.setActivo(false);
        propiedad.setEstado("Inactivo");
        propiedadRepository.save(propiedad);
    }

    public void validarPropiedadDeAgente(Propiedad propiedad, Usuario usuarioActual) {
        if ("AGENTE".equals(usuarioActual.getRol())) {
            if (propiedad.getAgente() == null || !propiedad.getAgente().getId().equals(usuarioActual.getId())) {
                throw new AccessDeniedException("Acceso denegado: No tiene permisos para modificar una propiedad que no le pertenece.");
            }
        }
    }
}
