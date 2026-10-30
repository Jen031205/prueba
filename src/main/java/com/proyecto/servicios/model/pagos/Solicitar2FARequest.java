package com.proyecto.servicios.model.pagos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Solicitar2FARequest {

    @NotNull(message = "El id del cliente es obligatorio")
    @JsonAlias({"clienteId", "id_cliente", "cliente_id"})
    private Long idCliente;

    @JsonAlias({"numCuenta", "numeroCuenta", "num_cuenta", "numero_cuenta"})
    private String numCuenta;
}
