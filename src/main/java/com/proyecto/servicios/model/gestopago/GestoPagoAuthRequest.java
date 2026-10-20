package com.proyecto.servicios.model.gestopago;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

    public GestoPagoAuthRequest() {
    }

    public GestoPagoAuthRequest(Integer idDistribuidor, String codigoDispositivo, String password) {
        this.idDistribuidor = idDistribuidor;
        this.codigoDispositivo = codigoDispositivo;
        this.password = password;
    }

    public Integer getIdDistribuidor() {
        return idDistribuidor;
    }

    public void setIdDistribuidor(Integer idDistribuidor) {
        this.idDistribuidor = idDistribuidor;
    }

    public String getCodigoDispositivo() {
        return codigoDispositivo;
    }

    public void setCodigoDispositivo(String codigoDispositivo) {
        this.codigoDispositivo = codigoDispositivo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
