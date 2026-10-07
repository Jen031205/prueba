package com.proyecto.servicios.controller.clientes;

import com.proyecto.servicios.model.clientes.ClienteActualizacionRequest;
import com.proyecto.servicios.model.clientes.ClientePatchRequest;
import com.proyecto.servicios.model.clientes.ClienteRequest;
import com.proyecto.servicios.model.clientes.ClienteResponse;
import com.proyecto.servicios.service.clientes.ClienteService;
import com.proyecto.servicios.service.clientes.ErrorValidacionException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "Operaciones de registro, consulta y actualización de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo cliente",
               description = "Registra un cliente persona física, su domicilio y le asigna automáticamente una cuenta bancaria activa.")
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(request));
    }

    @GetMapping
    @Operation(summary = "Consultar todos los clientes",
               description = "Retorna la lista completa de clientes registrados en el sistema.")
    public List<ClienteResponse> listar() {
        return clienteService.listar();
    }

    @GetMapping("/activos")
    @Operation(summary = "Consultar clientes activos",
               description = "Retorna únicamente los clientes con estatus activo.")
    public List<ClienteResponse> listarActivos() {
        return clienteService.listarActivos();
    }

    @GetMapping("/registrados")
    @Operation(summary = "Consultar clientes por rango de fechas",
               description = "Filtra clientes cuya fecha de registro se encuentre dentro del rango especificado (desde y hasta).")
    public List<ClienteResponse> registradosEntre(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return clienteService.registradosEntre(desde, hasta);
    }

    @GetMapping("/curp/{curp}")
    @Operation(summary = "Buscar cliente por CURP",
               description = "Retorna el cliente correspondiente a la CURP indicada.")
    public ClienteResponse porCurp(@PathVariable String curp) {
        return clienteService.buscarPorCurp(curp);
    }

    @GetMapping("/rfc/{rfc}")
    @Operation(summary = "Buscar cliente por RFC",
               description = "Retorna el cliente correspondiente al RFC indicado.")
    public ClienteResponse porRfc(@PathVariable String rfc) {
        return clienteService.buscarPorRfc(rfc);
    }

    @GetMapping({"/correo/{correo:.+}", "/correo"})
    @Operation(summary = "Buscar cliente por correo electrónico",
               description = "Retorna el cliente asociado al correo electrónico indicado (como path variable o parámetro de consulta).")
    public ClienteResponse porCorreo(
            @PathVariable(required = false) String correo,
            @RequestParam(required = false) String email) {
        String c = (correo != null && !correo.isBlank()) ? correo : email;
        if (c == null || c.isBlank()) {
            throw new ErrorValidacionException("El correo electrónico es obligatorio para realizar la búsqueda");
        }
        return clienteService.buscarPorCorreo(c);
    }

    @GetMapping("/cuenta/{numeroCuenta}")
    @Operation(summary = "Buscar cliente por número de cuenta",
               description = "Retorna el cliente titular del número de cuenta especificado.")
    public ClienteResponse porCuenta(@PathVariable String numeroCuenta) {
        return clienteService.buscarPorCuenta(numeroCuenta);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar cliente por ID",
               description = "Retorna el cliente asociado al identificador único.")
    public ClienteResponse porId(@PathVariable Long id) {
        return clienteService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar información de cliente",
               description = "Modifica datos personales, de contacto, domicilio y laborales. No permite modificar CURP, RFC ni número de cuenta.")
    public ClienteResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody ClienteActualizacionRequest request) {
        return clienteService.actualizar(id, request);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualización parcial de cliente",
               description = "Modifica uno o varios campos específicos del cliente (datos personales, de contacto, laborales, domicilio o estatus activo). Los campos no proporcionados conservan su valor actual.")
    public ClienteResponse patch(@PathVariable Long id,
                                 @Valid @RequestBody ClientePatchRequest request) {
        return clienteService.patch(id, request);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    @Operation(summary = "Desactivar cliente (Baja lógica)",
               description = "Desactiva lógicamente al cliente cambiando su estatus a inactivo y pasando sus cuentas activas a INACTIVA, sin eliminar físicamente su registro de la base de datos.")
    public ResponseEntity<java.util.Map<String, Object>> desactivar(@PathVariable Long id) {
        clienteService.desactivar(id);
        return ResponseEntity.ok(java.util.Map.of(
                "mensaje", "Cliente desactivado correctamente (baja lógica realizada)",
                "id", id,
                "activo", false
        ));
    }

    @PostMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar cliente vía POST (Baja lógica)",
               description = "Endpoint alternativo para desactivar lógicamente al cliente y sus cuentas activas.")
    public ResponseEntity<java.util.Map<String, Object>> desactivarPost(@PathVariable Long id) {
        return desactivar(id);
    }
}