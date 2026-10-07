package com.proyecto.servicios.controller.clientes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.model.clientes.ClienteActualizacionRequest;
import com.proyecto.servicios.model.clientes.ClientePatchRequest;
import com.proyecto.servicios.model.clientes.ClienteRequest;
import com.proyecto.servicios.model.clientes.ClienteResponse;
import com.proyecto.servicios.model.clientes.DomicilioRequest;
import com.proyecto.servicios.service.clientes.ClienteNoEncontradoException;
import com.proyecto.servicios.service.clientes.ClienteService;
import com.proyecto.servicios.service.clientes.CurpDuplicadaException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteService clienteService;

    @Test
    void postClientesRetorna201CuandoSolicitudEsValida() throws Exception {
        ClienteResponse response = new ClienteResponse();
        response.setId(1L);
        response.setNombre("Ana");
        response.setApellidoPaterno("Lopez");
        response.setCurp("GALA900101MDFRNN09");

        when(clienteService.crear(any(ClienteRequest.class))).thenReturn(response);

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitudValida())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Ana"));
    }

    @Test
    void postClientesRetorna400CuandoHayErrorDeValidacion() throws Exception {
        ClienteRequest invalido = solicitudValida();
        invalido.setNombre("123"); // Inválido: números

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detalles").isArray());
    }

    @Test
    void rechazaCualquierDatoSinComillasEnJson() throws Exception {
        String jsonSinComillasEnIngreso = """
            {
              "nombre": "Ana",
              "apellidoPaterno": "Lopez",
              "apellidoMaterno": "Garcia",
              "fechaNacimiento": "1990-01-01",
              "curp": "GALA900101MDFRNN09",
              "rfc": "GALA900101ABC",
              "sexo": "Masculino",
              "nacionalidad": "Mexicana",
              "estadoCivil": "Soltero",
              "correoElectronico": "ana@example.com",
              "telefonoMovil": "5512345678",
              "ocupacion": "Analista",
              "empresa": "Empresa SA",
              "ingresoMensual": 25000.00,
              "domicilio": {
                "calle": "Reforma",
                "numeroExterior": "10",
                "colonia": "Centro",
                "municipio": "Cuauhtemoc",
                "estado": "CDMX",
                "codigoPostal": "06000",
                "pais": "Mexico"
              }
            }
            """;

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSinComillasEnIngreso))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(
                        "Alerta: Para hacer el registro de cualquier dato debes de tomarlo únicamente con comillas \"\"")));
    }

    @Test
    void aceptaRegistroConTodosLosDatosEntreComillas() throws Exception {
        ClienteResponse response = new ClienteResponse();
        response.setId(1L);
        response.setNombre("Ana");

        when(clienteService.crear(any(ClienteRequest.class))).thenReturn(response);

        String jsonConComillas = """
            {
              "nombre": "Ana",
              "apellidoPaterno": "Lopez",
              "apellidoMaterno": "Garcia",
              "fechaNacimiento": "1990-01-01",
              "curp": "GALA900101MDFRNN09",
              "rfc": "GALA900101ABC",
              "sexo": "Masculino",
              "nacionalidad": "Mexicana",
              "estadoCivil": "Soltero",
              "correoElectronico": "ana@example.com",
              "telefonoMovil": "5512345678",
              "ocupacion": "Analista",
              "empresa": "Empresa SA",
              "ingresoMensual": "25000.00",
              "domicilio": {
                "calle": "Reforma",
                "numeroExterior": "10",
                "colonia": "Centro",
                "municipio": "Cuauhtemoc",
                "estado": "CDMX",
                "codigoPostal": "06000",
                "pais": "Mexico"
              }
            }
            """;

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonConComillas))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void postClientesRetorna409CuandoCurpEstaDuplicada() throws Exception {
        when(clienteService.crear(any(ClienteRequest.class)))
                .thenThrow(new CurpDuplicadaException("GALA900101MDFRNN09"));

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitudValida())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Ya existe un cliente registrado con ese CURP: GALA900101MDFRNN09"));
    }

    @Test
    void getClientesRetornaLista() throws Exception {
        ClienteResponse r = new ClienteResponse();
        r.setId(1L);
        r.setNombre("Ana");

        when(clienteService.listar()).thenReturn(List.of(r));

        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Ana"));
    }

    @Test
    void getClientePorIdRetorna200CuandoExiste() throws Exception {
        ClienteResponse r = new ClienteResponse();
        r.setId(1L);
        r.setNombre("Ana");

        when(clienteService.buscarPorId(1L)).thenReturn(r);

        mockMvc.perform(get("/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getClientePorIdRetorna404CuandoNoExiste() throws Exception {
        when(clienteService.buscarPorId(99L))
                .thenThrow(new ClienteNoEncontradoException("id 99"));

        mockMvc.perform(get("/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getClientePorCurpRetornaCliente() throws Exception {
        ClienteResponse r = new ClienteResponse();
        r.setCurp("GALA900101MDFRNN09");

        when(clienteService.buscarPorCurp("GALA900101MDFRNN09")).thenReturn(r);

        mockMvc.perform(get("/clientes/curp/GALA900101MDFRNN09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.curp").value("GALA900101MDFRNN09"));
    }

    @Test
    void getClientePorRfcRetornaCliente() throws Exception {
        ClienteResponse r = new ClienteResponse();
        r.setRfc("GALA900101ABC");

        when(clienteService.buscarPorRfc("GALA900101ABC")).thenReturn(r);

        mockMvc.perform(get("/clientes/rfc/GALA900101ABC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rfc").value("GALA900101ABC"));
    }

    @Test
    void getClientePorCorreoRetornaCliente() throws Exception {
        ClienteResponse r = new ClienteResponse();
        r.setCorreoElectronico("ana@example.com");

        when(clienteService.buscarPorCorreo("ana@example.com")).thenReturn(r);

        mockMvc.perform(get("/clientes/correo/ana@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correoElectronico").value("ana@example.com"));
    }

    @Test
    void getClientePorCuentaRetornaTitular() throws Exception {
        ClienteResponse r = new ClienteResponse();
        r.setNombre("Ana");

        when(clienteService.buscarPorCuenta("4100000000000001")).thenReturn(r);

        mockMvc.perform(get("/clientes/cuenta/4100000000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana"));
    }

    @Test
    void patchClienteRetorna200Ok() throws Exception {
        ClienteResponse r = new ClienteResponse();
        r.setId(1L);
        r.setNombre("Ana");
        r.setTelefonoMovil("5599887766");

        ClientePatchRequest patchRequest = new ClientePatchRequest();
        patchRequest.setTelefonoMovil("5599887766");

        when(clienteService.patch(eq(1L), any(ClientePatchRequest.class))).thenReturn(r);

        mockMvc.perform(patch("/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.telefonoMovil").value("5599887766"));
    }

    @Test
    void deleteClienteRetorna200OkYDesactiva() throws Exception {
        doNothing().when(clienteService).desactivar(1L);

        mockMvc.perform(delete("/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.activo").value(false))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("desactivado correctamente")));
    }

    private static ClienteRequest solicitudValida() {
        ClienteRequest request = new ClienteRequest();
        request.setNombre("Ana");
        request.setApellidoPaterno("Lopez");
        request.setApellidoMaterno("Garcia");
        request.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        request.setCurp("GALA900101MDFRNN09");
        request.setRfc("GALA900101ABC");
        request.setSexo("Masculino");
        request.setNacionalidad("Mexicana");
        request.setEstadoCivil("Soltero");
        request.setCorreoElectronico("ana@example.com");
        request.setTelefonoMovil("5512345678");
        request.setOcupacion("Analista");
        request.setEmpresa("Empresa SA");
        request.setIngresoMensual(new BigDecimal("25000.00"));

        DomicilioRequest domicilio = new DomicilioRequest();
        domicilio.setCalle("Reforma");
        domicilio.setNumeroExterior("10");
        domicilio.setColonia("Centro");
        domicilio.setMunicipio("Cuauhtemoc");
        domicilio.setEstado("CDMX");
        domicilio.setCodigoPostal("06000");
        domicilio.setPais("Mexico");
        request.setDomicilio(domicilio);
        return request;
    }
}
