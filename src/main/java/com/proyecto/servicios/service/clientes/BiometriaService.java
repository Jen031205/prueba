package com.proyecto.servicios.service.clientes;

import com.proyecto.servicios.entity.sf.clientes.BiometriaCliente;
import com.proyecto.servicios.entity.sf.clientes.Cliente;
import com.proyecto.servicios.model.clientes.biometria.BiometriaRegistroRequest;
import com.proyecto.servicios.model.clientes.biometria.BiometriaResponse;
import com.proyecto.servicios.model.clientes.biometria.BiometriaValidacionRequest;
import com.proyecto.servicios.model.clientes.biometria.BiometriaValidacionResponse;
import com.proyecto.servicios.repositorys.sf.clientes.BiometriaClienteRepository;
import com.proyecto.servicios.repositorys.sf.clientes.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

@Service
public class BiometriaService {

    private static final double UMBRAL_DEFAULT = 0.85;
    private final BiometriaClienteRepository biometriaRepository;
    private final ClienteRepository clienteRepository;

    public BiometriaService(BiometriaClienteRepository biometriaRepository,
                            ClienteRepository clienteRepository) {
        this.biometriaRepository = biometriaRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public BiometriaResponse registrar(Long clienteId, BiometriaRegistroRequest request) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ClienteNoEncontradoException("id " + clienteId));

        if (!cliente.isActivo()) {
            throw new ErrorValidacionException("No es posible registrar biometría para un cliente inactivo");
        }

        if (request.getRostrosDetectados() != null && request.getRostrosDetectados() > 1) {
            throw new ErrorValidacionException("Se detectó más de un rostro en la captura ("
                    + request.getRostrosDetectados() + "). La captura debe contener exactamente un rostro.");
        }

        double[] vector = parsearVector(request.getEmbedding());
        if (vector.length < 4) {
            throw new ErrorValidacionException("El vector de embedding de MediaPipe debe contener al menos 4 dimensiones numéricas");
        }

        Optional<BiometriaCliente> existente = biometriaRepository.findByClienteId(clienteId);
        BiometriaCliente biometria = existente.orElseGet(BiometriaCliente::new);

        biometria.setCliente(cliente);
        biometria.setTipoBiometria(request.getTipoBiometria() != null && !request.getTipoBiometria().isBlank()
                ? request.getTipoBiometria().trim() : "FACIAL_MEDIAPIPE");
        biometria.setEmbedding(formatearVector(vector));
        biometria.setConfianzaDeteccion(request.getConfianzaDeteccion());
        biometria.setRostrosDetectados(request.getRostrosDetectados() != null ? request.getRostrosDetectados() : 1);
        biometria.setFotoBase64(request.getFotoBase64() != null && !request.getFotoBase64().isBlank()
                ? request.getFotoBase64().trim() : null);
        biometria.setFechaCaptura(LocalDateTime.now());
        biometria.setEstatus("VIGENTE");

        biometria = biometriaRepository.saveAndFlush(biometria);
        return toResponse(biometria, vector.length);
    }

    @Transactional(readOnly = true)
    public BiometriaResponse consultarPorClienteId(Long clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new ClienteNoEncontradoException("id " + clienteId);
        }
        BiometriaCliente biometria = biometriaRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new BiometriaNoEncontradaException("id " + clienteId));

        double[] vector = parsearVector(biometria.getEmbedding());
        return toResponse(biometria, vector.length);
    }

    @Transactional(readOnly = true)
    public BiometriaValidacionResponse validar(Long clienteId, BiometriaValidacionRequest request) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new ClienteNoEncontradoException("id " + clienteId);
        }

        BiometriaCliente biometria = biometriaRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new BiometriaNoEncontradaException("id " + clienteId));

        double[] vGuardado = parsearVector(biometria.getEmbedding());
        double[] vCandidato = parsearVector(request.getEmbedding());

        if (vGuardado.length != vCandidato.length) {
            throw new ErrorValidacionException("La dimensión del vector enviado (" + vCandidato.length
                    + ") no coincide con la registrada en el sistema (" + vGuardado.length + ")");
        }

        double similitud = calcularSimilitudCoseno(vGuardado, vCandidato);
        double umbral = (request.getUmbral() != null)
                ? request.getUmbral().doubleValue()
                : UMBRAL_DEFAULT;

        boolean coincide = similitud >= umbral;
        BigDecimal porcentaje = BigDecimal.valueOf(Math.max(0.0, similitud * 100.0))
                .setScale(2, RoundingMode.HALF_UP);

        String mensaje = coincide
                ? "Validación biométrica exitosa. El rostro coincide con el registrado (similitud del " + porcentaje + "%)."
                : "Validación biométrica rechazada. La similitud del rostro (" + porcentaje
                + "%) no alcanza el umbral requerido (" + BigDecimal.valueOf(umbral * 100.0).setScale(2, RoundingMode.HALF_UP) + "%).";

        return new BiometriaValidacionResponse(
                clienteId,
                coincide,
                similitud,
                porcentaje,
                umbral,
                mensaje
        );
    }

    public double calcularSimilitudCoseno(double[] v1, double[] v2) {
        if (v1.length != v2.length || v1.length == 0) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    public double[] parsearVector(String embeddingTexto) {
        if (embeddingTexto == null || embeddingTexto.isBlank()) {
            throw new ErrorValidacionException("El vector de embedding no puede estar vacío");
        }

        String limpio = embeddingTexto.trim();
        if (limpio.startsWith("[")) {
            limpio = limpio.substring(1);
        }
        if (limpio.endsWith("]")) {
            limpio = limpio.substring(0, limpio.length() - 1);
        }
        limpio = limpio.trim();

        if (limpio.isEmpty()) {
            throw new ErrorValidacionException("El vector de embedding no contiene valores numéricos");
        }

        String[] tokens = limpio.contains(",") ? limpio.split(",") : limpio.split("\\s+");
        double[] resultado = new double[tokens.length];

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].trim();
            try {
                resultado[i] = Double.parseDouble(token);
            } catch (NumberFormatException e) {
                throw new ErrorValidacionException("Valor no numérico en vector de embedding: '" + token + "'");
            }
        }

        return resultado;
    }

    private String formatearVector(double[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private BiometriaResponse toResponse(BiometriaCliente b, int dimensionVector) {
        BiometriaResponse res = new BiometriaResponse();
        res.setId(b.getId());
        res.setClienteId(b.getCliente() != null ? b.getCliente().getId() : null);
        res.setTipoBiometria(b.getTipoBiometria());
        res.setConfianzaDeteccion(b.getConfianzaDeteccion());
        res.setRostrosDetectados(b.getRostrosDetectados());
        res.setFechaCaptura(b.getFechaCaptura());
        res.setEstatus(b.getEstatus());
        res.setTieneFoto(b.getFotoBase64() != null && !b.getFotoBase64().isBlank());
        res.setDimensionVector(dimensionVector);
        return res;
    }
}
