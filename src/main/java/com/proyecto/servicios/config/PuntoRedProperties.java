package com.proyecto.servicios.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class PuntoRedProperties {

    private final String baseUrl;
    private final String token;
    private final String apiKey;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    public PuntoRedProperties(
            @Value("${puntored.api.base-url}") String baseUrl,
            @Value("${puntored.api.token}") String token,
            @Value("${puntored.api.api-key:}") String apiKey,
            @Value("${puntored.api.connect-timeout-ms:5000}") int connectTimeoutMs,
            @Value("${puntored.api.read-timeout-ms:10000}") int readTimeoutMs) {
        this.baseUrl = baseUrl;
        this.token = token;
        this.apiKey = apiKey;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
    }
}
