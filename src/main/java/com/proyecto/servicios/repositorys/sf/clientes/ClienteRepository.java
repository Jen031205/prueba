package com.proyecto.servicios.repositorys.sf.clientes;

import com.proyecto.servicios.entity.sf.clientes.Cliente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByCurp(String curp);
    boolean existsByRfc(String rfc);
    boolean existsByCorreoElectronicoIgnoreCase(String correoElectronico);
    Optional<Cliente> findByCurp(String curp);
    Optional<Cliente> findByRfc(String rfc);
    Optional<Cliente> findByCorreoElectronicoIgnoreCase(String correoElectronico);

    @Override
    @EntityGraph(attributePaths = {"domicilio", "cuentas"})
    List<Cliente> findAll();

    @EntityGraph(attributePaths = {"domicilio", "cuentas"})
    List<Cliente> findByActivoTrue();

    @EntityGraph(attributePaths = {"domicilio", "cuentas"})
    List<Cliente> findByFechaRegistroBetween(LocalDateTime desde, LocalDateTime hasta);
}