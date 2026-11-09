package com.inmobiliaria.backend.model;

import jakarta.persistence.*;

/** Estado del ciclo de vida de un ticket (tabla 'estados_ticket'). */
@Entity
@Table(name = "estados_ticket")
public class EstadoTicket {

    public static final String ABIERTO = "Abierto";
    public static final String EN_PROCESO = "En Proceso";
    public static final String RESUELTO = "Resuelto";
    public static final String CERRADO = "Cerrado";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String nombre;

    public EstadoTicket() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
