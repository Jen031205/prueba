package com.proyecto.servicios.entity.sf.clientes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "biometria_clientes")
@Getter
@Setter
@NoArgsConstructor
public class BiometriaCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false, unique = true)
    private Cliente cliente;

    @Column(name = "tipo_biometria", nullable = false, length = 50)
    private String tipoBiometria = "FACIAL_MEDIAPIPE";

    @Column(name = "embedding", nullable = false, columnDefinition = "TEXT")
    private String embedding;

    @Column(name = "confianza_deteccion", nullable = false, precision = 5, scale = 2)
    private BigDecimal confianzaDeteccion;

    @Column(name = "rostros_detectados", nullable = false)
    private Integer rostrosDetectados = 1;

    @Column(name = "foto_base64", columnDefinition = "TEXT")
    private String fotoBase64;

    @Column(name = "fecha_captura", nullable = false, updatable = false)
    private LocalDateTime fechaCaptura;

    @Column(name = "estatus", nullable = false, length = 20)
    private String estatus = "VIGENTE";
}
