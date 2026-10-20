package com.proyecto.servicios.model.usuarios;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Criterios combinables para la búsqueda y consulta dinámica de usuarios")
public class UsuarioBusquedaRequest {

    @Schema(description = "Filtrar por identificador único exacto", example = "1")
    private Long id;

    @Schema(description = "Filtrar por correo electrónico (coincidencia parcial o exacta, case-insensitive)", example = "admin@banco.com")
    private String correo;

    @Schema(description = "Filtrar por nombre (coincidencia parcial, case-insensitive)", example = "Juan")
    private String nombre;

    @Schema(description = "Filtrar por estatus activo del usuario", example = "true")
    private Boolean activo;

    @Schema(description = "Filtrar por fecha de creación a partir de", example = "2026-01-01T00:00:00")
    private LocalDateTime fechaCreacionDesde;

    @Schema(description = "Filtrar por fecha de creación hasta", example = "2026-12-31T23:59:59")
    private LocalDateTime fechaCreacionHasta;

    public UsuarioBusquedaRequest() {
    }

    public UsuarioBusquedaRequest(Long id, String correo, String nombre, Boolean activo,
                                  LocalDateTime fechaCreacionDesde, LocalDateTime fechaCreacionHasta) {
        this.id = id;
        this.correo = correo;
        this.nombre = nombre;
        this.activo = activo;
        this.fechaCreacionDesde = fechaCreacionDesde;
        this.fechaCreacionHasta = fechaCreacionHasta;
    }

    public static UsuarioBusquedaRequestBuilder builder() {
        return new UsuarioBusquedaRequestBuilder();
    }

    public static class UsuarioBusquedaRequestBuilder {
        private Long id;
        private String correo;
        private String nombre;
        private Boolean activo;
        private LocalDateTime fechaCreacionDesde;
        private LocalDateTime fechaCreacionHasta;

        public UsuarioBusquedaRequestBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UsuarioBusquedaRequestBuilder correo(String correo) {
            this.correo = correo;
            return this;
        }

        public UsuarioBusquedaRequestBuilder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public UsuarioBusquedaRequestBuilder activo(Boolean activo) {
            this.activo = activo;
            return this;
        }

        public UsuarioBusquedaRequestBuilder fechaCreacionDesde(LocalDateTime fechaCreacionDesde) {
            this.fechaCreacionDesde = fechaCreacionDesde;
            return this;
        }

        public UsuarioBusquedaRequestBuilder fechaCreacionHasta(LocalDateTime fechaCreacionHasta) {
            this.fechaCreacionHasta = fechaCreacionHasta;
            return this;
        }

        public UsuarioBusquedaRequest build() {
            return new UsuarioBusquedaRequest(id, correo, nombre, activo, fechaCreacionDesde, fechaCreacionHasta);
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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacionDesde() {
        return fechaCreacionDesde;
    }

    public void setFechaCreacionDesde(LocalDateTime fechaCreacionDesde) {
        this.fechaCreacionDesde = fechaCreacionDesde;
    }

    public LocalDateTime getFechaCreacionHasta() {
        return fechaCreacionHasta;
    }

    public void setFechaCreacionHasta(LocalDateTime fechaCreacionHasta) {
        this.fechaCreacionHasta = fechaCreacionHasta;
    }
}
