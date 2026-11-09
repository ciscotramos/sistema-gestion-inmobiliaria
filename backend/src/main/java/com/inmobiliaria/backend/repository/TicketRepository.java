package com.inmobiliaria.backend.repository;

import com.inmobiliaria.backend.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    List<Ticket> findAllByOrderByFechaRegistroDescIdDesc();
    List<Ticket> findByReportadoPorIdOrderByFechaRegistroDescIdDesc(Integer usuarioId);
}
