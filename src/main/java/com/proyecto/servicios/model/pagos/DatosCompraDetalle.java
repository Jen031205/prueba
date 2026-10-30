package com.proyecto.servicios.model.pagos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatosCompraDetalle {

    private Integer idProducto;
    private Long idCliente;
    private String telefono;
    private String referencia;
    private String horaLocal;
    private BigDecimal monto;
    private String upc;
    private String unidad;
}
