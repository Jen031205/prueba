package com.proyecto.servicios.model.auth;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SesionEstadoResponse {
    private boolean activa;
    private String correo;
    private LocalDateTime fechaInicio;
    private LocalDateTime ultimaActividad;
    private LocalDateTime fechaExpiracion;
    private long segundosRestantes;
    private String alerta;
    private String mensaje;

    public SesionEstadoResponse() {
    }

    public SesionEstadoResponse(boolean activa, String correo, LocalDateTime fechaInicio, LocalDateTime ultimaActividad,
                                LocalDateTime fechaExpiracion, long segundosRestantes, String alerta, String mensaje) {
        this.activa = activa;
        this.correo = correo;
        this.fechaInicio = fechaInicio;
        this.ultimaActividad = ultimaActividad;
        this.fechaExpiracion = fechaExpiracion;
        this.segundosRestantes = segundosRestantes;
        this.alerta = alerta;
        this.mensaje = mensaje;
    }

    public static SesionEstadoResponseBuilder builder() {
        return new SesionEstadoResponseBuilder();
    }

    public static class SesionEstadoResponseBuilder {
        private boolean activa;
        private String correo;
        private LocalDateTime fechaInicio;
        private LocalDateTime ultimaActividad;
        private LocalDateTime fechaExpiracion;
        private long segundosRestantes;
        private String alerta;
        private String mensaje;

        public SesionEstadoResponseBuilder activa(boolean activa) {
            this.activa = activa;
            return this;
        }

        public SesionEstadoResponseBuilder correo(String correo) {
            this.correo = correo;
            return this;
        }

        public SesionEstadoResponseBuilder fechaInicio(LocalDateTime fechaInicio) {
            this.fechaInicio = fechaInicio;
            return this;
        }

        public SesionEstadoResponseBuilder ultimaActividad(LocalDateTime ultimaActividad) {
            this.ultimaActividad = ultimaActividad;
            return this;
        }

        public SesionEstadoResponseBuilder fechaExpiracion(LocalDateTime fechaExpiracion) {
            this.fechaExpiracion = fechaExpiracion;
            return this;
        }

        public SesionEstadoResponseBuilder segundosRestantes(long segundosRestantes) {
            this.segundosRestantes = segundosRestantes;
            return this;
        }

        public SesionEstadoResponseBuilder alerta(String alerta) {
            this.alerta = alerta;
            return this;
        }

        public SesionEstadoResponseBuilder mensaje(String mensaje) {
            this.mensaje = mensaje;
            return this;
        }

        public SesionEstadoResponse build() {
            return new SesionEstadoResponse(activa, correo, fechaInicio, ultimaActividad, fechaExpiracion, segundosRestantes, alerta, mensaje);
        }
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getUltimaActividad() {
        return ultimaActividad;
    }

    public void setUltimaActividad(LocalDateTime ultimaActividad) {
        this.ultimaActividad = ultimaActividad;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public long getSegundosRestantes() {
        return segundosRestantes;
    }

    public void setSegundosRestantes(long segundosRestantes) {
        this.segundosRestantes = segundosRestantes;
    }

    public String getAlerta() {
        return alerta;
    }

    public void setAlerta(String alerta) {
        this.alerta = alerta;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
