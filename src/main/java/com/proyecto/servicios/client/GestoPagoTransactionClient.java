package com.proyecto.servicios.client;

import com.proyecto.servicios.config.PuntoRedFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "gestoPagoTransactionClient",
        url = "${puntored.api.base-url:https://gestopago.portalventas.net}",
        configuration = PuntoRedFeignConfig.class
)
public interface GestoPagoTransactionClient {

    @PostMapping(value = "/sistema/service/sendTx.do")
    ResponseEntity<String> sendTx(@RequestBody String requestPayload);
}

