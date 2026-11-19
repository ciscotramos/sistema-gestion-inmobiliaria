package com.inmobiliaria.backend.repository;

import com.inmobiliaria.backend.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    List<Ticket> findAllByOrderByFechaRegistroDescIdDesc();
    /** Evita duplicar el ticket de una alerta que sigue activa. */
    boolean existsByTituloAndEstado_NombreIn(String titulo, Collection<String> estados);
    List<Ticket> findByReportadoPorIdOrderByFechaRegistroDescIdDesc(Integer usuarioId);
}
