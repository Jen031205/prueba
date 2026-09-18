package com.proyecto.servicios.controller;

import com.proyecto.servicios.client.GestoPagoAuthClient;
import com.proyecto.servicios.model.gestopago.GestoPagoAuthRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoAuthResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/gestopago")
public class GestoPagoController {

    private final GestoPagoAuthClient gestoPagoAuthClient;

    public GestoPagoController(GestoPagoAuthClient gestoPagoAuthClient) {
        this.gestoPagoAuthClient = gestoPagoAuthClient;
    }

    @PostMapping("/autenticar")
    public ResponseEntity<GestoPagoAuthResponse> autenticar(@Valid @RequestBody GestoPagoAuthRequest request) {
        // Llama al servicio externo para obtener el token de acceso.
        GestoPagoAuthResponse response = gestoPagoAuthClient.authenticate(
                request.getIdDistribuidor(),
                request.getCodigoDispositivo(),
                request.getPassword()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/renovar-token")
    public ResponseEntity<GestoPagoAuthResponse> renovarToken(@Valid @RequestBody GestoPagoAuthRequest request) {
        // Reutiliza la autenticacion para refrescar el token vigente.
        GestoPagoAuthResponse response = gestoPagoAuthClient.authenticate(
                request.getIdDistribuidor(),
                request.getCodigoDispositivo(),
                request.getPassword()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/echo")
    public ResponseEntity<Map<String, String>> echo() {
        // Verifica que el endpoint de GestoPago este activo.
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "GestoPago endpoint activo"
        ));
    }
}
