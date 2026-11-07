package com.inmobiliaria.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.backend.dto.CaracteristicaCreateDTO;
import com.inmobiliaria.backend.model.Caracteristica;
import com.inmobiliaria.backend.model.Usuario;
import com.inmobiliaria.backend.repository.CaracteristicaRepository;
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
public class CaracteristicaControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CaracteristicaRepository caracteristicaRepository;

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
    @DisplayName("CARACTERISTICAS 1: GET /api/caracteristicas sin token devuelve 200 OK (público)")
    void test1_listarCaracteristicasPublico200() throws Exception {
        mockMvc.perform(get("/api/caracteristicas"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("CARACTERISTICAS 2: GET /api/caracteristicas/{id} existente devuelve 200 OK")
    void test2_obtenerPorIdExistente200() throws Exception {
        String nombreUnico = "Piscina Test " + System.currentTimeMillis();
        Caracteristica testCarac = caracteristicaRepository.save(new Caracteristica(nombreUnico, "Exteriores"));
        Thread.sleep(100); // Sincronización de réplica MySQL Master/Replica

        mockMvc.perform(get("/api/caracteristicas/" + testCarac.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCarac.getId()))
                .andExpect(jsonPath("$.nombre").value(nombreUnico))
                .andExpect(jsonPath("$.categoria").value("Exteriores"));
    }

    @Test
    @DisplayName("CARACTERISTICAS 3: GET /api/caracteristicas/{id} inexistente devuelve 404 Not Found")
    void test3_obtenerPorIdInexistente404() throws Exception {
        mockMvc.perform(get("/api/caracteristicas/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message", containsString("Característica no encontrada")));
    }

    @Test
    @DisplayName("CARACTERISTICAS 4: POST /api/caracteristicas con rol ADMIN crea característica exitosamente (201 Created)")
    void test4_adminCreaCaracteristica201() throws Exception {
        String nombreUnico = "Ascensor Directo " + System.currentTimeMillis();
        CaracteristicaCreateDTO dto = new CaracteristicaCreateDTO(nombreUnico, "Edificio");

        mockMvc.perform(post("/api/caracteristicas")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value(nombreUnico))
                .andExpect(jsonPath("$.categoria").value("Edificio"));
    }

    @Test
    @DisplayName("CARACTERISTICAS 5: POST /api/caracteristicas sin autenticación devuelve 401 Unauthorized")
    void test5_crearSinTokenDevuelve401() throws Exception {
        CaracteristicaCreateDTO dto = new CaracteristicaCreateDTO("Gimnasio", "Comodidades");

        mockMvc.perform(post("/api/caracteristicas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("CARACTERISTICAS 6: POST /api/caracteristicas con rol AGENTE devuelve 403 Forbidden")
    void test6_agenteNoPuedeCrearCaracteristica403() throws Exception {
        CaracteristicaCreateDTO dto = new CaracteristicaCreateDTO("Terraza BBQ", "Exteriores");

        mockMvc.perform(post("/api/caracteristicas")
                        .header("Authorization", "Bearer " + tokenAgente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("CARACTERISTICAS 7: POST /api/caracteristicas con nombre duplicado devuelve 409 Conflict")
    void test7_nombreDuplicadoDevuelve409() throws Exception {
        String nombreDuplicado = "Cochera Doble " + System.currentTimeMillis();
        caracteristicaRepository.save(new Caracteristica(nombreDuplicado, "Estacionamiento"));

        CaracteristicaCreateDTO dto = new CaracteristicaCreateDTO(nombreDuplicado, "Estacionamiento");

        mockMvc.perform(post("/api/caracteristicas")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message", containsString("ya se encuentra registrada")));
    }

    @Test
    @DisplayName("CARACTERISTICAS 8: DELETE /api/caracteristicas/{id} con rol ADMIN elimina exitosamente (204 No Content)")
    void test8_adminEliminaCaracteristica204() throws Exception {
        String nombreABorrar = "Jacuzzi Test " + System.currentTimeMillis();
        Caracteristica guardada = caracteristicaRepository.save(new Caracteristica(nombreABorrar, "Lujo"));
        Thread.sleep(100);

        mockMvc.perform(delete("/api/caracteristicas/" + guardada.getId())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());

        Thread.sleep(100);

        mockMvc.perform(get("/api/caracteristicas/" + guardada.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CARACTERISTICAS 9: DELETE /api/caracteristicas/{id} inexistente devuelve 404 Not Found")
    void test9_eliminarInexistenteDevuelve404() throws Exception {
        mockMvc.perform(delete("/api/caracteristicas/999999")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("CARACTERISTICAS 10: POST /api/caracteristicas con datos inválidos (nombre vacío) devuelve 400 Bad Request")
    void test10_datosInvalidosDevuelve400() throws Exception {
        CaracteristicaCreateDTO dto = new CaracteristicaCreateDTO("", "General");

        mockMvc.perform(post("/api/caracteristicas")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("obligatorio")));
    }

    @Test
    @DisplayName("CARACTERISTICAS 11: POST /api/caracteristicas con payload XSS devuelve 400 Bad Request")
    void test11_intentoXssDevuelve400() throws Exception {
        CaracteristicaCreateDTO dto = new CaracteristicaCreateDTO("<script>alert('hack')</script>", "Seguridad");

        mockMvc.perform(post("/api/caracteristicas")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("scripts")));
    }
}
