package com.inmobiliaria.backend.repository;

import com.inmobiliaria.backend.model.Prioridad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrioridadRepository extends JpaRepository<Prioridad, Integer> {
    Optional<Prioridad> findByNivel(String nivel);
}
