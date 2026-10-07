package com.proyecto.servicios.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.math.BigDecimal;

public class StrictBigDecimalDeserializer extends JsonDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() != JsonToken.VALUE_STRING) {
            String campo = p.currentName() != null ? "'" + p.currentName() + "' " : "";
            throw new IllegalArgumentException(
                    "Alerta: Para hacer el registro de cualquier dato debes de tomarlo únicamente con comillas \"\" ("
                            + campo + "debe enviarse entre comillas, ejemplo: \"35000.00\").");
        }
        String text = p.getText().trim();
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Alerta: En campos numéricos solo debe de ingresar números válidos entre comillas");
        }
    }
}
