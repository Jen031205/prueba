package com.proyecto.servicios.controller.clientes;

import com.proyecto.servicios.model.clientes.biometria.BiometriaRegistroRequest;
import com.proyecto.servicios.model.clientes.biometria.BiometriaResponse;
import com.proyecto.servicios.model.clientes.biometria.BiometriaValidacionRequest;
import com.proyecto.servicios.model.clientes.biometria.BiometriaValidacionResponse;
import com.proyecto.servicios.service.clientes.BiometriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clientes/{id}/biometria")
@Tag(name = "Biometría Facial", description = "Operaciones de registro, consulta y validación de datos biométricos faciales (MediaPipe)")
public class BiometriaController {

    private final BiometriaService biometriaService;

    public BiometriaController(BiometriaService biometriaService) {
        this.biometriaService = biometriaService;
    }

    @PostMapping
    @Operation(summary = "Registrar biometría facial del cliente",
               description = "Guarda el vector de embedding facial generado por MediaPipe, la confianza de detección y la imagen opcional en base de datos.")
    public ResponseEntity<BiometriaResponse> registrar(
            @PathVariable Long id,
            @Valid @RequestBody BiometriaRegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(biometriaService.registrar(id, request));
    }

    @GetMapping
    @Operation(summary = "Consultar datos biométricos del cliente",
               description = "Retorna el estatus, fecha de captura y metadatos del registro biométrico del cliente.")
    public BiometriaResponse consultar(@PathVariable Long id) {
        return biometriaService.consultarPorClienteId(id);
    }

    @PostMapping("/validar")
    @Operation(summary = "Validar biometría facial actual",
               description = "Compara el vector de embedding enviado contra el registrado en la base de datos mediante similitud de coseno para comprobar identidad.")
    public BiometriaValidacionResponse validar(
            @PathVariable Long id,
            @Valid @RequestBody BiometriaValidacionRequest request) {
        return biometriaService.validar(id, request);
    }
}
