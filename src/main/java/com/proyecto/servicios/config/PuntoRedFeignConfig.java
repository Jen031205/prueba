package com.proyecto.servicios.config;

import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class PuntoRedFeignConfig {

    @Bean
    public Request.Options puntoRedRequestOptions(PuntoRedProperties properties) {
        return new Request.Options(
            properties.getConnectTimeoutMs(), TimeUnit.MILLISECONDS,
            properties.getReadTimeoutMs(), TimeUnit.MILLISECONDS,
            true);
    }
}
