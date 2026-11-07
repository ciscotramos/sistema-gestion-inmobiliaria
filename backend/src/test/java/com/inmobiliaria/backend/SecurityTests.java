package com.inmobiliaria.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.backend.dto.AgenteCreateDTO;
import com.inmobiliaria.backend.dto.LoginRequestDTO;
import com.inmobiliaria.backend.model.Usuario;
import com.inmobiliaria.backend.repository.UsuarioRepository;
import com.inmobiliaria.backend.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("TEST 1: POST /api/auth/login con credenciales ADMIN válidas devuelve 200 y token JWT")
    void test1_loginExitosoAdminDevuelveJwt() throws Exception {
        LoginRequestDTO login = new LoginRequestDTO("admin@inmobiliaria.com", "admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andExpect(jsonPath("$.email").value("admin@inmobiliaria.com"));
    }

    @Test
    @DisplayName("TEST 2: POST /api/auth/login con contraseña incorrecta devuelve 401 Unauthorized")
    void test2_loginPasswordInvalidoDevuelve401() throws Exception {
        LoginRequestDTO login = new LoginRequestDTO("admin@inmobiliaria.com", "claveIncorrecta999");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("TEST 3: GET /api/admin/agentes sin token devuelve 401 Unauthorized")
    void test3_accesoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/admin/agentes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("TEST 4: POST /api/admin/agentes con JWT de ADMIN crea agente exitosamente (201 Created)")
    void test4_adminCreaAgenteExitosamente() throws Exception {
        Usuario admin = usuarioRepository.findByEmail("admin@inmobiliaria.com").orElseThrow();
        String tokenAdmin = jwtService.generateToken(admin);

        String emailTest = "agente.nuevo." + System.currentTimeMillis() + "@inmobiliaria.com";
        AgenteCreateDTO nuevo = new AgenteCreateDTO(
                "Carlos Valdivia",
                emailTest,
                "password123",
                "+51 912 345 678",
                "Asesor Comercial"
        );

        mockMvc.perform(post("/api/admin/agentes")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value(emailTest))
                .andExpect(jsonPath("$.rol").value("AGENTE"));

        // Verificar formato de hash de contraseña
        Usuario enBd = usuarioRepository.findByEmail(emailTest).orElseThrow();
        assertTrue(enBd.getPasswordHash().startsWith("$2a$10$"));
    }

    @Test
    @DisplayName("TEST 5: POST /api/admin/agentes con JWT de AGENTE devuelve 403 Forbidden")
    void test5_agenteNoPuedeCrearAgenteDevuelve403() throws Exception {
        Usuario agente = usuarioRepository.findByEmail("juan.perez@inmobiliaria.com").orElseThrow();
        String tokenAgente = jwtService.generateToken(agente);

        AgenteCreateDTO dto = new AgenteCreateDTO(
                "Intruso",
                "intruso." + System.currentTimeMillis() + "@inmobiliaria.com",
                "password123",
                "+51 900 000 000",
                "Asesor"
        );

        mockMvc.perform(post("/api/admin/agentes")
                        .header("Authorization", "Bearer " + tokenAgente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("TEST 6: GET /api/agente/perfil con JWT de AGENTE devuelve 200 OK")
    void test6_agenteAccedeASuPerfil200() throws Exception {
        Usuario agente = usuarioRepository.findByEmail("juan.perez@inmobiliaria.com").orElseThrow();
        String tokenAgente = jwtService.generateToken(agente);

        mockMvc.perform(get("/api/agente/perfil")
                        .header("Authorization", "Bearer " + tokenAgente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("juan.perez@inmobiliaria.com"))
                .andExpect(jsonPath("$.rol").value("AGENTE"));
    }

    @Test
    @DisplayName("TEST 7: Verificación de hash BCrypt en BD para usuarios")
    void test7_verificarBCryptEnBaseDeDatos() {
        Usuario admin = usuarioRepository.findByEmail("admin@inmobiliaria.com").orElseThrow();
        assertNotNull(admin.getPasswordHash());
        assertTrue(admin.getPasswordHash().startsWith("$2a$10$"));
        assertTrue(admin.getPasswordHash().length() >= 60);
    }

    @Test
    @DisplayName("TEST 8: AGENTE intentando llamar endpoint exclusivo de ADMIN devuelve 403 Forbidden")
    void test8_agenteAccediendoEndpointAdminDevuelve403() throws Exception {
        Usuario agente = usuarioRepository.findByEmail("juan.perez@inmobiliaria.com").orElseThrow();
        String tokenAgente = jwtService.generateToken(agente);

        mockMvc.perform(get("/api/admin/agentes")
                        .header("Authorization", "Bearer " + tokenAgente))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("TEST 9: Enviar payload XSS <script>alert('XSS')</script> devuelve 400 Bad Request")
    void test9_payloadXssRechazadoCon400() throws Exception {
        Usuario admin = usuarioRepository.findByEmail("admin@inmobiliaria.com").orElseThrow();
        String tokenAdmin = jwtService.generateToken(admin);

        AgenteCreateDTO xssDto = new AgenteCreateDTO(
                "<script>alert('XSS')</script>",
                "xss." + System.currentTimeMillis() + "@inmobiliaria.com",
                "password123",
                "+51 900 000 000",
                "Asesor"
        );

        mockMvc.perform(post("/api/admin/agentes")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(xssDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("scripts")));
    }

    @Test
    @DisplayName("TEST 10: Solicitud OPTIONS desde http://localhost:5173 responde con cabeceras CORS")
    void test10_corsPermitidoParaFrontend() throws Exception {
        mockMvc.perform(options("/api/admin/agentes")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }
}
