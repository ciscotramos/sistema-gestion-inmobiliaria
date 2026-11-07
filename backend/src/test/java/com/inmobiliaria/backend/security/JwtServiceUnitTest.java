package com.inmobiliaria.backend.security;

import com.inmobiliaria.backend.model.Usuario;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Endurecimiento del JWT: clave obligatoria y fuerte, emisor, firma, expiración y usuario activo. */
class JwtServiceUnitTest {

    // Claves sintéticas generadas en la prueba: ningún secreto literal en el repositorio
    private static final String SECRETO = "k".repeat(48);
    private static final String OTRO_SECRETO = "m".repeat(48);

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(5);
        usuario.setNombre("Ana");
        usuario.setEmail("ana@inmobiliaria.com");
        usuario.setRol("AGENTE");
        usuario.setEstado("Activo");
    }

    private JwtService servicio(String secreto, long expiracionMs) {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secretKey", secreto);
        ReflectionTestUtils.setField(service, "jwtExpiration", expiracionMs);
        service.validarConfiguracion();
        return service;
    }

    @Test
    void sinClave_laAplicacionNoArranca() {
        assertThrows(IllegalStateException.class, () -> servicio("", 60_000));
        assertThrows(IllegalStateException.class, () -> servicio(null, 60_000));
    }

    @Test
    void claveMuyCorta_laAplicacionNoArranca() {
        assertThrows(IllegalStateException.class, () -> servicio("corta", 60_000));
    }

    @Test
    void expiracionNoPositiva_laAplicacionNoArranca() {
        assertThrows(IllegalStateException.class, () -> servicio(SECRETO, 0));
    }

    @Test
    void tokenValido_seAceptaParaUsuarioActivo() {
        JwtService service = servicio(SECRETO, 60_000);
        String token = service.generateToken(usuario);

        assertEquals("ana@inmobiliaria.com", service.extractUsername(token));
        assertEquals("AGENTE", service.extractRol(token));
        assertTrue(service.isTokenValid(token, usuario));
    }

    @Test
    void cadaTokenTieneIdentificadorUnico() {
        JwtService service = servicio(SECRETO, 60_000);

        String jti1 = service.extractClaim(service.generateToken(usuario), c -> c.getId());
        String jti2 = service.extractClaim(service.generateToken(usuario), c -> c.getId());

        assertFalse(jti1.equals(jti2));
    }

    @Test
    void usuarioInactivo_noPuedeUsarUnTokenAunVigente() {
        JwtService service = servicio(SECRETO, 60_000);
        String token = service.generateToken(usuario);
        usuario.setEstado("Inactivo");

        assertFalse(service.isTokenValid(token, usuario));
    }

    @Test
    void tokenAlterado_seRechaza() {
        JwtService service = servicio(SECRETO, 60_000);
        String token = service.generateToken(usuario);
        String alterado = token.substring(0, token.length() - 2) + (token.endsWith("A") ? "BB" : "AA");

        assertThrows(JwtException.class, () -> service.extractUsername(alterado));
    }

    @Test
    void tokenFirmadoConOtraClave_seRechaza() {
        String tokenAjeno = servicio(OTRO_SECRETO, 60_000).generateToken(usuario);

        assertThrows(JwtException.class, () -> servicio(SECRETO, 60_000).extractUsername(tokenAjeno));
    }

    @Test
    void tokenSinEmisorEsperado_seRechaza() {
        String sinEmisor = Jwts.builder()
                .subject("ana@inmobiliaria.com")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(JwtService.construirClave(SECRETO), Jwts.SIG.HS256)
                .compact();

        assertThrows(JwtException.class, () -> servicio(SECRETO, 60_000).extractUsername(sinEmisor));
    }

    @Test
    void tokenExpirado_seRechaza() throws InterruptedException {
        JwtService service = servicio(SECRETO, 1);
        String token = service.generateToken(usuario);
        Thread.sleep(25);

        assertThrows(ExpiredJwtException.class, () -> service.extractUsername(token));
    }
}
