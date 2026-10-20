package com.proyecto.servicios.model.clientes;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "Criterios combinables para la búsqueda y filtrado dinámico de clientes sin ingresar id")
public class ClienteBusquedaRequest {

    @Schema(description = "Filtrar por nombre o apellidos (coincidencia parcial)", example = "Ana")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String nombre;

    @Schema(description = "Filtrar por CURP (coincidencia parcial o exacta)", example = "GALA900101MDFRNN09")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String curp;

    @Schema(description = "Filtrar por RFC (coincidencia parcial o exacta)", example = "GALA900101ABC")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String rfc;

    @Schema(description = "Filtrar por correo electrónico (coincidencia parcial)", example = "ana@example.com")
    @JsonAlias("correo")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String correoElectronico;

    @Schema(description = "Filtrar por estatus activo del cliente", example = "true")
    private Boolean activo;

    @Schema(description = "Filtrar por número de cuenta bancaria", example = "4100000000000001")
    @JsonAlias("cuenta")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String numeroCuenta;

    @Schema(description = "Fecha de registro inicial (AAAA-MM-DD)", example = "2026-01-01")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd")
    @JsonAlias("fechaRegistroDesde")
    private LocalDate desde;

    @Schema(description = "Fecha de registro final (AAAA-MM-DD)", example = "2026-12-31")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd")
    @JsonAlias("fechaRegistroHasta")
    private LocalDate hasta;

    public ClienteBusquedaRequest() {
    }

    public ClienteBusquedaRequest(String nombre, String curp, String rfc, String correoElectronico,
                                  Boolean activo, String numeroCuenta, LocalDate desde, LocalDate hasta) {
        this.nombre = nombre;
        this.curp = curp;
        this.rfc = rfc;
        this.correoElectronico = correoElectronico;
        this.activo = activo;
        this.numeroCuenta = numeroCuenta;
        this.desde = desde;
        this.hasta = hasta;
    }
}
