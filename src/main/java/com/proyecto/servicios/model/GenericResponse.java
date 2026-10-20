package com.proyecto.servicios.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenericResponse {

    private Integer codigo;
    private String mensaje;
    private Object response;

    public GenericResponse() {
    }

    public GenericResponse(Integer codigo, String mensaje) {
        this.codigo = codigo;
        this.mensaje = mensaje;
    }

    public GenericResponse(Integer codigo, String mensaje, Object response) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.response = response;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }
}
