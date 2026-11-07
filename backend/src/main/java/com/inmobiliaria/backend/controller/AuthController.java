package com.inmobiliaria.backend.controller;

import com.inmobiliaria.backend.dto.AuthResponseDTO;
import com.inmobiliaria.backend.dto.LoginRequestDTO;
import com.inmobiliaria.backend.model.Usuario;
import com.inmobiliaria.backend.repository.UsuarioRepository;
import com.inmobiliaria.backend.security.JwtService;
import com.inmobiliaria.backend.security.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;

    public AuthController(
            AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            JwtService jwtService,
            LoginAttemptService loginAttemptService
    ) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
    }

    /**
     * Autentica credenciales y emite el token JWT de sesión.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        String email = request.getEmail().trim().toLowerCase();
        String ip = httpRequest.getRemoteAddr();

        // Fuerza bruta: se rechaza antes de tocar la base de datos
        loginAttemptService.verificarBloqueo(email, ip);

        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
        } catch (AuthenticationException e) {
            loginAttemptService.registrarFallo(email, ip);
            throw e;
        }

        if (!auth.isAuthenticated()) {
            loginAttemptService.registrarFallo(email, ip);
            throw new BadCredentialsException("Credenciales inválidas");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

        if (!"Activo".equalsIgnoreCase(usuario.getEstado())) {
            loginAttemptService.registrarFallo(email, ip);
            throw new BadCredentialsException("El usuario se encuentra inactivo");
        }

        loginAttemptService.registrarExito(email, ip);

        usuario.setUltimoLogin(LocalDateTime.now());
        usuarioRepository.save(usuario);

        String token = jwtService.generateToken(usuario);

        return ResponseEntity.ok(new AuthResponseDTO(
                token,
                usuario.getRol(),
                usuario.getNombre(),
                usuario.getEmail()
        ));
    }
}
