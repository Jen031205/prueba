package com.proyecto.servicios.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class StrictIntegerDeserializer extends JsonDeserializer<Integer> {

    @Override
    public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            String text = p.getText().trim();
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Alerta: En campos numéricos enteros solo debe de ingresar números válidos entre comillas");
            }
        } else if (p.currentToken() == JsonToken.VALUE_NUMBER_INT) {
            return p.getIntValue();
        }
        String campo = p.currentName() != null ? "'" + p.currentName() + "' " : "";
        throw new IllegalArgumentException(
                "Alerta: Para hacer el registro de cualquier dato debes de tomarlo únicamente con comillas \"\" ("
                        + campo + "debe enviarse entre comillas).");
    }
}
