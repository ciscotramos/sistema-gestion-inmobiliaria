package com.inmobiliaria.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.backend.dto.DistritoCreateDTO;
import com.inmobiliaria.backend.model.UbicacionDistrito;
import com.inmobiliaria.backend.model.Usuario;
import com.inmobiliaria.backend.repository.UbicacionDistritoRepository;
import com.inmobiliaria.backend.repository.UsuarioRepository;
import com.inmobiliaria.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class DistritoControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UbicacionDistritoRepository distritoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    private String tokenAdmin;
    private String tokenAgente;

    @BeforeEach
    void setUp() {
        Usuario admin = usuarioRepository.findByEmail("admin@inmobiliaria.com").orElseThrow();
        Usuario agente = usuarioRepository.findByEmail("juan.perez@inmobiliaria.com").orElseThrow();
        tokenAdmin = jwtService.generateToken(admin);
        tokenAgente = jwtService.generateToken(agente);
    }

    @Test
    @DisplayName("DISTRITOS 1: GET /api/distritos sin token devuelve 200 OK (acceso público)")
    void test1_listarDistritosPublico200() throws Exception {
        mockMvc.perform(get("/api/distritos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("DISTRITOS 2: GET /api/distritos/{id} existente devuelve 200 OK con datos")
    void test2_obtenerPorIdExistente200() throws Exception {
        String testNombre = "San Isidro Test " + System.currentTimeMillis();
        UbicacionDistrito testDistrito = distritoRepository.save(new UbicacionDistrito(testNombre, "Lima", "Lima"));
        Thread.sleep(100); // Sincronización de réplica MySQL Master/Replica

        mockMvc.perform(get("/api/distritos/" + testDistrito.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testDistrito.getId()))
                .andExpect(jsonPath("$.nombre").value(testNombre))
                .andExpect(jsonPath("$.provincia").value("Lima"))
                .andExpect(jsonPath("$.departamento").value("Lima"));
    }

    @Test
    @DisplayName("DISTRITOS 3: GET /api/distritos/{id} inexistente devuelve 404 Not Found")
    void test3_obtenerPorIdInexistente404() throws Exception {
        mockMvc.perform(get("/api/distritos/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message", containsString("Distrito no encontrado")));
    }

    @Test
    @DisplayName("DISTRITOS 4: POST /api/distritos con rol ADMIN crea distrito exitosamente (201 Created)")
    void test4_adminCreaDistrito201() throws Exception {
        String nombreUnico = "Surco Test " + System.currentTimeMillis();
        DistritoCreateDTO dto = new DistritoCreateDTO(nombreUnico, "Lima", "Lima");

        mockMvc.perform(post("/api/distritos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value(nombreUnico))
                .andExpect(jsonPath("$.provincia").value("Lima"))
                .andExpect(jsonPath("$.departamento").value("Lima"));
    }

    @Test
    @DisplayName("DISTRITOS 5: POST /api/distritos sin autenticación devuelve 401 Unauthorized")
    void test5_crearDistritoSinToken401() throws Exception {
        DistritoCreateDTO dto = new DistritoCreateDTO("Barranco", "Lima", "Lima");

        mockMvc.perform(post("/api/distritos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("DISTRITOS 6: POST /api/distritos con rol AGENTE devuelve 403 Forbidden")
    void test6_agenteNoPuedeCrearDistrito403() throws Exception {
        DistritoCreateDTO dto = new DistritoCreateDTO("La Molina", "Lima", "Lima");

        mockMvc.perform(post("/api/distritos")
                        .header("Authorization", "Bearer " + tokenAgente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("DISTRITOS 7: POST /api/distritos con combinación nombre+provincia+dep duplicada devuelve 409 Conflict")
    void test7_distritoDuplicadoDevuelve409() throws Exception {
        String nombreDuplicado = "Miraflores Dup " + System.currentTimeMillis();
        distritoRepository.save(new UbicacionDistrito(nombreDuplicado, "Lima", "Lima"));

        DistritoCreateDTO dto = new DistritoCreateDTO(nombreDuplicado, "Lima", "Lima");

        mockMvc.perform(post("/api/distritos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message", containsString("ya se encuentra registrado")));
    }

    @Test
    @DisplayName("DISTRITOS 8: POST /api/distritos con datos inválidos (nombre vacío) devuelve 400 Bad Request")
    void test8_datosInvalidosDevuelve400() throws Exception {
        DistritoCreateDTO dto = new DistritoCreateDTO("", "Lima", "Lima");

        mockMvc.perform(post("/api/distritos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("obligatorio")));
    }

    @Test
    @DisplayName("DISTRITOS 9: POST /api/distritos con intento de XSS <script> devuelve 400 Bad Request")
    void test9_intentoXssDevuelve400() throws Exception {
        DistritoCreateDTO dto = new DistritoCreateDTO("<script>alert('xss')</script>", "Lima", "Lima");

        mockMvc.perform(post("/api/distritos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("scripts")));
    }
}
