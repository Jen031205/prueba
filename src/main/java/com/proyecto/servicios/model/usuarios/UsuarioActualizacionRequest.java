package com.proyecto.servicios.model.usuarios;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos para la actualización de un usuario existente")
public class UsuarioActualizacionRequest {

    @NotNull(message = "El identificador (id) de usuario es obligatorio")
    @Schema(description = "Identificador único del usuario a actualizar", example = "1")
    private Long id;

    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "El nombre solo debe contener letras y espacios")
    @Schema(description = "Nombre actualizado del usuario", example = "Maria Fernandez Gomez")
    private String nombre;

    @Email(message = "El formato del correo electrónico es inválido")
    @Size(max = 100, message = "El correo no debe superar los 100 caracteres")
    @Schema(description = "Nuevo correo electrónico del usuario", example = "maria.nueva@example.com")
    private String correo;

    @Size(min = 8, max = 100, message = "La contraseña debe tener al menos 8 caracteres")
    @Schema(description = "Nueva contraseña del usuario", example = "NuevaPass123!")
    private String password;

    @Schema(description = "Estatus activo del usuario", example = "true")
    private Boolean activo;

    public UsuarioActualizacionRequest() {
    }

    public UsuarioActualizacionRequest(Long id, String nombre, String correo, String password, Boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.password = password;
        this.activo = activo;
    }

    public static UsuarioActualizacionRequestBuilder builder() {
        return new UsuarioActualizacionRequestBuilder();
    }

    public static class UsuarioActualizacionRequestBuilder {
        private Long id;
        private String nombre;
        private String correo;
        private String password;
        private Boolean activo;

        public UsuarioActualizacionRequestBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UsuarioActualizacionRequestBuilder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public UsuarioActualizacionRequestBuilder correo(String correo) {
            this.correo = correo;
            return this;
        }

        public UsuarioActualizacionRequestBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UsuarioActualizacionRequestBuilder activo(Boolean activo) {
            this.activo = activo;
            return this;
        }

        public UsuarioActualizacionRequest build() {
            return new UsuarioActualizacionRequest(id, nombre, correo, password, activo);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
