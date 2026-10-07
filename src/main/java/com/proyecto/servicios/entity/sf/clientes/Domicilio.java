package com.proyecto.servicios.entity.sf.clientes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "domicilios")
@Getter
@Setter
@NoArgsConstructor
public class Domicilio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "calle", nullable = false, length = 100)
    private String calle;

    @Column(name = "numero_exterior", nullable = false, length = 15)
    private String numeroExterior;

    @Column(name = "numero_interior", length = 15)
    private String numeroInterior;

    @Column(name = "colonia", nullable = false, length = 80)
    private String colonia;

    @Column(name = "municipio", nullable = false, length = 80)
    private String municipio;

    @Column(name = "estado", nullable = false, length = 80)
    private String estado;

    @Column(name = "codigo_postal", nullable = false, length = 5)
    private String codigoPostal;

    @Column(name = "pais", nullable = false, length = 60)
    private String pais;
}