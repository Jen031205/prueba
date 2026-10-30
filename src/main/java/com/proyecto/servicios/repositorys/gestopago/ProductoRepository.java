package com.proyecto.servicios.repositorys.gestopago;

import com.proyecto.servicios.entity.gestopago.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    Optional<Producto> findByIdProducto(Integer idProducto);
    Optional<Producto> findByIdServicioAndIdProducto(Integer idServicio, Integer idProducto);
}
