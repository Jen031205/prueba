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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SesionUsuarioRepository sesionUsuarioRepository;

    @InjectMocks
    private AuthService authService;

    private Usuario usuarioPrueba;

    @BeforeEach
    void setUp() {
        usuarioPrueba = new Usuario();
        usuarioPrueba.setId(1L);
        usuarioPrueba.setCorreo("admin@banco.com");
        usuarioPrueba.setPassword("Admin123!");
        usuarioPrueba.setNombre("Administrador");
        usuarioPrueba.setActivo(true);
    }

    @Test
    void loginExitosoGeneraTokenYSesionConCincoMinutos() {
        LoginRequest request = new LoginRequest();
        request.setCorreo("admin@banco.com");
        request.setPassword("Admin123!");

        when(usuarioRepository.findByCorreoIgnoreCase("admin@banco.com")).thenReturn(Optional.of(usuarioPrueba));

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("admin@banco.com", response.getCorreo());
        assertEquals(5, response.getTiempoInactividadMinutos());
        verify(sesionUsuarioRepository).desactivarSesionesPrevias(1L);
        verify(sesionUsuarioRepository).save(any(SesionUsuario.class));
    }

    @Test
    void loginFallaCuandoContrasenaOEmailSonDistintos() {
        LoginRequest request = new LoginRequest();
        request.setCorreo("admin@banco.com");
        request.setPassword("PasswordErronea123");

        when(usuarioRepository.findByCorreoIgnoreCase("admin@banco.com")).thenReturn(Optional.of(usuarioPrueba));

        CredencialesInvalidasException ex = assertThrows(CredencialesInvalidasException.class,
                () -> authService.login(request));

        assertTrue(ex.getMessage().contains("Alerta: El correo y la contraseña son distintos a los registrados"));
    }

    @Test
    void loginFallaCuandoUsuarioNoExiste() {
        LoginRequest request = new LoginRequest();
        request.setCorreo("desconocido@banco.com");
        request.setPassword("Cualquiera123");

        when(usuarioRepository.findByCorreoIgnoreCase(anyString())).thenReturn(Optional.empty());

        CredencialesInvalidasException ex = assertThrows(CredencialesInvalidasException.class,
                () -> authService.login(request));

        assertTrue(ex.getMessage().contains("Alerta: El correo y la contraseña son distintos a los registrados"));
    }

    @Test
    void validarSesionActivaReiniciaTemporizadorDeCincoMinutos() {
        SesionUsuario sesion = new SesionUsuario();
        sesion.setToken("token-activo-123");
        sesion.setCorreo("admin@banco.com");
        sesion.setActiva(true);
        sesion.setFechaInicio(LocalDateTime.now().minusMinutes(2));
        sesion.setUltimaActividad(LocalDateTime.now().minusMinutes(2));
        sesion.setFechaExpiracion(LocalDateTime.now().plusMinutes(3));

        when(sesionUsuarioRepository.findByToken("token-activo-123")).thenReturn(Optional.of(sesion));

        SesionEstadoResponse estado = authService.validarSesion("Bearer token-activo-123");

        assertNotNull(estado);
        assertTrue(estado.isActiva());
        assertTrue(estado.getMensaje().contains("reiniciado"));
        verify(sesionUsuarioRepository).save(sesion);
    }

    @Test
    void validarSesionLanzaAlertaCierreAutomaticoSiSuperoCincoMinutos() {
        SesionUsuario sesionExpirada = new SesionUsuario();
        sesionExpirada.setToken("token-expirado");
        sesionExpirada.setCorreo("admin@banco.com");
        sesionExpirada.setActiva(true);
        sesionExpirada.setFechaInicio(LocalDateTime.now().minusMinutes(10));
        sesionExpirada.setUltimaActividad(LocalDateTime.now().minusMinutes(6));
        sesionExpirada.setFechaExpiracion(LocalDateTime.now().minusMinutes(1)); // ya expiró

        when(sesionUsuarioRepository.findByToken("token-expirado")).thenReturn(Optional.of(sesionExpirada));

        SesionExpiradaException ex = assertThrows(SesionExpiradaException.class,
                () -> authService.validarSesion("token-expirado"));

        assertTrue(ex.getMessage().contains("Alerta: Sesión cerrada automáticamente por inactividad tras superar 5 minutos"));
    }

    @Test
    void logoutDesactivaLaSesion() {
        SesionUsuario sesion = new SesionUsuario();
        sesion.setToken("token-logout");
        sesion.setActiva(true);

        when(sesionUsuarioRepository.findByToken("token-logout")).thenReturn(Optional.of(sesion));

        authService.logout("token-logout");

        assertEquals(false, sesion.isActiva());
        verify(sesionUsuarioRepository).save(sesion);
    }

    @Test
    void registrarUsuarioFallaSiYaExisteElCorreo() {
        RegistroUsuarioRequest reg = new RegistroUsuarioRequest();
        reg.setCorreo("admin@banco.com");
        reg.setPassword("Pass123!");
        reg.setNombre("Otro");

        when(usuarioRepository.existsByCorreoIgnoreCase("admin@banco.com")).thenReturn(true);

        ErrorValidacionException ex = assertThrows(ErrorValidacionException.class,
                () -> authService.registrar(reg));

        assertTrue(ex.getMessage().contains("Ya existe un usuario registrado"));
    }
}
