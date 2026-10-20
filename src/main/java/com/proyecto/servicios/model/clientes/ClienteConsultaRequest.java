package com.proyecto.servicios.model.clientes;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Criterios para consultar un cliente específico sin ingresar id (por CURP, RFC, correo electrónico o número de cuenta)")
public class ClienteConsultaRequest {

    @Schema(description = "CURP del cliente", example = "GALA900101MDFRNN09")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String curp;

    @Schema(description = "RFC del cliente", example = "GALA900101ABC")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String rfc;

    @Schema(description = "Correo electrónico del cliente", example = "ana@example.com")
    @JsonAlias("correo")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String correoElectronico;

    @Schema(description = "Número de cuenta bancaria del cliente", example = "4100000000000001")
    @JsonAlias("cuenta")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String numeroCuenta;

    public ClienteConsultaRequest() {
    }

    public ClienteConsultaRequest(String curp, String rfc, String correoElectronico, String numeroCuenta) {
        this.curp = curp;
        this.rfc = rfc;
        this.correoElectronico = correoElectronico;
        this.numeroCuenta = numeroCuenta;
    }
}
