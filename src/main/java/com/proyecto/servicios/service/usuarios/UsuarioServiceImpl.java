package com.proyecto.servicios.service.usuarios;

import com.proyecto.servicios.entity.sf.Usuario;
import com.proyecto.servicios.model.usuarios.UsuarioActualizacionRequest;
import com.proyecto.servicios.model.usuarios.UsuarioBusquedaRequest;
import com.proyecto.servicios.model.usuarios.UsuarioCreacionRequest;
import com.proyecto.servicios.model.usuarios.UsuarioDesactivarRequest;
import com.proyecto.servicios.model.usuarios.UsuarioResponse;
import com.proyecto.servicios.repositorys.sf.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioCreacionRequest request) {
        String correoNormalizado = request.getCorreo().trim();
        if (usuarioRepository.existsByCorreoIgnoreCase(correoNormalizado)) {
            log.warn("Conflicto al crear usuario: ya existe correo='{}'", correoNormalizado);
            throw new UsuarioCorreoDuplicadoException(
                    "Ya existe un usuario registrado con el correo electrónico: " + correoNormalizado);
        }

        Usuario usuario = new Usuario();
        usuario.setCorreo(correoNormalizado);
        usuario.setNombre(request.getNombre().trim());
        usuario.setPassword(request.getPassword());
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario creado exitosamente con ID={}", guardado.getId());
        return mapToResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> buscar(UsuarioBusquedaRequest request) {
        Specification<Usuario> spec = UsuarioSpecification.conFiltros(request);
        List<Usuario> usuarios = usuarioRepository.findAll(spec);
        return usuarios.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public UsuarioResponse actualizar(UsuarioActualizacionRequest request) {
        Usuario usuario = usuarioRepository.findById(request.getId())
                .orElseThrow(() -> new UsuarioNoEncontradoException(
                        "Usuario no encontrado con el identificador ID: " + request.getId()));

        if (request.getCorreo() != null && !request.getCorreo().isBlank()) {
            String nuevoCorreo = request.getCorreo().trim();
            if (usuarioRepository.existsByCorreoIgnoreCaseAndIdNot(nuevoCorreo, usuario.getId())) {
                log.warn("Conflicto al actualizar usuario ID={}: correo='{}' ya en uso", usuario.getId(), nuevoCorreo);
                throw new UsuarioCorreoDuplicadoException(
                        "El correo electrónico ya se encuentra en uso por otro usuario: " + nuevoCorreo);
            }
            usuario.setCorreo(nuevoCorreo);
        }

        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            usuario.setNombre(request.getNombre().trim());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPassword(request.getPassword());
        }

        if (request.getActivo() != null) {
            usuario.setActivo(request.getActivo());
        }

        // Se preservan los campos controlados por el servidor (id y fechaCreacion)
        Usuario actualizado = usuarioRepository.save(usuario);
        log.info("Usuario ID={} actualizado exitosamente", actualizado.getId());
        return mapToResponse(actualizado);
    }

    @Override
    @Transactional
    public UsuarioResponse desactivar(UsuarioDesactivarRequest request) {
        Usuario usuario = usuarioRepository.findById(request.getId())
                .orElseThrow(() -> new UsuarioNoEncontradoException(
                        "Usuario no encontrado con el identificador ID: " + request.getId()));

        usuario.setActivo(false);
        Usuario desactivado = usuarioRepository.save(usuario);
        log.info("Usuario ID={} desactivado (baja lógica)", desactivado.getId());
        return mapToResponse(desactivado);
    }

    private UsuarioResponse mapToResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .correo(usuario.getCorreo())
                .nombre(usuario.getNombre())
                .activo(usuario.isActivo())
                .fechaCreacion(usuario.getFechaCreacion())
                .build();
    }
}
