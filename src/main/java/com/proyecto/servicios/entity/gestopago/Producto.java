package com.proyecto.servicios.entity.gestopago;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos")
@Getter
@Setter
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "servicio", length = 100)
    private String servicio;

    @Column(name = "producto", length = 255)
    private String producto;

    @Column(name = "id_servicio")
    private Integer idServicio;

    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "id_cat_tipo_servicio")
    private Integer idCatTipoServicio;

    @Column(name = "tipo_front", length = 50)
    private String tipoFront;

    @Column(name = "precio", precision = 15, scale = 2)
    private BigDecimal precio;

    @Column(name = "show_ayuda")
    private Boolean showAyuda;

    @Column(name = "tipo_referencia", length = 50)
    private String tipoReferencia;

    @Column(name = "has_digito_verificador")
    private Boolean hasDigitoVerificador;

    @Column(name = "leyenda", columnDefinition = "TEXT")
    private String leyenda;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}