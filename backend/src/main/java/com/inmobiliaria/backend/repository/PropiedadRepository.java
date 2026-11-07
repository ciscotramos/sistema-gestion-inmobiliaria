package com.inmobiliaria.backend.repository;

import com.inmobiliaria.backend.model.Propiedad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropiedadRepository extends JpaRepository<Propiedad, Integer>, JpaSpecificationExecutor<Propiedad> {

    @EntityGraph(attributePaths = {"distrito", "agente"})
    Optional<Propiedad> findById(Integer id);

    @EntityGraph(attributePaths = {"distrito", "agente"})
    Page<Propiedad> findAll(Specification<Propiedad> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"distrito", "agente"})
    Page<Propiedad> findByAgenteId(Integer agenteId, Pageable pageable);
}
