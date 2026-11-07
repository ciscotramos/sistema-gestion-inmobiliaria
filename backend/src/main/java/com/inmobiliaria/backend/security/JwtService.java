package com.inmobiliaria.backend.security;

import com.inmobiliaria.backend.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    /** Emisor del token: solo se aceptan tokens firmados y emitidos por esta API. */
    static final String ISSUER = "inmobiliaria-api";

    @Value("${jwt.secret:}")
    private String secretKey;

    @Value("${jwt.expiration:7200000}")
    private long jwtExpiration;

    private SecretKey signingKey;

    /**
     * Falla al arrancar si la configuración es insegura: sin clave por defecto
     * y con un mínimo de 256 bits. Evita que la API funcione con una clave conocida.
     */
    @PostConstruct
    void validarConfiguracion() {
        this.signingKey = construirClave(secretKey);
        if (jwtExpiration <= 0) {
            throw new IllegalStateException("jwt.expiration debe ser un valor positivo en milisegundos");
        }
    }

    static SecretKey construirClave(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET no está configurado. Define una clave aleatoria de al menos 32 caracteres.");
        }

        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
            if (keyBytes.length < 32) {
                keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            }
        } catch (RuntimeException e) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 bytes (256 bits).");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey getSigningKey() {
        if (signingKey == null) {
            signingKey = construirClave(secretKey);
        }
        return signingKey;
    }

    /**
     * Genera un token JWT para el usuario con claims de identidad y rol.
     */
    public String generateToken(Usuario usuario) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("id", usuario.getId());
        extraClaims.put("nombre", usuario.getNombre());
        extraClaims.put("rol", usuario.getRol());

        return Jwts.builder()
                .claims(extraClaims)
                .id(UUID.randomUUID().toString())
                .issuer(ISSUER)
                .subject(usuario.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Extrae el email (subject) del token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae el rol del usuario contenido en los claims del token.
     */
    public String extractRol(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("rol", String.class);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Valida la firma y vigencia del token contra los datos del usuario.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername())
                && userDetails.isEnabled()
                && !isTokenExpired(token);
    }

    /**
     * Verifica si el token ha expirado.
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .requireIssuer(ISSUER)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
