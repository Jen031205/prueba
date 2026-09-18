package com.proyecto.servicios.model;

import lombok.Getter;

@Getter
public class ProductListResponse {

    private final String content;
    private final String contentType;

    public ProductListResponse(String content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }
}
