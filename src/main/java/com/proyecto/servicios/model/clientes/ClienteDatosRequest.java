package com.proyecto.servicios.model.clientes;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public abstract class ClienteDatosRequest {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[\\p{L}]+(?: [\\p{L}]+)*$", message = "En campo de texto solo debe de ingresar texto y no caracteres especiales para nombre")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String nombre;

    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String segundoNombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido paterno debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[\\p{L}]+(?: [\\p{L}]+)*$", message = "En campo de texto solo debe de ingresar texto y no caracteres especiales para apellido paterno")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String apellidoPaterno;

    @NotBlank(message = "El apellido materno es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido materno debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[\\p{L}]+(?: [\\p{L}]+)*$", message = "En campo de texto solo debe de ingresar texto y no caracteres especiales para apellido materno")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String apellidoMaterno;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento no puede ser una fecha futura")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El sexo es obligatorio")
    @Pattern(regexp = "^(Masculino|Femenino|Otro)$",
             message = "Alerta: El valor ingresado para 'sexo' no es aceptado en el catálogo. Valores permitidos: Masculino, Femenino, Otro")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String sexo;

    @NotBlank(message = "La nacionalidad es obligatoria")
    @Size(min = 2, max = 60, message = "La nacionalidad debe tener entre 2 y 60 caracteres")
    @Pattern(regexp = "^(Mexicana|Mexicano|Estadounidense|Canadiense|Española|Español|Colombiana|Colombiano|Argentina|Argentino|Chilena|Chileno|Peruana|Peruano|Brasileña|Brasileño|Venezolana|Venezolano|Guatemalteca|Guatemalteco|Cubana|Cubano|Francesa|Francés|Alemana|Alemán|Italiana|Italiano)$",
             message = "Alerta: El valor ingresado para 'nacionalidad' no es aceptado en el catálogo. Valores permitidos: Mexicana, Estadounidense, Canadiense, Española, Colombiana, Argentina, etc.")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String nacionalidad;

    @NotBlank(message = "El estado civil es obligatorio")
    @Pattern(regexp = "^(Soltero|Casado|Divorciado|Viudo|Union [Ll]ibre)$",
             message = "Alerta: El valor ingresado para 'estadoCivil' no es aceptado en el catálogo. Valores permitidos: Soltero, Casado, Divorciado, Viudo, Union libre")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String estadoCivil;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico debe tener un formato válido")
    @Size(max = 100, message = "El correo electrónico no puede superar los 100 caracteres")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String correoElectronico;

    @NotBlank(message = "El teléfono móvil es obligatorio")
    @Pattern(regexp = "^\\d{10}$", message = "En campos numéricos solo debe de ingresar números (el teléfono móvil debe contener exactamente 10 dígitos)")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String telefonoMovil;

    @Pattern(regexp = "^$|^\\d{10}$", message = "En campos numéricos solo debe de ingresar números (el teléfono alternativo debe contener exactamente 10 dígitos)")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String telefonoAlternativo;

    @NotBlank(message = "La ocupación es obligatoria")
    @Size(min = 2, max = 80, message = "La ocupación debe tener entre 2 y 80 caracteres")
    @Pattern(regexp = "^[\\p{L}]+(?: [\\p{L}]+)*$", message = "En campo de texto solo debe de ingresar texto y no caracteres especiales para ocupación")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String ocupacion;

    @NotBlank(message = "La empresa es obligatoria")
    @Size(min = 2, max = 100, message = "La empresa debe tener entre 2 y 100 caracteres")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String empresa;

    @NotNull(message = "El ingreso mensual es obligatorio")
    @DecimalMin(value = "0.00", inclusive = false, message = "El ingreso mensual debe ser mayor a cero")
    @Digits(integer = 17, fraction = 2, message = "Alerta: En campos de número solo se aceptan hasta 2 decimales")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictBigDecimalDeserializer.class)
    private BigDecimal ingresoMensual;

    @NotNull(message = "El domicilio es obligatorio")
    @Valid
    private DomicilioRequest domicilio;

    @AssertTrue(message = "El cliente debe tener al menos 18 años")
    public boolean isMayorDeEdad() {
        return fechaNacimiento != null && !fechaNacimiento.isAfter(LocalDate.now().minusYears(18));
    }

    @AssertTrue(message = "En campo de texto solo debe de ingresar texto y no caracteres especiales para segundo nombre (entre 2 y 50 caracteres)")
    public boolean isSegundoNombreValido() {
        if (segundoNombre == null || segundoNombre.trim().isEmpty()) {
            return true;
        }
        String s = segundoNombre.trim();
        return s.length() >= 2 && s.length() <= 50 && s.matches("^[\\p{L}]+(?: [\\p{L}]+)*$");
    }
}