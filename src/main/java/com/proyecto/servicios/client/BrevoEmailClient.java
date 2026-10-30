package com.proyecto.servicios.client;

import com.proyecto.servicios.model.pagos.brevo.BrevoEmailRequest;
import com.proyecto.servicios.model.pagos.brevo.BrevoEmailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "brevoEmailClient", url = "${brevo.api.url:https://api.brevo.com/v3}")
public interface BrevoEmailClient {

    @PostMapping(value = "/smtp/email", consumes = "application/json", produces = "application/json")
    BrevoEmailResponse sendEmail(
            @RequestHeader("api-key") String apiKey,
            @RequestBody BrevoEmailRequest request
    );
}
