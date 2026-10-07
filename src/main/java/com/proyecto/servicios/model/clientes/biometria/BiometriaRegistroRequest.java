package com.proyecto.servicios.model.clientes.biometria;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.proyecto.servicios.config.StrictBigDecimalDeserializer;
import com.proyecto.servicios.config.StrictIntegerDeserializer;
import com.proyecto.servicios.config.StrictStringDeserializer;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class BiometriaRegistroRequest {

    @JsonDeserialize(using = StrictStringDeserializer.class)
    private String tipoBiometria;

    @NotNull(message = "El nivel de confianza de detección es obligatorio")
    @DecimalMin(value = "0.00", message = "La confianza de detección no puede ser menor a 0.00")
    @DecimalMax(value = "100.00", message = "La confianza de detección no puede ser mayor a 100.00")
    @Digits(integer = 3, fraction = 2, message = "Alerta: En campos de número solo se aceptan hasta 2 decimales")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = StrictBigDecimalDeserializer.class)
    private BigDecimal confianzaDeteccion;

    @NotNull(message = "El número de rostros detectados es obligatorio")
    @Min(value = 1, message = "Debe detectarse al menos un rostro para registrar la biometría")
    @JsonDeserialize(using = StrictIntegerDeserializer.class)
    private Integer rostrosDetectados;

    @NotBlank(message = "El vector de embedding de MediaPipe es obligatorio")
    @Size(max = 50000, message = "El vector de embedding no debe superar los 50KB")
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private String embedding;

    @Size(max = 5000000, message = "La imagen en Base64 no debe superar los 5MB")
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private String fotoBase64;
}
