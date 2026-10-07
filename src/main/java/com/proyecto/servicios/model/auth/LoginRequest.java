package com.proyecto.servicios.model.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico debe tener un formato válido")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String password;
}
