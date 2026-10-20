package com.proyecto.servicios.service.usuarios;

import com.proyecto.servicios.entity.sf.Usuario;
import com.proyecto.servicios.model.usuarios.UsuarioBusquedaRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UsuarioSpecification {

    public static Specification<Usuario> conFiltros(UsuarioBusquedaRequest request) {
        return (root, query, cb) -> {
            if (request == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (request.getId() != null) {
                predicates.add(cb.equal(root.get("id"), request.getId()));
            }

            if (request.getCorreo() != null && !request.getCorreo().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("correo")),
                        "%" + request.getCorreo().trim().toLowerCase() + "%"
                ));
            }

            if (request.getNombre() != null && !request.getNombre().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("nombre")),
                        "%" + request.getNombre().trim().toLowerCase() + "%"
                ));
            }

            if (request.getActivo() != null) {
                predicates.add(cb.equal(root.get("activo"), request.getActivo()));
            }

            if (request.getFechaCreacionDesde() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaCreacion"), request.getFechaCreacionDesde()));
            }

            if (request.getFechaCreacionHasta() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaCreacion"), request.getFechaCreacionHasta()));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
