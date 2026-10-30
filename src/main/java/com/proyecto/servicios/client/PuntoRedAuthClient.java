package com.proyecto.servicios.client;

import com.proyecto.servicios.model.pagos.PuntoredAuthRequest;
import com.proyecto.servicios.model.pagos.PuntoredAuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "puntoRedAuthClient", url = "${puntored.auth.url:https://services.preprodcxr.co/puntored/api/v1}")
public interface PuntoRedAuthClient {

    @PostMapping(value = "/auth", consumes = "application/json", produces = "application/json")
    PuntoredAuthResponse authenticate(@RequestBody PuntoredAuthRequest request);
}
