package com.proyecto.servicios.service.pagos.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.model.pagos.DatosCompraDetalle;
import com.proyecto.servicios.service.pagos.CifradoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;

@Service
@Slf4j
public class CifradoServiceImpl implements CifradoService {

    private static final String ALGORITMO = "AES/CBC/PKCS5Padding";
    private static final DateTimeFormatter FORMATO_HORA_LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ObjectMapper objectMapper;
    private final byte[] secretKeyBytes;

    public CifradoServiceImpl(ObjectMapper objectMapper,
                              @Value("${pagos.cifrado.clave:ClaveSecretaPuntoredPagos2026!}") String secretKey) {
        this.objectMapper = objectMapper;
        this.secretKeyBytes = derivarClave256(secretKey);
    }

    @Override
    public String cifrar(DatosCompraDetalle datos) {
        try {
            String jsonPlain = objectMapper.writeValueAsString(datos);

            byte[] iv = new byte[16];
            RANDOM.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            SecretKeySpec keySpec = new SecretKeySpec(secretKeyBytes, "AES");
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            byte[] encrypted = cipher.doFinal(jsonPlain.getBytes(StandardCharsets.UTF_8));

            // Concatena IV + datos cifrados para permitir descifrado si se requiere
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("Error al cifrar los datos de la compra: {}", e.getMessage(), e);
            throw new RuntimeException("Error en el cifrado de la petición de compra", e);
        }
    }

    @Override
    public String generarUpc() {
        // UPC: random string de exactamente 10 digitos
        StringBuilder upc = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            upc.append(RANDOM.nextInt(10));
        }
        return upc.toString();
    }

    @Override
    public String obtenerHoraLocal() {
        return LocalDateTime.now().format(FORMATO_HORA_LOCAL);
    }

    private byte[] derivarClave256(String key) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha.digest(key.getBytes(StandardCharsets.UTF_8));
            return Arrays.copyOf(hash, 32);
        } catch (Exception e) {
            throw new IllegalStateException("Error al derivar clave SHA-256", e);
        }
    }
}
