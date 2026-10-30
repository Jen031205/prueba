package com.proyecto.servicios.controller.pagos;

import com.proyecto.servicios.model.pagos.PagoCompraRequest;
import com.proyecto.servicios.model.pagos.PagoCompraResponse;
import com.proyecto.servicios.service.pagos.PagoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping
    public ResponseEntity<PagoCompraResponse> procesarPago(@Valid @RequestBody PagoCompraRequest request) {
        PagoCompraResponse response = pagoService.procesarPago(request);
        return ResponseEntity.ok(response);
    }
}
