package com.proyecto.servicios.service.pagos.Impl;

import com.proyecto.servicios.entity.sf.clientes.Cliente;
import com.proyecto.servicios.model.pagos.Solicitar2FAResponse;
import com.proyecto.servicios.service.clientes.ErrorValidacionException;
import com.proyecto.servicios.service.pagos.DobleAutenticacionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.proyecto.servicios.client.BrevoEmailClient;
import com.proyecto.servicios.model.pagos.brevo.BrevoEmailRequest;
import com.proyecto.servicios.model.pagos.brevo.BrevoEmailResponse;
import com.proyecto.servicios.model.pagos.brevo.BrevoRecipient;
import com.proyecto.servicios.model.pagos.brevo.BrevoSender;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class DobleAutenticacionServiceImpl implements DobleAutenticacionService {

    private static final int EXPIRACION_MINUTOS = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Map<Long, Codigo2FAEntry> cacheCodigos = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private BrevoEmailClient brevoEmailClient;

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    @Value("${brevo.sender.name:Banco Integrador - Notificaciones}")
    private String brevoSenderName;

    @Value("${brevo.sender.email:notificaciones@banco.com}")
    private String brevoSenderEmail;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:notificaciones@banco.com}")
    private String remitenteMail;

    @Override
    public Solicitar2FAResponse generarYEnviarCodigo(Cliente cliente) {
        String codigo = String.format("%06d", RANDOM.nextInt(1000000));
        Instant expiraEn = Instant.now().plusSeconds(EXPIRACION_MINUTOS * 60L);

        cacheCodigos.put(cliente.getId(), new Codigo2FAEntry(codigo, expiraEn));
        log.info("Código 2FA generado para cliente {}: [PROTEGIDO] (válido por {} minutos)",
                cliente.getId(), EXPIRACION_MINUTOS);

        enviarCorreo(cliente.getCorreoElectronico(), cliente.getNombre(), codigo);

        return Solicitar2FAResponse.builder()
                .estado(true)
                .mensaje("Código de doble autenticación generado y enviado al correo electrónico")
                .correoDestino(enmascararCorreo(cliente.getCorreoElectronico()))
                .expiraEnMinutos(EXPIRACION_MINUTOS)
                .build();
    }

    @Override
    public boolean validarCodigo(Long clienteId, String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new ErrorValidacionException("El código de doble autenticación es obligatorio");
        }

        String codigoLimpio = codigo.trim();

        Codigo2FAEntry entry = cacheCodigos.get(clienteId);
        if (entry == null) {
            throw new ErrorValidacionException(
                    "No hay un código de doble autenticación activo para este cliente. Solicita uno nuevo.");
        }

        if (Instant.now().isAfter(entry.expiraEn())) {
            cacheCodigos.remove(clienteId);
            throw new ErrorValidacionException(
                    "El código de doble autenticación ha expirado. Por favor solicita uno nuevo.");
        }

        if (!entry.codigo().equals(codigoLimpio)) {
            throw new ErrorValidacionException(
                    "El código de doble autenticación ingresado es incorrecto.");
        }

        // Código válido: se consume para evitar reutilización
        cacheCodigos.remove(clienteId);
        log.info("Doble autenticación 2FA validada exitosamente para cliente {}", clienteId);
        return true;
    }

    private void enviarCorreo(String destinatario, String nombre, String codigo) {
        if (destinatario == null || destinatario.isBlank()) {
            log.warn("El cliente no tiene un correo electrónico configurado para recibir 2FA");
            return;
        }

        String asunto = "Código de Seguridad para Autorización de Compra: " + codigo;

        String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 560px; margin: 0 auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 12px; background-color: #ffffff;'>"
                + "<h2 style='color: #1e3a8a; margin-top: 0;'>Autorización de Compra</h2>"
                + "<p style='font-size: 15px; color: #334155;'>Hola <strong>" + (nombre != null ? nombre : "Cliente") + "</strong>,</p>"
                + "<p style='font-size: 15px; color: #334155;'>Has solicitado realizar una compra con tu cuenta bancaria. Para completar la transacción, ingresa el siguiente código de verificación de doble autenticación:</p>"
                + "<div style='text-align: center; margin: 28px 0;'>"
                + "  <span style='display: inline-block; font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #1e3a8a; background-color: #f1f5f9; padding: 14px 28px; border-radius: 8px; border: 1px dashed #cbd5e1;'>" + codigo + "</span>"
                + "</div>"
                + "<p style='font-size: 14px; color: #64748b;'>Este código es confidencial y tiene una vigencia de <strong>" + EXPIRACION_MINUTOS + " minutos</strong>.</p>"
                + "<p style='font-size: 13px; color: #94a3b8; border-top: 1px solid #f1f5f9; padding-top: 16px; margin-bottom: 0;'>Si tú no solicitaste este movimiento, por favor comunícate de inmediato con atención a clientes.</p>"
                + "</div>";

        String textoPlano = "Hola " + (nombre != null ? nombre : "Cliente") + ",\n\n"
                + "Tu código de verificación para confirmar tu compra es: " + codigo + "\n\n"
                + "Este código es válido por " + EXPIRACION_MINUTOS + " minutos.\n"
                + "Si no solicitaste este código, comunícate con atención al cliente.\n";

        // 1. Intentar envío vía Brevo REST API si la API Key está configurada
        if (brevoEmailClient != null && brevoApiKey != null && !brevoApiKey.isBlank()) {
            try {
                BrevoEmailRequest brevoRequest = BrevoEmailRequest.builder()
                        .sender(BrevoSender.builder()
                                .name(brevoSenderName)
                                .email(brevoSenderEmail)
                                .build())
                        .to(List.of(BrevoRecipient.builder()
                                .name(nombre != null ? nombre : "Cliente")
                                .email(destinatario)
                                .build()))
                        .subject(asunto)
                        .htmlContent(htmlContent)
                        .textContent(textoPlano)
                        .build();

                BrevoEmailResponse response = brevoEmailClient.sendEmail(brevoApiKey, brevoRequest);
                log.info("Correo 2FA enviado exitosamente vía Brevo REST API a {}. MessageId: {}",
                        destinatario, response != null ? response.getMessageId() : "OK");
                return;
            } catch (Exception e) {
                log.warn("Fallo al enviar correo vía Brevo REST API a {}: {}. Intentando fallback SMTP...",
                        destinatario, e.getMessage());
            }
        }

        // 2. Fallback: Envío vía SMTP (Brevo Relay u otro servidor configurado)
        try {
            if (mailSender != null) {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(remitenteMail, "Banco Integrador - Notificaciones");
                helper.setTo(destinatario);
                helper.setSubject(asunto);
                helper.setText(textoPlano, htmlContent);
                mailSender.send(mimeMessage);
                log.info("Correo 2FA enviado satisfactoriamente vía SMTP a {}", destinatario);
            } else {
                log.info("Servidor de correo no configurado. Código 2FA generado para {}: {}", destinatario, codigo);
            }
        } catch (Exception e) {
            log.warn("Error al enviar correo SMTP a {}: {}. Código de respaldo: {}",
                    destinatario, e.getMessage(), codigo);
        }
    }

    private String enmascararCorreo(String correo) {
        if (correo == null || !correo.contains("@")) {
            return correo;
        }
        int atIndex = correo.indexOf('@');
        if (atIndex <= 2) {
            return "***" + correo.substring(atIndex);
        }
        return correo.substring(0, 2) + "***" + correo.substring(atIndex);
    }

    private record Codigo2FAEntry(String codigo, Instant expiraEn) {}
}
