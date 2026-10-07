package com.proyecto.servicios.repositorys.sf.clientes;

import com.proyecto.servicios.entity.sf.clientes.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);
    List<Cuenta> findByEstatus(String estatus);
    List<Cuenta> findByClienteId(Long clienteId);
}