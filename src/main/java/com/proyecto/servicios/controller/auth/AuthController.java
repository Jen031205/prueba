package com.proyecto.servicios.controller.auth;

import com.proyecto.servicios.entity.sf.Usuario;
import com.proyecto.servicios.model.auth.LoginRequest;
import com.proyecto.servicios.model.auth.LoginResponse;
import com.proyecto.servicios.model.auth.RegistroUsuarioRequest;
import com.proyecto.servicios.model.auth.SesionEstadoResponse;
import com.proyecto.servicios.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación y Login", description = "Inicio de sesión con validación de credenciales y timeout de inactividad de 5 minutos en base de datos")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión (Login)",
               description = "Valida correo y contraseña. Si son distintos a los registrados, retorna alerta HTTP 401. Si son correctos, inicia sesión con temporizador de inactividad de 5 minutos que se cierra automáticamente.")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/validar-sesion")
    @Operation(summary = "Validar estado de sesión y reiniciar timeout de 5 minutos",
               description = "Verifica si la sesión sigue activa. Si han transcurrido más de 5 minutos sin actividad, se cierra automáticamente y retorna alerta HTTP 401. Si está activa, reinicia el temporizador de 5 minutos.")
    public ResponseEntity<SesionEstadoResponse> validarSesion(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam) {
        String token = (authHeader != null && !authHeader.isBlank()) ? authHeader : tokenParam;
        return ResponseEntity.ok(authService.validarSesion(token));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión manualmente",
               description = "Invalida la sesión activa en la base de datos.")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam) {
        String token = (authHeader != null && !authHeader.isBlank()) ? authHeader : tokenParam;
        authService.logout(token);
        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada correctamente"));
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar nuevo usuario",
               description = "Permite registrar un nuevo usuario con correo, contraseña y nombre para poder iniciar sesión.")
    public ResponseEntity<Map<String, Object>> registrar(@Valid @RequestBody RegistroUsuarioRequest request) {
        Usuario usuario = authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensaje", "Usuario registrado exitosamente",
                "id", usuario.getId(),
                "correo", usuario.getCorreo(),
                "nombre", usuario.getNombre()
        ));
    }
}
