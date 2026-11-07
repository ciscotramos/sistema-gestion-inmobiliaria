package com.inmobiliaria.backend.config;

import com.inmobiliaria.backend.model.Usuario;
import com.inmobiliaria.backend.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    @Order(10)
    public CommandLineRunner seedInitialUsers(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            try {
                // 1. Sembrar Administrador si no existe
                String adminEmail = "admin@inmobiliaria.com";
                if (!usuarioRepository.existsByEmail(adminEmail)) {
                    Usuario admin = new Usuario(
                            "Luis Administrador",
                            adminEmail,
                            passwordEncoder.encode("admin123"),
                            "ADMIN",
                            "+51 999 888 777",
                            "Administrador General"
                    );
                    usuarioRepository.save(admin);
                    log.info("✅ Usuario ADMIN inicial creado con hash BCrypt: {}", adminEmail);
                }

                // 2. Sembrar Agente base si no existe
                String agenteEmail = "juan.perez@inmobiliaria.com";
                if (!usuarioRepository.existsByEmail(agenteEmail)) {
                    Usuario agente = new Usuario(
                            "Juan Carlos Pérez",
                            agenteEmail,
                            passwordEncoder.encode("agente123"),
                            "AGENTE",
                            "+51 987 654 321",
                            "Asesor Inmobiliario"
                    );
                    usuarioRepository.save(agente);
                    log.info("✅ Usuario AGENTE inicial creado con hash BCrypt: {}", agenteEmail);
                }
            } catch (Exception e) {
                log.warn("Nota sobre inicialización de usuarios: {}", e.getMessage());
            }
        };
    }
}
