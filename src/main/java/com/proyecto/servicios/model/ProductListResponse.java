package com.proyecto.servicios.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.json.XML;

@Getter
public class ProductListResponse {

    private final JsonNode content;
    private final String contentType;

    public ProductListResponse(String content, String contentType) {
        this.content = toJson(content);
        this.contentType = contentType;
    }

    public ProductListResponse(JsonNode content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    private JsonNode toJson(String value) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(XML.toJSONObject(value).toString());
        } catch (Exception exception) {
            return new ObjectMapper().getNodeFactory().textNode(value);
        }
    }
}
