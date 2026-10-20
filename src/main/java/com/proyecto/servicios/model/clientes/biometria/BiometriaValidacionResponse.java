package com.proyecto.servicios.model.clientes.biometria;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BiometriaValidacionResponse {
    private Long clienteId;
    private boolean coincide;
    private double similitud;
    private BigDecimal porcentajeSimilitud;
    private double umbralAplicado;
    private String mensaje;

    public BiometriaValidacionResponse() {
    }

    public BiometriaValidacionResponse(Long clienteId, boolean coincide, double similitud,
                                       BigDecimal porcentajeSimilitud, double umbralAplicado, String mensaje) {
        this.clienteId = clienteId;
        this.coincide = coincide;
        this.similitud = similitud;
        this.porcentajeSimilitud = porcentajeSimilitud;
        this.umbralAplicado = umbralAplicado;
        this.mensaje = mensaje;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public boolean isCoincide() {
        return coincide;
    }

    public void setCoincide(boolean coincide) {
        this.coincide = coincide;
    }

    public double getSimilitud() {
        return similitud;
    }

    public void setSimilitud(double similitud) {
        this.similitud = similitud;
    }

    public BigDecimal getPorcentajeSimilitud() {
        return porcentajeSimilitud;
    }

    public void setPorcentajeSimilitud(BigDecimal porcentajeSimilitud) {
        this.porcentajeSimilitud = porcentajeSimilitud;
    }

    public double getUmbralAplicado() {
        return umbralAplicado;
    }

    public void setUmbralAplicado(double umbralAplicado) {
        this.umbralAplicado = umbralAplicado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
