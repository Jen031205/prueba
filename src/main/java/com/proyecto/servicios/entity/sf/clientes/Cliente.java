package com.proyecto.servicios.entity.sf.clientes;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "segundo_nombre", length = 50)
    private String segundoNombre;

    @Column(name = "apellido_paterno", nullable = false, length = 50)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", nullable = false, length = 50)
    private String apellidoMaterno;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "curp", nullable = false, length = 18, unique = true)
    private String curp;

    @Column(name = "rfc", nullable = false, length = 13, unique = true)
    private String rfc;

    @Column(name = "sexo", nullable = false, length = 20)
    private String sexo;

    @Column(name = "nacionalidad", nullable = false, length = 60)
    private String nacionalidad;

    @Column(name = "estado_civil", nullable = false, length = 20)
    private String estadoCivil;

    @Column(name = "correo_electronico", nullable = false, length = 100)
    private String correoElectronico;

    @Column(name = "telefono_movil", nullable = false, length = 10)
    private String telefonoMovil;

    @Column(name = "telefono_alternativo", length = 10)
    private String telefonoAlternativo;

    @Column(name = "ocupacion", nullable = false, length = 80)
    private String ocupacion;

    @Column(name = "empresa", nullable = false, length = 100)
    private String empresa;

    @Column(name = "ingreso_mensual", nullable = false, precision = 19, scale = 2)
    private BigDecimal ingresoMensual;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "domicilio_id", nullable = false, unique = true)
    private Domicilio domicilio;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Cuenta> cuentas = new ArrayList<>();
}