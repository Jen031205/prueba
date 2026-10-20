package com.proyecto.servicios.controller.usuarios;

import com.proyecto.servicios.model.usuarios.UsuarioActualizacionRequest;
import com.proyecto.servicios.model.usuarios.UsuarioBusquedaRequest;
import com.proyecto.servicios.model.usuarios.UsuarioCreacionRequest;
import com.proyecto.servicios.model.usuarios.UsuarioDesactivarRequest;
import com.proyecto.servicios.model.usuarios.UsuarioResponse;
import com.proyecto.servicios.service.usuarios.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuarios", description = "API REST para la administración de usuarios: creación, consulta dinámica combinada, actualización y baja lógica sin uso de @PathVariable ni @RequestParam")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @Operation(summary = "Crear nuevo usuario",
               description = "Registra un nuevo usuario en el sistema validando sus datos y evitando correos duplicados.")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioCreacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(request));
    }

    @PostMapping("/buscar")
    @Operation(summary = "Consultar y buscar usuarios con filtros dinámicos combinables",
               description = "Único endpoint de consulta de usuarios. Permite combinar filtros opcionales (id, correo, nombre, activo, rango de fechas). Si no se envían filtros, retorna la lista completa.")
    public ResponseEntity<List<UsuarioResponse>> buscar(
            @Valid @RequestBody(required = false) UsuarioBusquedaRequest request) {
        return ResponseEntity.ok(usuarioService.buscar(request));
    }

    @PutMapping
    @Operation(summary = "Actualizar usuario existente",
               description = "Actualiza los datos del usuario especificado por su id dentro del payload. Protege los campos controlados por el servidor.")
    public ResponseEntity<UsuarioResponse> actualizar(
            @Valid @RequestBody UsuarioActualizacionRequest request) {
        return ResponseEntity.ok(usuarioService.actualizar(request));
    }

    @DeleteMapping
    @Operation(summary = "Eliminar / Desactivar usuario",
               description = "Realiza la baja lógica del usuario identificado en el cuerpo de la solicitud.")
    public ResponseEntity<UsuarioResponse> eliminar(
            @Valid @RequestBody UsuarioDesactivarRequest request) {
        return ResponseEntity.ok(usuarioService.desactivar(request));
    }

    @PatchMapping("/desactivar")
    @Operation(summary = "Desactivar usuario (baja lógica)",
               description = "Desactiva al usuario identificado en el cuerpo de la solicitud, preservando su historial en el sistema.")
    public ResponseEntity<UsuarioResponse> desactivar(
            @Valid @RequestBody UsuarioDesactivarRequest request) {
        return ResponseEntity.ok(usuarioService.desactivar(request));
    }
}
