package com.proyecto.servicios.model.clientes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClienteRequest extends ClienteDatosRequest {
    @NotBlank(message = "La CURP es obligatoria")
    @Size(min = 18, max = 18, message = "La CURP debe contener exactamente 18 caracteres")
    @Pattern(
        regexp = "(?i)^[A-Z][AEIOUX][A-Z]{2}\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])[MH](AS|BC|BS|CC|CL|CM|CS|CH|DF|DG|GT|GR|HG|JC|MC|MN|MS|NT|NL|OC|PL|QT|QR|SP|SL|SR|TC|TS|TL|VZ|YN|ZS|NE)[B-DF-HJ-NP-TV-Z]{3}[A-Z0-9]\\d$",
        message = "El formato de la CURP es inválido"
    )
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String curp;

    @NotBlank(message = "El RFC es obligatorio")
    @Size(min = 12, max = 13, message = "El RFC debe contener 12 o 13 caracteres")
    @Pattern(
        regexp = "(?i)^[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{3}$",
        message = "El formato del RFC es inválido"
    )
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.proyecto.servicios.config.StrictStringDeserializer.class)
    private String rfc;
}