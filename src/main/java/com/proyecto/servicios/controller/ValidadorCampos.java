package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.GenericResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ValidadorCampos {

    /**
     * En caso de que alguna validacion no se cumpla para algun DTO se tira MethodArgumentNotValidException
     * este metodo la cacha y regresa los mensajes de error para el cliente, con los message de cada campo.
     * @param errors
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse> dtoValidation(MethodArgumentNotValidException errors) {
        return processErrors(errors.getBindingResult());
    }

    public ResponseEntity<GenericResponse> dtoValidation(Errors errors) {
        return processErrors(errors);
    }

    private ResponseEntity<GenericResponse> processErrors(Errors errors) {
        GenericResponse genericResponse = new GenericResponse();
        Map<String, String> responseError = new HashMap<>();

        for (FieldError fieldError : errors.getFieldErrors()) {
            responseError.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        genericResponse.setCodigo(1);
        genericResponse.setMensaje("Datos no validos: " + responseError);
        genericResponse.setResponse(responseError);

        return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
    }
}
