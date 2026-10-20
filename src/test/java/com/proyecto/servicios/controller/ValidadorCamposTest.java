package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.GenericResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorCamposTest {

    private final ValidadorCampos validadorCampos = new ValidadorCampos();

    @Test
    void dtoValidationRetornaCodigo1YMensajeConErrores() {
        Object target = new Object();
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(target, "personasRequest");
        errors.addError(new FieldError("personasRequest", "nombre", "El nombre es obligatorio"));
        errors.addError(new FieldError("personasRequest", "correo", "El formato es invalido"));

        ResponseEntity<GenericResponse> response = validadorCampos.dtoValidation(errors);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getCodigo());
        assertTrue(response.getBody().getMensaje().contains("Datos no validos"));
        assertTrue(response.getBody().getMensaje().contains("nombre=El nombre es obligatorio"));
        assertTrue(response.getBody().getMensaje().contains("correo=El formato es invalido"));
    }
}
