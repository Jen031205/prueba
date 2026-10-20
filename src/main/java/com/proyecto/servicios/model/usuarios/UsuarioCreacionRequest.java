package com.proyecto.servicios.model.usuarios;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos para el registro de un nuevo usuario")
public class UsuarioCreacionRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "El nombre solo debe contener letras y espacios")
    @Schema(description = "Nombre completo del usuario", example = "Maria Fernandez")
    private String nombre;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico es inválido")
    @Size(max = 100, message = "El correo no debe superar los 100 caracteres")
    @Schema(description = "Correo electrónico único del usuario", example = "maria.fernandez@example.com")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener al menos 8 caracteres")
    @Schema(description = "Contraseña segura del usuario", example = "Password123!")
    private String password;

    public UsuarioCreacionRequest() {
    }

    public UsuarioCreacionRequest(String nombre, String correo, String password) {
        this.nombre = nombre;
        this.correo = correo;
        this.password = password;
    }

    public static UsuarioCreacionRequestBuilder builder() {
        return new UsuarioCreacionRequestBuilder();
    }

    public static class UsuarioCreacionRequestBuilder {
        private String nombre;
        private String correo;
        private String password;

        public UsuarioCreacionRequestBuilder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public UsuarioCreacionRequestBuilder correo(String correo) {
            this.correo = correo;
            return this;
        }

        public UsuarioCreacionRequestBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UsuarioCreacionRequest build() {
            return new UsuarioCreacionRequest(nombre, correo, password);
        }
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
}
