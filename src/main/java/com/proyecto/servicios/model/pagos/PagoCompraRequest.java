package com.proyecto.servicios.model.pagos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PagoCompraRequest {

    @NotBlank(message = "El número de cuenta es obligatorio")
    @Pattern(regexp = "^\\d{16}$", message = "El número de cuenta debe tener exactamente 16 dígitos")
    @JsonAlias({"numeroCuenta", "num_cuenta", "numero_cuenta"})
    private String numCuenta;

    @NotNull(message = "El id del cliente es obligatorio")
    @JsonAlias({"clienteId", "id_cliente", "cliente_id"})
    private Long idCliente;

    @NotNull(message = "El id del producto es obligatorio")
    @JsonAlias({"productoId", "idDelProducto", "id_producto", "producto_id"})
    private Integer idProducto;

    @JsonAlias({"id_servicio", "servicioId", "servicio_id"})
    private Integer idServicio;

    @JsonAlias({"idempotencyKey", "idempotency_key", "folioCliente", "clave_idempotencia"})
    private String claveIdempotencia;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    private BigDecimal monto;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\d{10}$", message = "El teléfono debe contener exactamente 10 dígitos numéricos")
    private String telefono;

    @NotBlank(message = "La referencia del comprobante de pago es obligatoria")
    private String referencia;

    @JsonAlias({"dobleAutenticacion", "codigo2FA", "codigo2fa", "doble_autentificacion", "doble_autenticacion"})
    private String dobleAutentificacion;

    @JsonAlias({"unidades"})
    private String unidad;
}
