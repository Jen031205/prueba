package com.proyecto.servicios.client;

import com.proyecto.servicios.config.PuntoRedFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "puntoRedClient", url = "${puntored.api.base-url}", configuration = PuntoRedFeignConfig.class)
public interface PuntoRedClient {

    @GetMapping(value = "/sistema/service/getProductList.do", produces = "application/xml")
    ResponseEntity<String> getProductList(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey);
}
