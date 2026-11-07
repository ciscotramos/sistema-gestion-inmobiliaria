package com.inmobiliaria.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.backend.dto.PropiedadCreateDTO;
import com.inmobiliaria.backend.dto.PropiedadEstadoDTO;
import com.inmobiliaria.backend.dto.PropiedadImagenCreateDTO;
import com.inmobiliaria.backend.dto.PropiedadUpdateDTO;
import com.inmobiliaria.backend.model.Caracteristica;
import com.inmobiliaria.backend.model.Propiedad;
import com.inmobiliaria.backend.model.UbicacionDistrito;
import com.inmobiliaria.backend.model.Usuario;
import com.inmobiliaria.backend.repository.CaracteristicaRepository;
import com.inmobiliaria.backend.repository.PropiedadRepository;
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
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PropiedadControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PropiedadRepository propiedadRepository;

    @Autowired
    private UbicacionDistritoRepository distritoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CaracteristicaRepository caracteristicaRepository;

    @Autowired
    private JwtService jwtService;

    private String tokenAdmin;
    private String tokenAgente1;
    private String tokenAgente2;
    private Usuario agente1;
    private Usuario agente2;
    private UbicacionDistrito distritoPrueba;
    private Caracteristica caracPrueba;

    @BeforeEach
    void setUp() {
        Usuario admin = usuarioRepository.findByEmail("admin@inmobiliaria.com").orElseThrow();
        agente1 = usuarioRepository.findByEmail("juan.perez@inmobiliaria.com").orElseThrow();
        agente2 = usuarioRepository.findByEmail("carlos.mendoza@inmobiliaria.com").orElseGet(() ->
                usuarioRepository.save(new Usuario(
                        "Carlos Mendoza",
                        "carlos.mendoza@inmobiliaria.com",
                        "$2a$10$abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopq",
                        "AGENTE",
                        "+51 912 345 678",
                        "Asesor Comercial"
                ))
        );

        tokenAdmin = jwtService.generateToken(admin);
        tokenAgente1 = jwtService.generateToken(agente1);
        tokenAgente2 = jwtService.generateToken(agente2);

        distritoPrueba = distritoRepository.findAll().stream().findFirst().orElseGet(() ->
                distritoRepository.save(new UbicacionDistrito("Miraflores Test", "Lima", "Lima"))
        );

        caracPrueba = caracteristicaRepository.findAll().stream().findFirst().orElseGet(() ->
                caracteristicaRepository.save(new Caracteristica("Piscina Test", "Exteriores"))
        );
    }

    @Test
    @DisplayName("PROPIEDADES 1: GET /api/propiedades sin token devuelve 200 OK (acceso público)")
    void test1_listarPropiedadesPublico200() throws Exception {
        mockMvc.perform(get("/api/propiedades"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("PROPIEDADES 2: GET /api/propiedades/{id} con ID inexistente devuelve 404 Not Found")
    void test2_buscarPorIdInexistente404() throws Exception {
        mockMvc.perform(get("/api/propiedades/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("PROPIEDADES 3: POST /api/propiedades sin token devuelve 401 Unauthorized")
    void test3_crearPropiedadSinToken401() throws Exception {
        PropiedadCreateDTO dto = new PropiedadCreateDTO();
        dto.setTitulo("Casa de Lujo");
        dto.setOperacion("Venta");
        dto.setTipo("Casa");
        dto.setPrecio(new BigDecimal("350000.00"));
        dto.setDistritoId(distritoPrueba.getId());

        mockMvc.perform(post("/api/propiedades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PROPIEDADES 4: POST /api/propiedades con datos inválidos devuelve 400 Bad Request")
    void test4_crearPropiedadDatosInvalidos400() throws Exception {
        PropiedadCreateDTO dto = new PropiedadCreateDTO();
        dto.setTitulo(""); // inválido (blanco)
        dto.setOperacion("Invalida"); // inválido (no es Venta ni Alquiler)
        dto.setTipo("Auto"); // inválido
        dto.setPrecio(new BigDecimal("-100.00")); // inválido (negativo)
        dto.setDistritoId(null); // inválido (requerido)

        mockMvc.perform(post("/api/propiedades")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("PROPIEDADES 5: POST /api/propiedades como AGENTE crea propiedad asignada a sí mismo (201 Created)")
    void test5_crearPropiedadComoAgente201() throws Exception {
        PropiedadCreateDTO dto = new PropiedadCreateDTO();
        dto.setTitulo("Departamento Moderno en Miraflores");
        dto.setDescripcion("Hermoso flat con vista al parque");
        dto.setOperacion("Venta");
        dto.setTipo("Departamento");
        dto.setPrecio(new BigDecimal("185000.00"));
        dto.setMoneda("USD");
        dto.setDormitorios(3);
        dto.setBanos(2);
        dto.setAreaConstruida(new BigDecimal("95.50"));
        dto.setAreaTotal(new BigDecimal("95.50"));
        dto.setDireccion("Av. Larco 123");
        dto.setDistritoId(distritoPrueba.getId());
        dto.setCaracteristicasIds(Set.of(caracPrueba.getId()));
        dto.setImagenes(List.of("https://example.com/foto1.jpg", "https://example.com/foto2.jpg"));

        MvcResult result = mockMvc.perform(post("/api/propiedades")
                        .header("Authorization", "Bearer " + tokenAgente1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.titulo").value("Departamento Moderno en Miraflores"))
                .andExpect(jsonPath("$.operacion").value("Venta"))
                .andExpect(jsonPath("$.tipo").value("Departamento"))
                .andExpect(jsonPath("$.agenteId").value(agente1.getId()))
                .andExpect(jsonPath("$.caracteristicas", hasSize(1)))
                .andExpect(jsonPath("$.imagenes", hasSize(2)))
                .andExpect(jsonPath("$.imagenPrincipal").value("https://example.com/foto1.jpg"))
                .andReturn();

        // Pausa para sincronización binlog hacia la réplica en Docker
        Thread.sleep(100);

        String json = result.getResponse().getContentAsString();
        Integer propiedadId = objectMapper.readTree(json).get("id").asInt();

        mockMvc.perform(get("/api/propiedades/" + propiedadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Departamento Moderno en Miraflores"));
    }

    @Test
    @DisplayName("PROPIEDADES 6: PUT /api/propiedades/{id} AGENTE actualiza SU PROPIA propiedad (200 OK)")
    void test6_actualizarPropiedadPropiaComoAgente200() throws Exception {
        // Crear propiedad con agente1
        PropiedadCreateDTO createDto = new PropiedadCreateDTO();
        createDto.setTitulo("Casa para reformar");
        createDto.setOperacion("Venta");
        createDto.setTipo("Casa");
        createDto.setPrecio(new BigDecimal("120000.00"));
        createDto.setDistritoId(distritoPrueba.getId());

        MvcResult createResult = mockMvc.perform(post("/api/propiedades")
                        .header("Authorization", "Bearer " + tokenAgente1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Integer propId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asInt();

        // Actualizar como agente1
        PropiedadUpdateDTO updateDto = new PropiedadUpdateDTO();
        updateDto.setTitulo("Casa Completamente Reformada");
        updateDto.setOperacion("Venta");
        updateDto.setTipo("Casa");
        updateDto.setPrecio(new BigDecimal("160000.00"));
        updateDto.setDistritoId(distritoPrueba.getId());

        mockMvc.perform(put("/api/propiedades/" + propId)
                        .header("Authorization", "Bearer " + tokenAgente1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Casa Completamente Reformada"))
                .andExpect(jsonPath("$.precio").value(160000.00));
    }

    @Test
    @DisplayName("PROPIEDADES 7: PUT /api/propiedades/{id} AGENTE intenta actualizar propiedad de OTRO AGENTE (403 Forbidden)")
    void test7_actualizarPropiedadOtroAgente403() throws Exception {
        // Crear propiedad con agente1
        PropiedadCreateDTO createDto = new PropiedadCreateDTO();
        createDto.setTitulo("Propiedad de Agente 1");
        createDto.setOperacion("Alquiler");
        createDto.setTipo("Oficina");
        createDto.setPrecio(new BigDecimal("2500.00"));
        createDto.setDistritoId(distritoPrueba.getId());

        MvcResult createResult = mockMvc.perform(post("/api/propiedades")
                        .header("Authorization", "Bearer " + tokenAgente1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Integer propId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asInt();

        // Agente 2 intenta actualizar la propiedad de Agente 1
        PropiedadUpdateDTO updateDto = new PropiedadUpdateDTO();
        updateDto.setTitulo("Intento de modificación ilícita");
        updateDto.setOperacion("Alquiler");
        updateDto.setTipo("Oficina");
        updateDto.setPrecio(new BigDecimal("1000.00"));
        updateDto.setDistritoId(distritoPrueba.getId());

        mockMvc.perform(put("/api/propiedades/" + propId)
                        .header("Authorization", "Bearer " + tokenAgente2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("PROPIEDADES 8: PUT /api/propiedades/{id} ADMIN puede actualizar propiedad de cualquier agente (200 OK)")
    void test8_adminActualizaCualquierPropiedad200() throws Exception {
        // Crear propiedad con agente1
        PropiedadCreateDTO createDto = new PropiedadCreateDTO();
        createDto.setTitulo("Propiedad administrada");
        createDto.setOperacion("Venta");
        createDto.setTipo("Terreno");
        createDto.setPrecio(new BigDecimal("300000.00"));
        createDto.setDistritoId(distritoPrueba.getId());

        MvcResult createResult = mockMvc.perform(post("/api/propiedades")
                        .header("Authorization", "Bearer " + tokenAgente1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Integer propId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asInt();

        // ADMIN actualiza
        PropiedadUpdateDTO updateDto = new PropiedadUpdateDTO();
        updateDto.setTitulo("Terreno Verificado por Admin");
        updateDto.setOperacion("Venta");
        updateDto.setTipo("Terreno");
        updateDto.setPrecio(new BigDecimal("320000.00"));
        updateDto.setDistritoId(distritoPrueba.getId());

        mockMvc.perform(put("/api/propiedades/" + propId)
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Terreno Verificado por Admin"));
    }

    @Test
    @DisplayName("PROPIEDADES 9: PATCH /api/propiedades/{id}/estado cambia estado (200 OK)")
    void test9_cambiarEstadoPropiedad200() throws Exception {
        // Crear propiedad
        PropiedadCreateDTO createDto = new PropiedadCreateDTO();
        createDto.setTitulo("Departamento en Venta");
        createDto.setOperacion("Venta");
        createDto.setTipo("Departamento");
        createDto.setPrecio(new BigDecimal("150000.00"));
        createDto.setDistritoId(distritoPrueba.getId());

        MvcResult createResult = mockMvc.perform(post("/api/propiedades")
                        .header("Authorization", "Bearer " + tokenAgente1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Integer propId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asInt();

        // Cambiar estado a Reservado
        PropiedadEstadoDTO estadoDto = new PropiedadEstadoDTO("Reservado");

        mockMvc.perform(patch("/api/propiedades/" + propId + "/estado")
                        .header("Authorization", "Bearer " + tokenAgente1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estadoDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Reservado"));
    }

    @Test
    @DisplayName("PROPIEDADES 10: DELETE /api/propiedades/{id} AGENTE no puede eliminar (403 Forbidden)")
    void test10_eliminarPropiedadComoAgente403() throws Exception {
        // Crear propiedad
        PropiedadCreateDTO createDto = new PropiedadCreateDTO();
        createDto.setTitulo("Inmueble Test Delete");
        createDto.setOperacion("Venta");
        createDto.setTipo("Casa");
        createDto.setPrecio(new BigDecimal("99000.00"));
        createDto.setDistritoId(distritoPrueba.getId());

        MvcResult createResult = mockMvc.perform(post("/api/propiedades")
                        .header("Authorization", "Bearer " + tokenAgente1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Integer propId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asInt();

        mockMvc.perform(delete("/api/propiedades/" + propId)
                        .header("Authorization", "Bearer " + tokenAgente1))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PROPIEDADES 11: DELETE /api/propiedades/{id} ADMIN realiza desactivación lógica (204 No Content)")
    void test11_desactivarPropiedadComoAdmin204() throws Exception {
        // Crear propiedad
        PropiedadCreateDTO createDto = new PropiedadCreateDTO();
        createDto.setTitulo("Inmueble para desactivar");
        createDto.setOperacion("Venta");
        createDto.setTipo("Casa");
        createDto.setPrecio(new BigDecimal("110000.00"));
        createDto.setDistritoId(distritoPrueba.getId());

        MvcResult createResult = mockMvc.perform(post("/api/propiedades")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Integer propId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asInt();

        mockMvc.perform(delete("/api/propiedades/" + propId)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());

        // Pausa para sincronización binlog hacia la réplica en Docker
        Thread.sleep(100);

        // Verificar en BD que sigue existiendo pero con activo = false y estado = 'Inactivo'
        Propiedad enBd = propiedadRepository.findById(propId).orElseThrow();
        assertFalse(enBd.getActivo(), "El campo activo debe ser false tras eliminación lógica");
        assertEquals("Inactivo", enBd.getEstado(), "El estado debe ser 'Inactivo'");
    }

    @Test
    @DisplayName("PROPIEDADES 12: Gestión de Imágenes (POST, PATCH principal, DELETE)")
    void test12_gestionImagenesPropiedad() throws Exception {
        // 1. Crear propiedad
        PropiedadCreateDTO createDto = new PropiedadCreateDTO();
        createDto.setTitulo("Casa con Galería");
        createDto.setOperacion("Venta");
        createDto.setTipo("Casa");
        createDto.setPrecio(new BigDecimal("210000.00"));
        createDto.setDistritoId(distritoPrueba.getId());

        MvcResult createResult = mockMvc.perform(post("/api/propiedades")
                        .header("Authorization", "Bearer " + tokenAgente1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Integer propId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asInt();

        // 2. Agregar imagen
        PropiedadImagenCreateDTO imgDto = new PropiedadImagenCreateDTO("https://example.com/fachada.jpg", 1, true);

        MvcResult imgResult = mockMvc.perform(post("/api/propiedades/" + propId + "/imagenes")
                        .header("Authorization", "Bearer " + tokenAgente1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(imgDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value("https://example.com/fachada.jpg"))
                .andExpect(jsonPath("$.esPrincipal").value(true))
                .andReturn();

        Integer imgId = objectMapper.readTree(imgResult.getResponse().getContentAsString()).get("id").asInt();

        // 3. Agregar segunda imagen
        PropiedadImagenCreateDTO imgDto2 = new PropiedadImagenCreateDTO("https://example.com/interior.jpg", 2, false);
        MvcResult imgResult2 = mockMvc.perform(post("/api/propiedades/" + propId + "/imagenes")
                        .header("Authorization", "Bearer " + tokenAgente1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(imgDto2)))
                .andExpect(status().isCreated())
                .andReturn();

        Integer imgId2 = objectMapper.readTree(imgResult2.getResponse().getContentAsString()).get("id").asInt();

        // 4. Marcar segunda imagen como principal
        mockMvc.perform(patch("/api/propiedades/" + propId + "/imagenes/" + imgId2 + "/principal")
                        .header("Authorization", "Bearer " + tokenAgente1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.esPrincipal").value(true));

        // 5. Eliminar primera imagen
        mockMvc.perform(delete("/api/propiedades/" + propId + "/imagenes/" + imgId)
                        .header("Authorization", "Bearer " + tokenAgente1))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PROPIEDADES 13: Filtros de búsqueda (operacion, tipo, precio)")
    void test13_filtrosPropiedades() throws Exception {
        mockMvc.perform(get("/api/propiedades")
                        .param("operacion", "Venta")
                        .param("tipo", "Casa")
                        .param("precioMin", "50000")
                        .param("precioMax", "500000")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(5));
    }
}
