package com.inmobiliaria.backend.repository;

import com.inmobiliaria.backend.model.EquipoAsignado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipoAsignadoRepository extends JpaRepository<EquipoAsignado, Integer> {
    List<EquipoAsignado> findAllByOrderByNivelSoporteAsc();
}
