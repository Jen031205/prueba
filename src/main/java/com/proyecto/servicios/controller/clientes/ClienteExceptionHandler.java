package com.proyecto.servicios.controller.clientes;

import com.proyecto.servicios.service.auth.CredencialesInvalidasException;
import com.proyecto.servicios.service.auth.SesionExpiradaException;
import com.proyecto.servicios.service.auth.SesionInvalidaException;
import com.proyecto.servicios.service.clientes.BiometriaNoEncontradaException;
import com.proyecto.servicios.service.clientes.ClienteNoEncontradoException;
import com.proyecto.servicios.service.clientes.ClienteYaRegistradoException;
import com.proyecto.servicios.service.clientes.CuentaNoEncontradaException;
import com.proyecto.servicios.service.clientes.ErrorValidacionException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.proyecto.servicios.controller")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ClienteExceptionHandler {

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, Object>> credencialesInvalidas(CredencialesInvalidasException exception) {
        return respuesta(HttpStatus.UNAUTHORIZED, exception.getMessage(), "Alerta: El correo y la contraseña son distintos a los registrados.");
    }

    @ExceptionHandler(SesionExpiradaException.class)
    public ResponseEntity<Map<String, Object>> sesionExpirada(SesionExpiradaException exception) {
        return respuesta(HttpStatus.UNAUTHORIZED, exception.getMessage(), "Alerta: Sesión cerrada automáticamente por inactividad tras 5 minutos.");
    }

    @ExceptionHandler(SesionInvalidaException.class)
    public ResponseEntity<Map<String, Object>> sesionInvalida(SesionInvalidaException exception) {
        return respuesta(HttpStatus.UNAUTHORIZED, exception.getMessage(), "Alerta: Token de sesión inválido o inexistente.");
    }

    @ExceptionHandler(ClienteYaRegistradoException.class)
    public ResponseEntity<Map<String, Object>> clienteYaRegistrado(ClienteYaRegistradoException exception) {
        return respuesta(HttpStatus.CONFLICT, exception.getMessage(), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> integridad(DataIntegrityViolationException exception) {
        return respuesta(HttpStatus.CONFLICT, "Los datos infringen una restricción de unicidad o integridad referencial", null);
    }

    @ExceptionHandler({ClienteNoEncontradoException.class, CuentaNoEncontradaException.class, BiometriaNoEncontradaException.class})
    public ResponseEntity<Map<String, Object>> noEncontrado(RuntimeException exception) {
        return respuesta(HttpStatus.NOT_FOUND, exception.getMessage(), null);
    }

    @ExceptionHandler(ErrorValidacionException.class)
    public ResponseEntity<Map<String, Object>> errorValidacion(ErrorValidacionException exception) {
        return respuesta(HttpStatus.BAD_REQUEST, exception.getMessage(), null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> argumentoInvalido(IllegalArgumentException exception) {
        return respuesta(HttpStatus.BAD_REQUEST, exception.getMessage(), exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> mensajeNoLegible(HttpMessageNotReadableException exception) {
        Throwable root = exception.getMostSpecificCause();
        String causeMsg = (root != null && root.getMessage() != null) ? root.getMessage() :
                (exception.getMessage() != null ? exception.getMessage() : "");
        String mensajeAlerta;

        if (causeMsg.contains("comillas")) {
            mensajeAlerta = causeMsg;
        } else if (causeMsg.contains("LocalDate") || causeMsg.contains("java.time") || causeMsg.contains("DateTimeParseException")) {
            mensajeAlerta = "Alerta: En el formato de las fechas solo debe permitir AAAA-MM-DD (ejemplo: 1995-12-31)";
        } else if (causeMsg.contains("BigDecimal") || causeMsg.contains("Double") || causeMsg.contains("Integer") || causeMsg.contains("NumberFormatException")) {
            mensajeAlerta = "Alerta: En campos numéricos solo debe de ingresar números";
        } else {
            mensajeAlerta = "Alerta: Formato de solicitud inválido o tipo de dato incompatible";
        }

        return respuesta(HttpStatus.BAD_REQUEST, mensajeAlerta, mensajeAlerta);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> tipoIncompatible(MethodArgumentTypeMismatchException exception) {
        String mensaje;
        if (LocalDate.class.equals(exception.getRequiredType())) {
            mensaje = "Alerta: En el formato de las fechas solo debe permitir AAAA-MM-DD (ejemplo: 2026-10-02)";
        } else {
            mensaje = "Alerta: Parámetro '" + exception.getName() + "' tiene un tipo de dato inválido";
        }
        return respuesta(HttpStatus.BAD_REQUEST, mensaje, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacionCampos(MethodArgumentNotValidException exception) {
        List<Map<String, String>> errores = exception.getBindingResult().getFieldErrors().stream()
                .map(this::mapFieldError)
                .toList();

        String mensaje = errores.isEmpty() ? "Error de validación en la solicitud" :
                errores.get(0).get("campo") + ": " + errores.get(0).get("error");

        String primeraAlerta = errores.isEmpty() ? null : errores.get(0).get("error");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put("alerta", primeraAlerta);
        body.put("message", mensaje);
        body.put("detalles", errores);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private Map<String, String> mapFieldError(FieldError fieldError) {
        Map<String, String> errorMap = new LinkedHashMap<>();
        errorMap.put("campo", fieldError.getField());
        errorMap.put("error", fieldError.getDefaultMessage());
        return errorMap;
    }

    private ResponseEntity<Map<String, Object>> respuesta(HttpStatus status, String mensaje, String alerta) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        if (alerta != null) {
            body.put("alerta", alerta);
        }
        body.put("message", mensaje);
        return ResponseEntity.status(status).body(body);
    }
}