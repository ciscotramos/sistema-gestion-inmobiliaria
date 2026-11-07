package com.inmobiliaria.backend.repository;

import com.inmobiliaria.backend.model.Caracteristica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaracteristicaRepository extends JpaRepository<Caracteristica, Integer> {

    List<Caracteristica> findAllByOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);

    Optional<Caracteristica> findByNombreIgnoreCase(String nombre);

    List<Caracteristica> findByCategoriaIgnoreCaseOrderByNombreAsc(String categoria);
}
