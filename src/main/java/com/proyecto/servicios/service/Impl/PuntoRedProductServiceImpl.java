package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.PuntoRedClient;
import com.proyecto.servicios.config.PuntoRedProperties;
import com.proyecto.servicios.config.PuntoRedTokenProvider;
import com.proyecto.servicios.model.ProductListResponse;
import com.proyecto.servicios.service.PuntoRedIntegrationException;
import com.proyecto.servicios.service.PuntoRedProductService;
import feign.FeignException;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PuntoRedProductServiceImpl implements PuntoRedProductService {

    private final PuntoRedClient puntoRedClient;
    private final PuntoRedProperties properties;
    private final PuntoRedTokenProvider tokenProvider;

    public PuntoRedProductServiceImpl(PuntoRedClient puntoRedClient, PuntoRedProperties properties, PuntoRedTokenProvider tokenProvider) {
        this.puntoRedClient = puntoRedClient;
        this.properties = properties;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public ProductListResponse getProductList() {
        // Inicia la invocacion al servicio externo.
        log.info("Iniciando invocacion de lista de productos PuntoRed");

        try {
            String authorization = tokenProvider.getBearerToken();
            ResponseEntity<String> response = puntoRedClient.getProductList(
                    authorization, properties.getApiKey());
            String content = response.getBody();

            if (!response.getStatusCode().is2xxSuccessful() || content == null) {
                throw new PuntoRedIntegrationException("PuntoRed devolvio una respuesta no exitosa");
            }

            return new ProductListResponse(content, response.getHeaders().getFirst("Content-Type"));
        } catch (IllegalStateException exception) {
            log.error("No fue posible obtener el token de PuntoRed", exception);
            throw new PuntoRedIntegrationException(
                "No fue posible autenticar contra PuntoRed. Configura PUNTORED_API_TOKEN o verifica las credenciales de GestoPago",
                exception);
        } catch (FeignException.Unauthorized | FeignException.Forbidden exception) {
            log.error("PuntoRed rechazo la autenticacion al consultar productos. status={}", exception.status());
            throw new PuntoRedIntegrationException(
                "PuntoRed rechazo la autenticacion (HTTP " + exception.status()
                    + "). Verifica PUNTORED_API_TOKEN y PUNTORED_API_KEY",
                exception);
        } catch (RetryableException exception) {
            log.error("Timeout o error de comunicacion al invocar lista de productos PuntoRed");
            throw new PuntoRedIntegrationException("PuntoRed no respondio dentro del tiempo esperado", exception);
        } catch (FeignException exception) {
            log.error("PuntoRed devolvio un error HTTP status={}", exception.status());
            throw new PuntoRedIntegrationException("PuntoRed devolvio una respuesta no exitosa", exception);
        } finally {
            // Cierra el flujo de logs para la operacion.
            log.info("Finalizo invocacion de lista de productos PuntoRed");
        }
    }
}
