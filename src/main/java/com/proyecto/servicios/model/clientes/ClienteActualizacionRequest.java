package com.proyecto.servicios.model.clientes;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos para actualizar un cliente existente. Se localiza por CURP, RFC, número de cuenta o correo sin requerir id.")
public class ClienteActualizacionRequest extends ClienteDatosRequest {

    @Schema(description = "CURP del cliente para identificar el registro a actualizar", example = "GALA900101MDFRNN09")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String curp;

    @Schema(description = "RFC del cliente para identificar el registro a actualizar (opcional)", example = "GALA900101ABC")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String rfc;

    @Schema(description = "Número de cuenta bancaria del cliente para identificar el registro a actualizar (opcional)", example = "4100000000000001")
    @JsonAlias("cuenta")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String numeroCuenta;
}