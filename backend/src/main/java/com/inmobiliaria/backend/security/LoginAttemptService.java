package com.inmobiliaria.backend.security;

import com.inmobiliaria.backend.exception.TooManyLoginAttemptsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protección contra fuerza bruta en el inicio de sesión.
 *
 * Cuenta los intentos fallidos por cuenta (correo) y por dirección IP dentro de una
 * ventana de 15 minutos. Al superar el máximo, bloquea nuevos intentos hasta que la
 * ventana termina. Un inicio de sesión correcto reinicia el contador de la cuenta.
 *
 * Los contadores viven en memoria: se reinician con la aplicación y no se comparten
 * entre varias instancias del backend.
 */
@Service
public class LoginAttemptService {

    static final int MAX_FALLOS_POR_CUENTA = 5;
    static final int MAX_FALLOS_POR_IP = 20;
    static final Duration VENTANA = Duration.ofMinutes(15);
    private static final int TAMANO_MAXIMO_MAPA = 10_000;

    private static final class Registro {
        private int fallos;
        private Instant inicioVentana;
    }

    private final Clock clock;
    private final Map<String, Registro> registros = new ConcurrentHashMap<>();

    @Autowired
    public LoginAttemptService() {
        this(Clock.systemUTC());
    }

    public LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    /** Lanza TooManyLoginAttemptsException si la cuenta o la IP están bloqueadas. */
    public void verificarBloqueo(String email, String ip) {
        long restanteCuenta = segundosRestantes(claveCuenta(email), MAX_FALLOS_POR_CUENTA);
        long restanteIp = segundosRestantes(claveIp(ip), MAX_FALLOS_POR_IP);
        long restante = Math.max(restanteCuenta, restanteIp);
        if (restante > 0) {
            throw new TooManyLoginAttemptsException(restante);
        }
    }

    public void registrarFallo(String email, String ip) {
        limpiarVencidos();
        incrementar(claveCuenta(email));
        incrementar(claveIp(ip));
    }

    public void registrarExito(String email, String ip) {
        registros.remove(claveCuenta(email));
    }

    private void incrementar(String clave) {
        Instant ahora = clock.instant();
        registros.compute(clave, (k, actual) -> {
            if (actual == null || !ahora.isBefore(actual.inicioVentana.plus(VENTANA))) {
                actual = new Registro();
                actual.inicioVentana = ahora;
            }
            actual.fallos++;
            return actual;
        });
    }

    private long segundosRestantes(String clave, int maximo) {
        Registro registro = registros.get(clave);
        if (registro == null || registro.fallos < maximo) {
            return 0;
        }
        Instant fin = registro.inicioVentana.plus(VENTANA);
        long restante = Duration.between(clock.instant(), fin).getSeconds();
        return Math.max(restante, 0);
    }

    private void limpiarVencidos() {
        if (registros.size() < TAMANO_MAXIMO_MAPA) {
            return;
        }
        Instant ahora = clock.instant();
        registros.entrySet().removeIf(e -> !ahora.isBefore(e.getValue().inicioVentana.plus(VENTANA)));
    }

    private static String claveCuenta(String email) {
        return "cuenta:" + (email == null ? "" : email.trim().toLowerCase());
    }

    private static String claveIp(String ip) {
        return "ip:" + (ip == null ? "" : ip);
    }
}
