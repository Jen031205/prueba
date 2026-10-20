package com.proyecto.servicios.model.usuarios;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Respuesta uniforme con los datos públicos del usuario")
public class UsuarioResponse {

    @Schema(description = "Identificador único del usuario", example = "1")
    private Long id;

    @Schema(description = "Correo electrónico del usuario", example = "admin@banco.com")
    private String correo;

    @Schema(description = "Nombre completo del usuario", example = "Administrador Sistema")
    private String nombre;

    @Schema(description = "Indica si el usuario se encuentra activo", example = "true")
    private boolean activo;

    @Schema(description = "Fecha y hora de registro del usuario", example = "2026-10-16T19:30:00")
    private LocalDateTime fechaCreacion;

    public UsuarioResponse() {
    }

    public UsuarioResponse(Long id, String correo, String nombre, boolean activo, LocalDateTime fechaCreacion) {
        this.id = id;
        this.correo = correo;
        this.nombre = nombre;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
    }

    public static UsuarioResponseBuilder builder() {
        return new UsuarioResponseBuilder();
    }

    public static class UsuarioResponseBuilder {
        private Long id;
        private String correo;
        private String nombre;
        private boolean activo;
        private LocalDateTime fechaCreacion;

        public UsuarioResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UsuarioResponseBuilder correo(String correo) {
            this.correo = correo;
            return this;
        }

        public UsuarioResponseBuilder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public UsuarioResponseBuilder activo(boolean activo) {
            this.activo = activo;
            return this;
        }

        public UsuarioResponseBuilder fechaCreacion(LocalDateTime fechaCreacion) {
            this.fechaCreacion = fechaCreacion;
            return this;
        }

        public UsuarioResponse build() {
            return new UsuarioResponse(id, correo, nombre, activo, fechaCreacion);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
