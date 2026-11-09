package com.inmobiliaria.backend.repository;

import com.inmobiliaria.backend.model.EstadoTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoTicketRepository extends JpaRepository<EstadoTicket, Integer> {
    Optional<EstadoTicket> findByNombre(String nombre);
}
