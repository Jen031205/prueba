package com.proyecto.servicios.model.clientes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DomicilioRequest {
    @NotBlank(message = "La calle es obligatoria")
    @Size(max = 100, message = "La calle no debe superar los 100 caracteres")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String calle;

    @NotBlank(message = "El número exterior es obligatorio")
    @Size(max = 15, message = "El número exterior no debe superar los 15 caracteres")
    @Pattern(regexp = "^\\d{1,15}$", message = "En campos numéricos solo debe de ingresar números (el número exterior solo debe contener dígitos)")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String numeroExterior;

    @Size(max = 15, message = "El número interior no debe superar los 15 caracteres")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String numeroInterior;

    @NotBlank(message = "La colonia es obligatoria")
    @Size(max = 80, message = "La colonia no debe superar los 80 caracteres")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String colonia;

    @NotBlank(message = "El municipio es obligatorio")
    @Size(max = 80, message = "El municipio no debe superar los 80 caracteres")
    @Pattern(regexp = "^[\\p{L}]+(?: [\\p{L}]+)*$", message = "En campo de texto solo debe de ingresar texto y no caracteres especiales para municipio")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String municipio;

    @NotBlank(message = "El estado es obligatorio")
    @Size(max = 80, message = "El estado no debe superar los 80 caracteres")
    @Pattern(regexp = "^[\\p{L}]+(?: [\\p{L}]+)*$", message = "En campo de texto solo debe de ingresar texto y no caracteres especiales para estado")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String estado;

    @NotBlank(message = "El código postal es obligatorio")
    @Pattern(regexp = "^\\d{5}$", message = "En campos numéricos solo debe de ingresar números (el código postal debe contener exactamente 5 dígitos)")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String codigoPostal;

    @NotBlank(message = "El país es obligatorio")
    @Size(max = 60, message = "El país no debe superar los 60 caracteres")
    @Pattern(regexp = "^[\\p{L}]+(?: [\\p{L}]+)*$", message = "En campo de texto solo debe de ingresar texto y no caracteres especiales para país")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String pais;
}