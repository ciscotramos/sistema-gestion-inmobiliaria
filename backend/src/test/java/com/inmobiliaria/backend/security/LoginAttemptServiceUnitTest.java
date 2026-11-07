package com.inmobiliaria.backend.security;

import com.inmobiliaria.backend.exception.TooManyLoginAttemptsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Protección contra fuerza bruta: bloqueo por cuenta y por IP, reinicio y desbloqueo por tiempo. */
class LoginAttemptServiceUnitTest {

    static class RelojMutable extends Clock {
        Instant ahora = Instant.parse("2026-11-07T12:00:00Z");

        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return ahora; }
    }

    private RelojMutable reloj;
    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        reloj = new RelojMutable();
        service = new LoginAttemptService(reloj);
    }

    private void fallar(String email, String ip, int veces) {
        for (int i = 0; i < veces; i++) {
            service.registrarFallo(email, ip);
        }
    }

    @Test
    void cuatroFallos_noBloquean() {
        fallar("ana@x.com", "1.1.1.1", 4);

        assertDoesNotThrow(() -> service.verificarBloqueo("ana@x.com", "1.1.1.1"));
    }

    @Test
    void cincoFallosEnLaCuenta_bloquean_conTiempoDeEspera() {
        fallar("ana@x.com", "1.1.1.1", 5);

        TooManyLoginAttemptsException ex = assertThrows(TooManyLoginAttemptsException.class,
                () -> service.verificarBloqueo("ana@x.com", "9.9.9.9"));
        assertTrue(ex.getRetryAfterSeconds() > 0);
    }

    @Test
    void elBloqueoDeUnaCuenta_noAfectaAOtra() {
        fallar("ana@x.com", "1.1.1.1", 5);

        assertDoesNotThrow(() -> service.verificarBloqueo("luis@x.com", "2.2.2.2"));
    }

    @Test
    void elCorreo_seNormalizaSinImportarMayusculas() {
        fallar("Ana@X.com", "1.1.1.1", 3);
        fallar("ana@x.COM", "1.1.1.1", 2);

        assertThrows(TooManyLoginAttemptsException.class,
                () -> service.verificarBloqueo("ANA@x.com", "8.8.8.8"));
    }

    @Test
    void unInicioDeSesionCorrecto_reiniciaElContadorDeLaCuenta() {
        fallar("ana@x.com", "1.1.1.1", 4);
        service.registrarExito("ana@x.com", "1.1.1.1");
        fallar("ana@x.com", "1.1.1.1", 4);

        assertDoesNotThrow(() -> service.verificarBloqueo("ana@x.com", "1.1.1.1"));
    }

    @Test
    void despuesDeLaVentana_seDesbloquea() {
        fallar("ana@x.com", "1.1.1.1", 5);
        reloj.ahora = reloj.ahora.plus(Duration.ofMinutes(16));

        assertDoesNotThrow(() -> service.verificarBloqueo("ana@x.com", "1.1.1.1"));
    }

    @Test
    void veinteFallosDesdeUnaIpConDistintasCuentas_bloqueanLaIp() {
        for (int i = 0; i < 20; i++) {
            service.registrarFallo("usuario" + i + "@x.com", "6.6.6.6");
        }

        assertThrows(TooManyLoginAttemptsException.class,
                () -> service.verificarBloqueo("otro@x.com", "6.6.6.6"));
        assertDoesNotThrow(() -> service.verificarBloqueo("otro@x.com", "7.7.7.7"));
    }
}
