package com.inmobiliaria.backend.exception;

/** Se superó el máximo de intentos fallidos de inicio de sesión para una cuenta o una IP. */
public class TooManyLoginAttemptsException extends RuntimeException {

    private final long retryAfterSeconds;

    public TooManyLoginAttemptsException(long retryAfterSeconds) {
        super("Demasiados intentos fallidos de inicio de sesión");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
