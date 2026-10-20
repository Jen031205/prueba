package com.proyecto.servicios.service.usuarios;

import com.proyecto.servicios.entity.sf.Usuario;
import com.proyecto.servicios.model.usuarios.UsuarioActualizacionRequest;
import com.proyecto.servicios.model.usuarios.UsuarioBusquedaRequest;
import com.proyecto.servicios.model.usuarios.UsuarioCreacionRequest;
import com.proyecto.servicios.model.usuarios.UsuarioDesactivarRequest;
import com.proyecto.servicios.model.usuarios.UsuarioResponse;
import com.proyecto.servicios.repositorys.sf.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuarioPrueba;

    @BeforeEach
    void setUp() {
        usuarioPrueba = new Usuario();
        usuarioPrueba.setId(1L);
        usuarioPrueba.setCorreo("test@banco.com");
        usuarioPrueba.setNombre("Usuario Prueba");
        usuarioPrueba.setPassword("Password123!");
        usuarioPrueba.setActivo(true);
        usuarioPrueba.setFechaCreacion(LocalDateTime.now());
    }

    @Test
    void crearUsuarioExitoso() {
        UsuarioCreacionRequest request = UsuarioCreacionRequest.builder()
                .correo("nuevo@banco.com")
                .nombre("Nuevo Usuario")
                .password("Password123!")
                .build();

        when(usuarioRepository.existsByCorreoIgnoreCase("nuevo@banco.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(2L);
            return u;
        });

        UsuarioResponse response = usuarioService.crear(request);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("nuevo@banco.com", response.getCorreo());
        assertTrue(response.isActivo());
    }

    @Test
    void crearUsuarioFallaCuandoCorreoDuplicado() {
        UsuarioCreacionRequest request = UsuarioCreacionRequest.builder()
                .correo("test@banco.com")
                .nombre("Usuario Duplicado")
                .password("Password123!")
                .build();

        when(usuarioRepository.existsByCorreoIgnoreCase("test@banco.com")).thenReturn(true);

        assertThrows(UsuarioCorreoDuplicadoException.class, () -> usuarioService.crear(request));
    }

    @Test
    @SuppressWarnings("unchecked")
    void buscarUsuariosConFiltros() {
        UsuarioBusquedaRequest request = UsuarioBusquedaRequest.builder()
                .correo("test")
                .activo(true)
                .build();

        when(usuarioRepository.findAll(any(Specification.class))).thenReturn(List.of(usuarioPrueba));

        List<UsuarioResponse> resultados = usuarioService.buscar(request);

        assertNotNull(resultados);
        assertEquals(1, resultados.size());
        assertEquals("test@banco.com", resultados.get(0).getCorreo());
    }

    @Test
    void actualizarUsuarioExitoso() {
        UsuarioActualizacionRequest request = UsuarioActualizacionRequest.builder()
                .id(1L)
                .nombre("Nombre Modificado")
                .correo("nuevo.correo@banco.com")
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioPrueba));
        when(usuarioRepository.existsByCorreoIgnoreCaseAndIdNot("nuevo.correo@banco.com", 1L)).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioResponse response = usuarioService.actualizar(request);

        assertNotNull(response);
        assertEquals("Nombre Modificado", response.getNombre());
        assertEquals("nuevo.correo@banco.com", response.getCorreo());
    }

    @Test
    void actualizarUsuarioFallaCuandoNoExiste() {
        UsuarioActualizacionRequest request = UsuarioActualizacionRequest.builder()
                .id(99L)
                .nombre("Inexistente")
                .build();

        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class, () -> usuarioService.actualizar(request));
    }

    @Test
    void actualizarUsuarioFallaCuandoCorreoDuplicado() {
        UsuarioActualizacionRequest request = UsuarioActualizacionRequest.builder()
                .id(1L)
                .correo("otro@banco.com")
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioPrueba));
        when(usuarioRepository.existsByCorreoIgnoreCaseAndIdNot("otro@banco.com", 1L)).thenReturn(true);

        assertThrows(UsuarioCorreoDuplicadoException.class, () -> usuarioService.actualizar(request));
    }

    @Test
    void desactivarUsuarioExitoso() {
        UsuarioDesactivarRequest request = UsuarioDesactivarRequest.builder()
                .id(1L)
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioPrueba));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioResponse response = usuarioService.desactivar(request);

        assertNotNull(response);
        assertFalse(response.isActivo());
    }

    @Test
    void desactivarUsuarioFallaCuandoNoExiste() {
        UsuarioDesactivarRequest request = UsuarioDesactivarRequest.builder()
                .id(999L)
                .build();

        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class, () -> usuarioService.desactivar(request));
    }
}
