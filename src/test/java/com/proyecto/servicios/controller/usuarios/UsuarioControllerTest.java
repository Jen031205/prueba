package com.proyecto.servicios.controller.usuarios;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.controller.clientes.ClienteExceptionHandler;
import com.proyecto.servicios.model.usuarios.UsuarioActualizacionRequest;
import com.proyecto.servicios.model.usuarios.UsuarioBusquedaRequest;
import com.proyecto.servicios.model.usuarios.UsuarioCreacionRequest;
import com.proyecto.servicios.model.usuarios.UsuarioDesactivarRequest;
import com.proyecto.servicios.model.usuarios.UsuarioResponse;
import com.proyecto.servicios.service.usuarios.UsuarioCorreoDuplicadoException;
import com.proyecto.servicios.service.usuarios.UsuarioNoEncontradoException;
import com.proyecto.servicios.service.usuarios.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@Import(ClienteExceptionHandler.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    void postCrearUsuarioRetorna201CuandoSolicitudEsValida() throws Exception {
        UsuarioCreacionRequest request = UsuarioCreacionRequest.builder()
                .nombre("Carlos Mendoza")
                .correo("carlos.mendoza@banco.com")
                .password("Password123!")
                .build();

        UsuarioResponse response = UsuarioResponse.builder()
                .id(10L)
                .nombre("Carlos Mendoza")
                .correo("carlos.mendoza@banco.com")
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(usuarioService.crear(any(UsuarioCreacionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.nombre").value("Carlos Mendoza"))
                .andExpect(jsonPath("$.correo").value("carlos.mendoza@banco.com"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void postCrearUsuarioRetorna400CuandoHayErrorDeValidacion() throws Exception {
        UsuarioCreacionRequest requestInvalido = UsuarioCreacionRequest.builder()
                .nombre("1234") // Nombre inválido con números
                .correo("correo-no-valido") // Formato inválido
                .password("corta") // Menos de 8 caracteres
                .build();

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detalles").isArray());
    }

    @Test
    void postCrearUsuarioRetorna409CuandoCorreoYaExiste() throws Exception {
        UsuarioCreacionRequest request = UsuarioCreacionRequest.builder()
                .nombre("Carlos Mendoza")
                .correo("duplicado@banco.com")
                .password("Password123!")
                .build();

        when(usuarioService.crear(any(UsuarioCreacionRequest.class)))
                .thenThrow(new UsuarioCorreoDuplicadoException(
                        "Ya existe un usuario registrado con el correo electrónico: duplicado@banco.com"));

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(
                        "Ya existe un usuario registrado con el correo electrónico: duplicado@banco.com"));
    }

    @Test
    void postBuscarUsuariosRetorna200ConFiltrosCombinados() throws Exception {
        UsuarioBusquedaRequest request = UsuarioBusquedaRequest.builder()
                .correo("banco.com")
                .activo(true)
                .build();

        UsuarioResponse user1 = UsuarioResponse.builder()
                .id(1L)
                .nombre("Admin")
                .correo("admin@banco.com")
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(usuarioService.buscar(any(UsuarioBusquedaRequest.class))).thenReturn(List.of(user1));

        mockMvc.perform(post("/usuarios/buscar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].correo").value("admin@banco.com"));
    }

    @Test
    void postBuscarUsuariosRetorna200SinCuerpo() throws Exception {
        UsuarioResponse user1 = UsuarioResponse.builder()
                .id(1L)
                .nombre("Admin")
                .correo("admin@banco.com")
                .activo(true)
                .build();

        when(usuarioService.buscar(any())).thenReturn(List.of(user1));

        mockMvc.perform(post("/usuarios/buscar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void putActualizarUsuarioRetorna200() throws Exception {
        UsuarioActualizacionRequest request = UsuarioActualizacionRequest.builder()
                .id(1L)
                .nombre("Admin Actualizado")
                .build();

        UsuarioResponse response = UsuarioResponse.builder()
                .id(1L)
                .nombre("Admin Actualizado")
                .correo("admin@banco.com")
                .activo(true)
                .build();

        when(usuarioService.actualizar(any(UsuarioActualizacionRequest.class))).thenReturn(response);

        mockMvc.perform(put("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Admin Actualizado"));
    }

    @Test
    void putActualizarUsuarioRetorna404CuandoNoExiste() throws Exception {
        UsuarioActualizacionRequest request = UsuarioActualizacionRequest.builder()
                .id(999L)
                .nombre("Inexistente")
                .build();

        when(usuarioService.actualizar(any(UsuarioActualizacionRequest.class)))
                .thenThrow(new UsuarioNoEncontradoException(
                        "Usuario no encontrado con el identificador ID: 999"));

        mockMvc.perform(put("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deleteEliminarUsuarioRetorna200() throws Exception {
        UsuarioDesactivarRequest request = UsuarioDesactivarRequest.builder()
                .id(1L)
                .build();

        UsuarioResponse response = UsuarioResponse.builder()
                .id(1L)
                .nombre("Admin")
                .activo(false)
                .build();

        when(usuarioService.desactivar(any(UsuarioDesactivarRequest.class))).thenReturn(response);

        mockMvc.perform(delete("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    void patchDesactivarUsuarioRetorna200() throws Exception {
        UsuarioDesactivarRequest request = UsuarioDesactivarRequest.builder()
                .id(1L)
                .build();

        UsuarioResponse response = UsuarioResponse.builder()
                .id(1L)
                .nombre("Admin")
                .activo(false)
                .build();

        when(usuarioService.desactivar(any(UsuarioDesactivarRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/usuarios/desactivar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }
}
