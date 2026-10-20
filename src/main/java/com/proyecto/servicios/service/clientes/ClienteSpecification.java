package com.proyecto.servicios.service.clientes;

import com.proyecto.servicios.entity.sf.clientes.Cliente;
import com.proyecto.servicios.model.clientes.ClienteBusquedaRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ClienteSpecification {

    public static Specification<Cliente> conFiltros(ClienteBusquedaRequest request) {
        return (root, query, cb) -> {
            if (request == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (request.getCurp() != null && !request.getCurp().isBlank()) {
                predicates.add(cb.like(
                        cb.upper(root.get("curp")),
                        "%" + request.getCurp().trim().toUpperCase() + "%"
                ));
            }

            if (request.getRfc() != null && !request.getRfc().isBlank()) {
                predicates.add(cb.like(
                        cb.upper(root.get("rfc")),
                        "%" + request.getRfc().trim().toUpperCase() + "%"
                ));
            }

            if (request.getNombre() != null && !request.getNombre().isBlank()) {
                String term = "%" + request.getNombre().trim().toLowerCase() + "%";
                Predicate pNombre = cb.like(cb.lower(root.get("nombre")), term);
                Predicate pSegundoNombre = cb.like(cb.lower(root.get("segundoNombre")), term);
                Predicate pPaterno = cb.like(cb.lower(root.get("apellidoPaterno")), term);
                Predicate pMaterno = cb.like(cb.lower(root.get("apellidoMaterno")), term);
                predicates.add(cb.or(pNombre, pSegundoNombre, pPaterno, pMaterno));
            }

            if (request.getCorreoElectronico() != null && !request.getCorreoElectronico().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("correoElectronico")),
                        "%" + request.getCorreoElectronico().trim().toLowerCase() + "%"
                ));
            }

            if (request.getActivo() != null) {
                predicates.add(cb.equal(root.get("activo"), request.getActivo()));
            }

            if (request.getDesde() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("fechaRegistro"),
                        request.getDesde().atStartOfDay()
                ));
            }

            if (request.getHasta() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("fechaRegistro"),
                        request.getHasta().atTime(LocalTime.MAX)
                ));
            }

            if (request.getNumeroCuenta() != null && !request.getNumeroCuenta().isBlank()) {
                var cuentasJoin = root.join("cuentas", jakarta.persistence.criteria.JoinType.LEFT);
                predicates.add(cb.like(cuentasJoin.get("numeroCuenta"), "%" + request.getNumeroCuenta().trim() + "%"));
                query.distinct(true);
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
