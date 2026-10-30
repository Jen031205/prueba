package com.proyecto.servicios.service.pagos;

import com.proyecto.servicios.model.pagos.PagoCompraRequest;
import com.proyecto.servicios.model.pagos.PagoCompraResponse;
import com.proyecto.servicios.model.pagos.Solicitar2FARequest;
import com.proyecto.servicios.model.pagos.Solicitar2FAResponse;

public interface PagoService {

    PagoCompraResponse procesarPago(PagoCompraRequest request);

    Solicitar2FAResponse solicitarDobleAutenticacion(Solicitar2FARequest request);
}
