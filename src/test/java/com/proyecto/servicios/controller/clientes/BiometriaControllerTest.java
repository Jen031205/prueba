package com.proyecto.servicios.controller.clientes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.model.clientes.biometria.BiometriaRegistroRequest;
import com.proyecto.servicios.model.clientes.biometria.BiometriaResponse;
import com.proyecto.servicios.model.clientes.biometria.BiometriaValidacionRequest;
import com.proyecto.servicios.model.clientes.biometria.BiometriaValidacionResponse;
import com.proyecto.servicios.service.clientes.BiometriaNoEncontradaException;
import com.proyecto.servicios.service.clientes.BiometriaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BiometriaController.class)
class BiometriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BiometriaService biometriaService;

    @Test
    void postRegistrarBiometriaRetorna201CuandoDatosSonValidos() throws Exception {
        BiometriaResponse response = new BiometriaResponse();
        response.setId(1L);
        response.setClienteId(10L);
        response.setTipoBiometria("FACIAL_MEDIAPIPE");
        response.setConfianzaDeteccion(new BigDecimal("98.50"));
        response.setRostrosDetectados(1);
        response.setFechaCaptura(LocalDateTime.now());
        response.setEstatus("VIGENTE");
        response.setTieneFoto(true);
        response.setDimensionVector(128);

        when(biometriaService.registrar(eq(10L), any(BiometriaRegistroRequest.class)))
                .thenReturn(response);

        String json = """
            {
              "tipoBiometria": "FACIAL_MEDIAPIPE",
              "confianzaDeteccion": "98.50",
              "rostrosDetectados": "1",
              "embedding": "[0.12, -0.34, 0.56, 0.78]",
              "fotoBase64": "data:image/jpeg;base64,sample"
            }
            """;

        mockMvc.perform(post("/clientes/10/biometria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clienteId").value(10))
                .andExpect(jsonPath("$.tipoBiometria").value("FACIAL_MEDIAPIPE"))
                .andExpect(jsonPath("$.dimensionVector").value(128));
    }

    @Test
    void postRegistrarBiometriaRetorna400CuandoFaltaEmbedding() throws Exception {
        String jsonSinEmbedding = """
            {
              "tipoBiometria": "FACIAL_MEDIAPIPE",
              "confianzaDeteccion": "98.50",
              "rostrosDetectados": "1"
            }
            """;

        mockMvc.perform(post("/clientes/10/biometria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSinEmbedding))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getConsultarBiometriaRetorna200Ok() throws Exception {
        BiometriaResponse response = new BiometriaResponse();
        response.setId(1L);
        response.setClienteId(10L);
        response.setTipoBiometria("FACIAL_MEDIAPIPE");
        response.setConfianzaDeteccion(new BigDecimal("99.00"));
        response.setRostrosDetectados(1);
        response.setDimensionVector(128);

        when(biometriaService.consultarPorClienteId(10L)).thenReturn(response);

        mockMvc.perform(get("/clientes/10/biometria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(10))
                .andExpect(jsonPath("$.dimensionVector").value(128));
    }

    @Test
    void getConsultarBiometriaRetorna404CuandoNoTieneRegistrada() throws Exception {
        when(biometriaService.consultarPorClienteId(99L))
                .thenThrow(new BiometriaNoEncontradaException("id 99"));

        mockMvc.perform(get("/clientes/99/biometria"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void postValidarBiometriaRetornaResultadoDeSimilitud() throws Exception {
        BiometriaValidacionResponse response = new BiometriaValidacionResponse(
                10L,
                true,
                0.9754,
                new BigDecimal("97.54"),
                0.85,
                "Validación biométrica exitosa."
        );

        when(biometriaService.validar(eq(10L), any(BiometriaValidacionRequest.class)))
                .thenReturn(response);

        String json = """
            {
              "embedding": "[0.12, -0.34, 0.56, 0.78]",
              "umbral": "0.85"
            }
            """;

        mockMvc.perform(post("/clientes/10/biometria/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(10))
                .andExpect(jsonPath("$.coincide").value(true))
                .andExpect(jsonPath("$.porcentajeSimilitud").value(97.54));
    }
}
