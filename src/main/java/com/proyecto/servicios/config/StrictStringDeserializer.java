package com.proyecto.servicios.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class StrictStringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() != JsonToken.VALUE_STRING) {
            String campo = p.currentName() != null ? "'" + p.currentName() + "' " : "";
            throw new IllegalArgumentException(
                    "Alerta: Para hacer el registro de cualquier dato debes de tomarlo únicamente con comillas \"\" ("
                            + campo + "debe enviarse entre comillas).");
        }
        return p.getText();
    }
}
