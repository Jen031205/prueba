package com.proyecto.servicios.service.pagos;

import com.proyecto.servicios.model.pagos.DatosCompraDetalle;

public interface CifradoService {

    String cifrar(DatosCompraDetalle datos);

    String generarUpc();

    String obtenerHoraLocal();
}
