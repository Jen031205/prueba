package com.proyecto.servicios.repositorys.sf.clientes;

import com.proyecto.servicios.entity.sf.clientes.Domicilio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomicilioRepository extends JpaRepository<Domicilio, Long> {
}
