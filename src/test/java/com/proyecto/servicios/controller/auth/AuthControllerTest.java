package com.proyecto.servicios.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.controller.clientes.ClienteExceptionHandler;
import com.proyecto.servicios.model.auth.LoginRequest;
import com.proyecto.servicios.model.auth.LoginResponse;
import com.proyecto.servicios.model.auth.SesionEstadoResponse;
import com.proyecto.servicios.service.auth.AuthService;
import com.proyecto.servicios.service.auth.CredencialesInvalidasException;
import com.proyecto.servicios.service.auth.SesionExpiradaException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(ClienteExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void loginRetorna200YTokenConCincoMinutosDeInactividad() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setCorreo("admin@banco.com");
        req.setPassword("Admin123!");

        LoginResponse resp = LoginResponse.builder()
                .token("token-jwt-12345")
                .correo("admin@banco.com")
                .nombre("Administrador")
                .fechaInicio(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusMinutes(5))
                .tiempoInactividadMinutos(5)
                .mensaje("Inicio de sesión exitoso. Su sesión se cerrará automáticamente tras 5 minutos de inactividad.")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-jwt-12345"))
                .andExpect(jsonPath("$.correo").value("admin@banco.com"))
                .andExpect(jsonPath("$.tiempoInactividadMinutos").value(5));
    }

    @Test
    void loginRetorna401ConAlertaCuandoCorreoYPasswordSonDistintos() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setCorreo("admin@banco.com");
        req.setPassword("Erronea!");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new CredencialesInvalidasException(
                        "Alerta: El correo y la contraseña son distintos a los registrados. Verifique sus credenciales."));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value(
                        "Alerta: El correo y la contraseña son distintos a los registrados. Verifique sus credenciales."));
    }

    @Test
    void validarSesionRetorna200CuandoSesionEstaActiva() throws Exception {
        SesionEstadoResponse resp = SesionEstadoResponse.builder()
                .activa(true)
                .correo("admin@banco.com")
                .segundosRestantes(300L)
                .mensaje("Sesión activa. Temporizador de inactividad de 5 minutos reiniciado.")
                .build();

        when(authService.validarSesion("Bearer token-valido")).thenReturn(resp);

        mockMvc.perform(get("/auth/validar-sesion")
                        .header("Authorization", "Bearer token-valido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activa").value(true))
                .andExpect(jsonPath("$.segundosRestantes").value(300));
    }

    @Test
    void validarSesionRetorna401ConAlertaCierreAutomaticoTrasSuperarCincoMinutos() throws Exception {
        when(authService.validarSesion(eq("token-expirado")))
                .thenThrow(new SesionExpiradaException(
                        "Alerta: Sesión cerrada automáticamente por inactividad tras superar 5 minutos. Inicie sesión nuevamente."));

        mockMvc.perform(get("/auth/validar-sesion")
                        .header("Authorization", "token-expirado"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value(
                        "Alerta: Sesión cerrada automáticamente por inactividad tras superar 5 minutos. Inicie sesión nuevamente."));
    }

    @Test
    void logoutRetorna200() throws Exception {
        doNothing().when(authService).logout(any());

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer token-logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Sesión cerrada correctamente"));
    }
}
