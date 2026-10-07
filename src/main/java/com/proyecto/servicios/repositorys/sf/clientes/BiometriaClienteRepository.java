package com.proyecto.servicios.repositorys.sf.clientes;

import com.proyecto.servicios.entity.sf.clientes.BiometriaCliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BiometriaClienteRepository extends JpaRepository<BiometriaCliente, Long> {
    Optional<BiometriaCliente> findByClienteId(Long clienteId);
    boolean existsByClienteId(Long clienteId);
    void deleteByClienteId(Long clienteId);
}
