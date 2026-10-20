package com.proyecto.servicios.model.usuarios;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Solicitud para desactivar o dar de baja a un usuario")
public class UsuarioDesactivarRequest {

    @NotNull(message = "El identificador (id) de usuario es obligatorio")
    @Schema(description = "Identificador único del usuario", example = "1")
    private Long id;

    public UsuarioDesactivarRequest() {
    }

    public UsuarioDesactivarRequest(Long id) {
        this.id = id;
    }

    public static UsuarioDesactivarRequestBuilder builder() {
        return new UsuarioDesactivarRequestBuilder();
    }

    public static class UsuarioDesactivarRequestBuilder {
        private Long id;

        public UsuarioDesactivarRequestBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UsuarioDesactivarRequest build() {
            return new UsuarioDesactivarRequest(id);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
