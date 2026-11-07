package com.inmobiliaria.backend.controller;

import com.inmobiliaria.backend.dto.AgenteResponseDTO;
import com.inmobiliaria.backend.model.Usuario;
import com.inmobiliaria.backend.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agente")
@PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
public class AgenteController {

    private final UsuarioRepository usuarioRepository;

    public AgenteController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Retorna el perfil del usuario autenticado.
     */
    @GetMapping("/perfil")
    public ResponseEntity<AgenteResponseDTO> obtenerMiPerfil(Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario en sesión no encontrado"));

        return ResponseEntity.ok(new AgenteResponseDTO(usuario));
    }
}
