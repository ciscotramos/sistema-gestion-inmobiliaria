package com.inmobiliaria.backend.exception;

/** Datos o transición de estado no válidos en la gestión de tickets (HTTP 400). */
public class TicketInvalidoException extends RuntimeException {
    public TicketInvalidoException(String message) {
        super(message);
    }
}
