package com.inmobiliaria.backend.repository;

import com.inmobiliaria.backend.model.Operacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperacionRepository extends JpaRepository<Operacion, Integer> {
}
