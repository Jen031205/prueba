package com.proyecto.servicios.controller.clientes;

import com.proyecto.servicios.model.clientes.ClienteActualizacionRequest;
import com.proyecto.servicios.model.clientes.ClienteBusquedaRequest;
import com.proyecto.servicios.model.clientes.ClienteConsultaRequest;
import com.proyecto.servicios.model.clientes.ClientePatchRequest;
import com.proyecto.servicios.model.clientes.ClienteRequest;
import com.proyecto.servicios.model.clientes.ClienteResponse;
import com.proyecto.servicios.service.clientes.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/clientes")
@Tag(name = "cliente-controller", description = "Operaciones de administración, consulta y actualización de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    @Operation(summary = "Consultar todos los clientes",
               description = "Retorna la lista completa de clientes registrados en el sistema.")
    public ResponseEntity<List<ClienteResponse>> listar() {
        return ResponseEntity.ok(clienteService.listar());
    }

    @PutMapping
    @Operation(summary = "Actualizar información de cliente",
               description = "Modifica los datos del cliente identificado mediante curp, rfc, numeroCuenta o correo sin requerir id.")
    public ResponseEntity<ClienteResponse> actualizar(@Valid @RequestBody ClienteActualizacionRequest request) {
        return ResponseEntity.ok(clienteService.actualizar(request));
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo cliente",
               description = "Registra un cliente persona física, su domicilio y le asigna automáticamente una cuenta bancaria activa.")
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(request));
    }

    @PatchMapping
    @Operation(summary = "Actualización parcial de cliente",
               description = "Modifica los campos específicos del cliente identificado mediante curp, rfc, numeroCuenta o correo sin requerir id.")
    public ResponseEntity<ClienteResponse> patch(@Valid @RequestBody ClientePatchRequest request) {
        return ResponseEntity.ok(clienteService.patch(request));
    }

    @PostMapping("/consulta")
    @Operation(summary = "Consultar cliente por identificador natural",
               description = "Retorna el cliente correspondiente al criterio indicado (curp, rfc, correo o cuenta) sin ingresar id.")
    public ResponseEntity<ClienteResponse> consultar(@Valid @RequestBody ClienteConsultaRequest request) {
        return ResponseEntity.ok(clienteService.consultar(request));
    }

    @PostMapping("/buscar")
    @Operation(summary = "Buscar clientes con filtros combinables",
               description = "Permite buscar clientes combinando filtros opcionales (nombre, CURP, RFC, correo, activo, fechas). Si no se envían filtros, retorna todos los clientes.")
    public ResponseEntity<List<ClienteResponse>> buscar(
            @Valid @RequestBody(required = false) ClienteBusquedaRequest request) {
        return ResponseEntity.ok(clienteService.buscar(request));
    }
}