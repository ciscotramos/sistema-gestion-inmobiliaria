package com.inmobiliaria.backend.model;

import jakarta.persistence.*;

/** Nivel de prioridad ITIL (P1 a P4) con su tiempo SLA de resolución (tabla 'prioridades'). */
@Entity
@Table(name = "prioridades")
public class Prioridad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 2)
    private String nivel;

    @Column(nullable = false, length = 30)
    private String nombre;

    @Column(name = "tiempo_sla_horas", nullable = false)
    private Integer tiempoSlaHoras;

    public Prioridad() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getTiempoSlaHoras() { return tiempoSlaHoras; }
    public void setTiempoSlaHoras(Integer tiempoSlaHoras) { this.tiempoSlaHoras = tiempoSlaHoras; }
}
