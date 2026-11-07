package com.inmobiliaria.backend.repository;

import com.inmobiliaria.backend.model.Propiedad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropiedadRepository extends JpaRepository<Propiedad, Integer>, JpaSpecificationExecutor<Propiedad> {

    @EntityGraph(attributePaths = {"distrito", "agente"})
    Optional<Propiedad> findById(Integer id);

    @EntityGraph(attributePaths = {"distrito", "agente"})
    Page<Propiedad> findAll(Specification<Propiedad> spec, Pageable pageable);

    /**
     * Bloqueo pesimista (SELECT ... FOR UPDATE): ninguna otra transacción puede
     * modificar la fila hasta el commit o rollback. Se usa al registrar ventas y alquileres.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Propiedad p where p.id = :id")
    Optional<Propiedad> findByIdForUpdate(@Param("id") Integer id);

    @EntityGraph(attributePaths = {"distrito", "agente"})
    Page<Propiedad> findByAgenteId(Integer agenteId, Pageable pageable);
}
