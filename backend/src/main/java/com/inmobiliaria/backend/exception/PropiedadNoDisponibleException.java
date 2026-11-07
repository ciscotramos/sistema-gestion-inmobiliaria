package com.inmobiliaria.backend.exception;

/** La propiedad ya no puede venderse o alquilarse (otro asesor la confirmó primero, está inactiva, etc.). */
public class PropiedadNoDisponibleException extends RuntimeException {
    public PropiedadNoDisponibleException(String message) {
        super(message);
    }
}
