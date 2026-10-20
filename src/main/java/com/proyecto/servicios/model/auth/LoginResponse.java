package com.proyecto.servicios.model.auth;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LoginResponse {
    private String token;
    private String correo;
    private String nombre;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaExpiracion;
    private int tiempoInactividadMinutos;
    private String mensaje;

    public LoginResponse() {
    }

    public LoginResponse(String token, String correo, String nombre, LocalDateTime fechaInicio,
                         LocalDateTime fechaExpiracion, int tiempoInactividadMinutos, String mensaje) {
        this.token = token;
        this.correo = correo;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaExpiracion = fechaExpiracion;
        this.tiempoInactividadMinutos = tiempoInactividadMinutos;
        this.mensaje = mensaje;
    }

    public static LoginResponseBuilder builder() {
        return new LoginResponseBuilder();
    }

    public static class LoginResponseBuilder {
        private String token;
        private String correo;
        private String nombre;
        private LocalDateTime fechaInicio;
        private LocalDateTime fechaExpiracion;
        private int tiempoInactividadMinutos;
        private String mensaje;

        public LoginResponseBuilder token(String token) {
            this.token = token;
            return this;
        }

        public LoginResponseBuilder correo(String correo) {
            this.correo = correo;
            return this;
        }

        public LoginResponseBuilder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public LoginResponseBuilder fechaInicio(LocalDateTime fechaInicio) {
            this.fechaInicio = fechaInicio;
            return this;
        }

        public LoginResponseBuilder fechaExpiracion(LocalDateTime fechaExpiracion) {
            this.fechaExpiracion = fechaExpiracion;
            return this;
        }

        public LoginResponseBuilder tiempoInactividadMinutos(int tiempoInactividadMinutos) {
            this.tiempoInactividadMinutos = tiempoInactividadMinutos;
            return this;
        }

        public LoginResponseBuilder mensaje(String mensaje) {
            this.mensaje = mensaje;
            return this;
        }

        public LoginResponse build() {
            return new LoginResponse(token, correo, nombre, fechaInicio, fechaExpiracion, tiempoInactividadMinutos, mensaje);
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public int getTiempoInactividadMinutos() {
        return tiempoInactividadMinutos;
    }

    public void setTiempoInactividadMinutos(int tiempoInactividadMinutos) {
        this.tiempoInactividadMinutos = tiempoInactividadMinutos;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
