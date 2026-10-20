package com.proyecto.servicios.service.usuarios;

import com.proyecto.servicios.model.usuarios.UsuarioActualizacionRequest;
import com.proyecto.servicios.model.usuarios.UsuarioBusquedaRequest;
import com.proyecto.servicios.model.usuarios.UsuarioCreacionRequest;
import com.proyecto.servicios.model.usuarios.UsuarioDesactivarRequest;
import com.proyecto.servicios.model.usuarios.UsuarioResponse;

import java.util.List;

public interface UsuarioService {
    UsuarioResponse crear(UsuarioCreacionRequest request);
    List<UsuarioResponse> buscar(UsuarioBusquedaRequest request);
    UsuarioResponse actualizar(UsuarioActualizacionRequest request);
    UsuarioResponse desactivar(UsuarioDesactivarRequest request);
}
