package com.proyecto.servicios.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String correo;
    private String nombre;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaExpiracion;
    private int tiempoInactividadMinutos;
    private String mensaje;
}
