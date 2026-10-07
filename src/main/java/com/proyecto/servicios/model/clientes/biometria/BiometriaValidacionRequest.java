package com.proyecto.servicios.model.clientes.biometria;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.proyecto.servicios.config.StrictBigDecimalDeserializer;
import com.proyecto.servicios.config.StrictStringDeserializer;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class BiometriaValidacionRequest {

    @NotBlank(message = "El vector de embedding actual es obligatorio para validar la biometría")
    @Size(max = 50000, message = "El vector de embedding no debe superar los 50KB")
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private String embedding;

    @DecimalMin(value = "0.00", message = "El umbral de similitud mínimo es 0.00")
    @DecimalMax(value = "1.00", message = "El umbral de similitud máximo es 1.00")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = StrictBigDecimalDeserializer.class)
    private BigDecimal umbral;
}
