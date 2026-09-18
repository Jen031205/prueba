package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.ProductListResponse;
import com.proyecto.servicios.service.PuntoRedProductService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/productos")
public class PuntoRedProductController {

    private final PuntoRedProductService productService;

    public PuntoRedProductController(PuntoRedProductService productService) {
        this.productService = productService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductListResponse> getProductList() {
        return ResponseEntity.ok(productService.getProductList());
    }
}
