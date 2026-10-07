package com.proyecto.servicios.config;

import feign.Request;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class PuntoRedFeignConfig {

    @Bean
    public RequestInterceptor puntoRedAuthenticationInterceptor(
            PuntoRedTokenProvider tokenProvider, PuntoRedProperties properties) {
        return template -> {
            template.header("Authorization", tokenProvider.getBearerToken());
            if (properties.getApiKey() != null && !properties.getApiKey().isBlank()) {
                template.header("X-API-Key", properties.getApiKey());
            }
        };
    }

    @Bean
    public Request.Options puntoRedRequestOptions(PuntoRedProperties properties) {
        return new Request.Options(
            properties.getConnectTimeoutMs(), TimeUnit.MILLISECONDS,
            properties.getReadTimeoutMs(), TimeUnit.MILLISECONDS,
            true);
    }
}
