package com.proyecto.servicios.model.pagos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PuntoredAuthRequest {

    private String username;
    private String password;
    private Integer commerce;
}
