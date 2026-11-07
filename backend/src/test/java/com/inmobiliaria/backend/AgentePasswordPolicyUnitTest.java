package com.inmobiliaria.backend;

import com.inmobiliaria.backend.dto.AgenteCreateDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Política de contraseñas del alta de agentes: 8 a 72 caracteres, con letras y números, sin < ni >. */
class AgentePasswordPolicyUnitTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private boolean passwordInvalida(String password) {
        AgenteCreateDTO dto = new AgenteCreateDTO(
                "Carlos Valdivia", "carlos@inmobiliaria.com", password, "+51 912 345 678", "Asesor");
        Set<ConstraintViolation<AgenteCreateDTO>> errores = validator.validate(dto);
        return errores.stream().anyMatch(e -> "password".equals(e.getPropertyPath().toString()));
    }

    @Test
    void aceptaContrasenasConLetrasYNumeros() {
        assertFalse(passwordInvalida("password123"));
        assertFalse(passwordInvalida("Clave2026Segura"));
    }

    @Test
    void rechazaContrasenasCortas() {
        assertTrue(passwordInvalida("abc123"));
    }

    @Test
    void rechazaContrasenasSinNumeros() {
        assertTrue(passwordInvalida("soloLetrasLargas"));
    }

    @Test
    void rechazaContrasenasSinLetras() {
        assertTrue(passwordInvalida("1234567890"));
    }

    @Test
    void rechazaEtiquetasHtml() {
        assertTrue(passwordInvalida("abc<script>123"));
    }

    @Test
    void rechazaContrasenasMasLargasQueElLimiteDeBCrypt() {
        assertTrue(passwordInvalida("a1".repeat(40)));
    }
}
