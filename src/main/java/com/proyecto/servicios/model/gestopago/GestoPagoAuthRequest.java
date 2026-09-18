package com.proyecto.servicios.model.gestopago;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GestoPagoAuthRequest {

    // Identificador del distribuidor para la autenticacion.
    @NotNull(message = "idDistribuidor es obligatorio")
    private Integer idDistribuidor;

    // Codigo del dispositivo asociado al distribuido.
    @NotBlank(message = "codigoDispositivo es obligatorio")
    private String codigoDispositivo;

    // Password del acceso al servicio externo.
    @NotBlank(message = "password es obligatorio")
    private String password;
}
