package com.proyecto.servicios.model.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroUsuarioRequest {
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico debe tener un formato válido")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 50, message = "La contraseña debe tener al menos 6 caracteres")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String password;

    @NotBlank(message = "El nombre es obligatorio")
    @Pattern(regexp = "^[\\p{L}]+(?: [\\p{L}]+)*$", message = "El nombre solo debe contener texto (sin números ni caracteres especiales)")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String nombre;
}
