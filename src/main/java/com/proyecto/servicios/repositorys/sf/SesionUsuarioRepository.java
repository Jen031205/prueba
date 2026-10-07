package com.proyecto.servicios.repositorys.sf;

import com.proyecto.servicios.entity.sf.SesionUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SesionUsuarioRepository extends JpaRepository<SesionUsuario, Long> {

    Optional<SesionUsuario> findByToken(String token);

    @Modifying
    @Query("UPDATE SesionUsuario s SET s.activa = false WHERE s.usuario.id = :usuarioId AND s.activa = true")
    void desactivarSesionesPrevias(@Param("usuarioId") Long usuarioId);

    @Modifying
    @Query("UPDATE SesionUsuario s SET s.activa = false WHERE s.activa = true AND s.fechaExpiracion < :ahora")
    int cerrarSesionesExpiradas(@Param("ahora") LocalDateTime ahora);
}
