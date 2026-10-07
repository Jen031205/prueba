package com.proyecto.servicios.service.auth;

import com.proyecto.servicios.entity.sf.SesionUsuario;
import com.proyecto.servicios.entity.sf.Usuario;
import com.proyecto.servicios.model.auth.LoginRequest;
import com.proyecto.servicios.model.auth.LoginResponse;
import com.proyecto.servicios.model.auth.RegistroUsuarioRequest;
import com.proyecto.servicios.model.auth.SesionEstadoResponse;
import com.proyecto.servicios.repositorys.sf.SesionUsuarioRepository;
import com.proyecto.servicios.repositorys.sf.UsuarioRepository;
import com.proyecto.servicios.service.clientes.ErrorValidacionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class AuthService {

    public static final int TIEMPO_INACTIVIDAD_MINUTOS = 5;

    private final UsuarioRepository usuarioRepository;
    private final SesionUsuarioRepository sesionUsuarioRepository;

    public AuthService(UsuarioRepository usuarioRepository,
                       SesionUsuarioRepository sesionUsuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.sesionUsuarioRepository = sesionUsuarioRepository;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String correo = request.getCorreo().trim();
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElse(null);

        if (usuario == null || !usuario.getPassword().equals(request.getPassword())) {
            log.warn("Intento de login fallido: credenciales distintas para correo='{}'", correo);
            throw new CredencialesInvalidasException(
                    "Alerta: El correo y la contraseña son distintos a los registrados. Verifique sus credenciales.");
        }

        if (!usuario.isActivo()) {
            throw new ErrorValidacionException("Alerta: El usuario se encuentra inactivo.");
        }

        sesionUsuarioRepository.desactivarSesionesPrevias(usuario.getId());

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime expiracion = ahora.plusMinutes(TIEMPO_INACTIVIDAD_MINUTOS);

        SesionUsuario sesion = new SesionUsuario();
        sesion.setUsuario(usuario);
        sesion.setCorreo(usuario.getCorreo());
        sesion.setToken(UUID.randomUUID().toString());
        sesion.setFechaInicio(ahora);
        sesion.setUltimaActividad(ahora);
        sesion.setFechaExpiracion(expiracion);
        sesion.setActiva(true);

        sesionUsuarioRepository.save(sesion);
        log.info("Sesión iniciada exitosamente para '{}'. Token generado: {}", usuario.getCorreo(), sesion.getToken());

        return LoginResponse.builder()
                .token(sesion.getToken())
                .correo(usuario.getCorreo())
                .nombre(usuario.getNombre())
                .fechaInicio(ahora)
                .fechaExpiracion(expiracion)
                .tiempoInactividadMinutos(TIEMPO_INACTIVIDAD_MINUTOS)
                .mensaje("Inicio de sesión exitoso. Su sesión se cerrará automáticamente tras 5 minutos de inactividad.")
                .build();
    }

    @Transactional
    public SesionEstadoResponse validarSesion(String token) {
        if (token == null || token.isBlank()) {
            throw new SesionInvalidaException("Alerta: Se requiere un token de sesión para validar.");
        }

        String limpio = token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();
        SesionUsuario sesion = sesionUsuarioRepository.findByToken(limpio)
                .orElseThrow(() -> new SesionInvalidaException("Alerta: Token de sesión inexistente o inválido."));

        LocalDateTime ahora = LocalDateTime.now();

        if (!sesion.isActiva() || sesion.getFechaExpiracion().isBefore(ahora)) {
            if (sesion.isActiva()) {
                sesion.setActiva(false);
                sesionUsuarioRepository.save(sesion);
                log.info("Sesión '{}' cerrada automáticamente por inactividad (> 5 minutos).", sesion.getToken());
            }
            throw new SesionExpiradaException(
                    "Alerta: Sesión cerrada automáticamente por inactividad tras superar 5 minutos. Inicie sesión nuevamente.");
        }

        sesion.setUltimaActividad(ahora);
        sesion.setFechaExpiracion(ahora.plusMinutes(TIEMPO_INACTIVIDAD_MINUTOS));
        sesionUsuarioRepository.save(sesion);

        long segundosRestantes = Duration.between(ahora, sesion.getFechaExpiracion()).getSeconds();

        return SesionEstadoResponse.builder()
                .activa(true)
                .correo(sesion.getCorreo())
                .fechaInicio(sesion.getFechaInicio())
                .ultimaActividad(ahora)
                .fechaExpiracion(sesion.getFechaExpiracion())
                .segundosRestantes(Math.max(0, segundosRestantes))
                .mensaje("Sesión activa. Temporizador de inactividad de 5 minutos reiniciado.")
                .build();
    }

    @Transactional
    public void logout(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        String limpio = token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();
        sesionUsuarioRepository.findByToken(limpio).ifPresent(sesion -> {
            sesion.setActiva(false);
            sesionUsuarioRepository.save(sesion);
            log.info("Sesión '{}' cerrada manualmente por logout.", limpio);
        });
    }

    @Transactional
    public Usuario registrar(RegistroUsuarioRequest request) {
        String correo = request.getCorreo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ErrorValidacionException("Alerta: Ya existe un usuario registrado con el correo: " + correo);
        }

        Usuario usuario = new Usuario();
        usuario.setCorreo(correo);
        usuario.setPassword(request.getPassword());
        usuario.setNombre(request.getNombre().trim());
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());

        return usuarioRepository.save(usuario);
    }

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void limpiarSesionesInactivasEnBd() {
        int cerradas = sesionUsuarioRepository.cerrarSesionesExpiradas(LocalDateTime.now());
        if (cerradas > 0) {
            log.info("Temporizador de inactividad: {} sesiones fueron cerradas automáticamente en la base de datos por superar 5 minutos.", cerradas);
        }
    }
}
