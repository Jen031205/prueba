package com.proyecto.servicios.model.clientes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ClienteResponse {
    private Long id;
    private String nombre;
    private String segundoNombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private LocalDate fechaNacimiento;
    private String curp;
    private String rfc;
    private String sexo;
    private String nacionalidad;
    private String estadoCivil;
    private String correoElectronico;
    private String telefonoMovil;
    private String telefonoAlternativo;
    private String ocupacion;
    private String empresa;
    private BigDecimal ingresoMensual;
    private boolean activo;
    private LocalDateTime fechaRegistro;
    private DomicilioResponse domicilio;
    private List<CuentaResumen> cuentas;

    @Getter @Setter @NoArgsConstructor
    public static class DomicilioResponse {
        private String calle;
        private String numeroExterior;
        private String numeroInterior;
        private String colonia;
        private String municipio;
        private String estado;
        private String codigoPostal;
        private String pais;
    }

    @Getter @Setter @NoArgsConstructor
    public static class CuentaResumen {
        private String numeroCuenta;
        private BigDecimal saldo;
        private String estatus;
        private LocalDateTime fechaCreacion;
    }
}