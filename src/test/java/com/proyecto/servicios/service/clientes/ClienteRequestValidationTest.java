package com.proyecto.servicios.service.clientes;

import com.proyecto.servicios.model.clientes.ClienteRequest;
import com.proyecto.servicios.model.clientes.DomicilioRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClienteRequestValidationTest {
    private static Validator validator;

    @BeforeAll
    static void crearValidador() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void aceptaSolicitudValidaDePersonaMayorDeEdad() {
        Set<ConstraintViolation<ClienteRequest>> errores = validator.validate(solicitudValida());
        assertTrue(errores.isEmpty(), "La solicitud válida no debería tener errores: " + errores);
    }

    @Test
    void aceptaRfcDe12Y13Caracteres() {
        ClienteRequest req13 = solicitudValida();
        req13.setRfc("GALA900101ABC"); // 13 chars
        assertTrue(validator.validate(req13).isEmpty());

        ClienteRequest req12 = solicitudValida();
        req12.setRfc("GAL900101ABC"); // 12 chars
        assertTrue(validator.validate(req12).isEmpty());
    }

    @Test
    void rechazaMenorDeEdad() {
        ClienteRequest request = solicitudValida();
        request.setFechaNacimiento(LocalDate.now().minusYears(17));

        Set<ConstraintViolation<ClienteRequest>> errores = validator.validate(request);
        assertFalse(errores.isEmpty());
        assertTrue(errores.stream().anyMatch(e -> e.getPropertyPath().toString().equals("mayorDeEdad")));
    }

    @Test
    void rechazaFechaNacimientoFutura() {
        ClienteRequest request = solicitudValida();
        request.setFechaNacimiento(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<ClienteRequest>> errores = validator.validate(request);
        assertFalse(errores.isEmpty());
        assertTrue(errores.stream().anyMatch(e -> e.getPropertyPath().toString().equals("fechaNacimiento")));
    }

    @Test
    void rechazaNombreInvalidoConNumerosOCaracteresEspeciales() {
        ClienteRequest request = solicitudValida();
        request.setNombre("Ana123");

        Set<ConstraintViolation<ClienteRequest>> errores = validator.validate(request);
        assertFalse(errores.isEmpty());
        assertTrue(errores.stream().anyMatch(e -> e.getPropertyPath().toString().equals("nombre")));
    }

    @Test
    void rechazaNombreDemasiadoCorto() {
        ClienteRequest request = solicitudValida();
        request.setNombre("A");

        Set<ConstraintViolation<ClienteRequest>> errores = validator.validate(request);
        assertFalse(errores.isEmpty());
        assertTrue(errores.stream().anyMatch(e -> e.getPropertyPath().toString().equals("nombre")));
    }

    @Test
    void rechazaCurpInvalida() {
        ClienteRequest request = solicitudValida();
        request.setCurp("CURP_INVALIDA_123");

        Set<ConstraintViolation<ClienteRequest>> errores = validator.validate(request);
        assertFalse(errores.isEmpty());
        assertTrue(errores.stream().anyMatch(e -> e.getPropertyPath().toString().equals("curp")));
    }

    @Test
    void rechazaRfcInvalido() {
        ClienteRequest request = solicitudValida();
        request.setRfc("RFC_INVALIDO");

        Set<ConstraintViolation<ClienteRequest>> errores = validator.validate(request);
        assertFalse(errores.isEmpty());
        assertTrue(errores.stream().anyMatch(e -> e.getPropertyPath().toString().equals("rfc")));
    }

    @Test
    void rechazaCorreoInvalido() {
        ClienteRequest request = solicitudValida();
        request.setCorreoElectronico("correo-no-valido");

        Set<ConstraintViolation<ClienteRequest>> errores = validator.validate(request);
        assertFalse(errores.isEmpty());
        assertTrue(errores.stream().anyMatch(e -> e.getPropertyPath().toString().equals("correoElectronico")));
    }

    @Test
    void rechazaTelefonoMovilConLongitudIncorrecta() {
        ClienteRequest request = solicitudValida();
        request.setTelefonoMovil("12345");

        Set<ConstraintViolation<ClienteRequest>> errores = validator.validate(request);
        assertFalse(errores.isEmpty());
        assertTrue(errores.stream().anyMatch(e -> e.getPropertyPath().toString().equals("telefonoMovil")));
    }

    @Test
    void rechazaCodigoPostalInvalido() {
        ClienteRequest request = solicitudValida();
        request.getDomicilio().setCodigoPostal("123");

        Set<ConstraintViolation<ClienteRequest>> errores = validator.validate(request);
        assertFalse(errores.isEmpty());
        assertTrue(errores.stream().anyMatch(e -> e.getPropertyPath().toString().contains("codigoPostal")));
    }

    @Test
    void rechazaIngresoMensualCeroONegativo() {
        ClienteRequest requestCero = solicitudValida();
        requestCero.setIngresoMensual(BigDecimal.ZERO);
        assertFalse(validator.validate(requestCero).isEmpty());

        ClienteRequest requestNegativo = solicitudValida();
        requestNegativo.setIngresoMensual(new BigDecimal("-100.00"));
        assertFalse(validator.validate(requestNegativo).isEmpty());
    }

    @Test
    void aceptaSolicitudConSegundoNombreOpcional() {
        ClienteRequest sinSegundoNombre = solicitudValida();
        sinSegundoNombre.setSegundoNombre(null);
        assertTrue(validator.validate(sinSegundoNombre).isEmpty());

        ClienteRequest conSegundoNombreVacio = solicitudValida();
        conSegundoNombreVacio.setSegundoNombre("");
        assertTrue(validator.validate(conSegundoNombreVacio).isEmpty());

        ClienteRequest conSegundoNombreValido = solicitudValida();
        conSegundoNombreValido.setSegundoNombre("Maria");
        assertTrue(validator.validate(conSegundoNombreValido).isEmpty());
    }

    @Test
    void rechazaIngresoMensualConMasDeDosDecimales() {
        ClienteRequest request = solicitudValida();
        request.setIngresoMensual(new BigDecimal("25000.123")); // 3 decimales

        Set<ConstraintViolation<ClienteRequest>> errores = validator.validate(request);
        assertFalse(errores.isEmpty());
        assertTrue(errores.stream().anyMatch(e -> e.getPropertyPath().toString().equals("ingresoMensual")
                && e.getMessage().contains("2 decimales")));
    }

    @Test
    void aceptaIngresoMensualConHastaDosDecimales() {
        ClienteRequest request = solicitudValida();
        request.setIngresoMensual(new BigDecimal("25000.50"));
        assertTrue(validator.validate(request).isEmpty());

        request.setIngresoMensual(new BigDecimal("25000"));
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rechazaValoresFueraDeCatalogoEnSexoYEstadoCivil() {
        ClienteRequest requestSexoInvalido = solicitudValida();
        requestSexoInvalido.setSexo("Z");
        Set<ConstraintViolation<ClienteRequest>> erroresSexo = validator.validate(requestSexoInvalido);
        assertFalse(erroresSexo.isEmpty());
        assertTrue(erroresSexo.stream().anyMatch(e -> e.getPropertyPath().toString().equals("sexo")
                && e.getMessage().contains("no es aceptado en el catálogo")));

        // Antiguo 'M' o 'H' debe ser rechazado ahora que se exige 'Masculino', 'Femenino', 'Otro'
        ClienteRequest requestSexoAbreviado = solicitudValida();
        requestSexoAbreviado.setSexo("M");
        assertFalse(validator.validate(requestSexoAbreviado).isEmpty());

        // UNION_LIBRE con guion bajo debe ser rechazado
        ClienteRequest requestUnionLibreGuion = solicitudValida();
        requestUnionLibreGuion.setEstadoCivil("UNION_LIBRE");
        assertFalse(validator.validate(requestUnionLibreGuion).isEmpty());

        // SOLTERO todo en mayúsculas debe ser rechazado ahora que se exige primera mayúscula y luego minúsculas
        ClienteRequest requestSolteroMayus = solicitudValida();
        requestSolteroMayus.setEstadoCivil("SOLTERO");
        assertFalse(validator.validate(requestSolteroMayus).isEmpty());

        // Union libre o Union Libre debe ser aceptado
        ClienteRequest requestUnionLibreValido = solicitudValida();
        requestUnionLibreValido.setEstadoCivil("Union libre");
        assertTrue(validator.validate(requestUnionLibreValido).isEmpty());

        requestUnionLibreValido.setEstadoCivil("Union Libre");
        assertTrue(validator.validate(requestUnionLibreValido).isEmpty());

        // Soltero, Casado, Divorciado, Viudo aceptados
        ClienteRequest reqCasado = solicitudValida();
        reqCasado.setEstadoCivil("Casado");
        assertTrue(validator.validate(reqCasado).isEmpty());

        ClienteRequest requestEstadoCivilInvalido = solicitudValida();
        requestEstadoCivilInvalido.setEstadoCivil("DESCONOCIDO");
        Set<ConstraintViolation<ClienteRequest>> erroresEstadoCivil = validator.validate(requestEstadoCivilInvalido);
        assertFalse(erroresEstadoCivil.isEmpty());
        assertTrue(erroresEstadoCivil.stream().anyMatch(e -> e.getPropertyPath().toString().equals("estadoCivil")
                && e.getMessage().contains("no es aceptado en el catálogo")));
    }

    @Test
    void validaCatalogoDeNacionalidad() {
        ClienteRequest reqValido = solicitudValida();
        reqValido.setNacionalidad("Mexicana");
        assertTrue(validator.validate(reqValido).isEmpty());

        ClienteRequest reqInvalido = solicitudValida();
        reqInvalido.setNacionalidad("PlanetaTierra");
        Set<ConstraintViolation<ClienteRequest>> errores = validator.validate(reqInvalido);
        assertFalse(errores.isEmpty());
        assertTrue(errores.stream().anyMatch(e -> e.getPropertyPath().toString().equals("nacionalidad")
                && e.getMessage().contains("no es aceptado en el catálogo")));
    }

    @Test
    void rechazaCamposDeTextoConCaracteresEspecialesONumeros() {
        ClienteRequest reqOcupacion = solicitudValida();
        reqOcupacion.setOcupacion("Ingeniero@TI");
        assertFalse(validator.validate(reqOcupacion).isEmpty());

        ClienteRequest reqMunicipio = solicitudValida();
        reqMunicipio.getDomicilio().setMunicipio("Cuauhtemoc#1");
        assertFalse(validator.validate(reqMunicipio).isEmpty());

        ClienteRequest reqEstado = solicitudValida();
        reqEstado.getDomicilio().setEstado("CDMX-2");
        assertFalse(validator.validate(reqEstado).isEmpty());
    }

    @Test
    void rechazaCamposNumericosConCaracteresNoNumericos() {
        ClienteRequest reqTel = solicitudValida();
        reqTel.setTelefonoMovil("551234ABCD");
        assertFalse(validator.validate(reqTel).isEmpty());

        ClienteRequest reqCp = solicitudValida();
        reqCp.getDomicilio().setCodigoPostal("0600A");
        assertFalse(validator.validate(reqCp).isEmpty());

        ClienteRequest reqNumExt = solicitudValida();
        reqNumExt.getDomicilio().setNumeroExterior("10-B");
        assertFalse(validator.validate(reqNumExt).isEmpty());
    }

    private static ClienteRequest solicitudValida() {
        ClienteRequest request = new ClienteRequest();
        request.setNombre("Ana");
        request.setApellidoPaterno("Lopez");
        request.setApellidoMaterno("Garcia");
        request.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        request.setCurp("GALA900101MDFRNN09");
        request.setRfc("GALA900101ABC");
        request.setSexo("Masculino");
        request.setNacionalidad("Mexicana");
        request.setEstadoCivil("Soltero");
        request.setCorreoElectronico("ana@example.com");
        request.setTelefonoMovil("5512345678");
        request.setOcupacion("Analista");
        request.setEmpresa("Empresa SA");
        request.setIngresoMensual(new BigDecimal("25000.00"));

        DomicilioRequest domicilio = new DomicilioRequest();
        domicilio.setCalle("Reforma");
        domicilio.setNumeroExterior("10");
        domicilio.setColonia("Centro");
        domicilio.setMunicipio("Cuauhtemoc");
        domicilio.setEstado("CDMX");
        domicilio.setCodigoPostal("06000");
        domicilio.setPais("Mexico");
        request.setDomicilio(domicilio);
        return request;
    }
}