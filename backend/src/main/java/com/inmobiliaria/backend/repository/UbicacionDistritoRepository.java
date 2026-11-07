package com.inmobiliaria.backend.repository;

import com.inmobiliaria.backend.model.UbicacionDistrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UbicacionDistritoRepository extends JpaRepository<UbicacionDistrito, Integer> {

    List<UbicacionDistrito> findAllByOrderByNombreAsc();

    boolean existsByNombreIgnoreCaseAndProvinciaIgnoreCaseAndDepartamentoIgnoreCase(
            String nombre,
            String provincia,
            String departamento
    );

    Optional<UbicacionDistrito> findByNombreIgnoreCaseAndProvinciaIgnoreCaseAndDepartamentoIgnoreCase(
            String nombre,
            String provincia,
            String departamento
    );
}
