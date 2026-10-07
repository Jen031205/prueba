package com.proyecto.servicios.service.clientes;

import com.proyecto.servicios.entity.sf.clientes.Cliente;
import com.proyecto.servicios.entity.sf.clientes.Cuenta;
import com.proyecto.servicios.entity.sf.clientes.Domicilio;
import com.proyecto.servicios.model.clientes.ClienteActualizacionRequest;
import com.proyecto.servicios.model.clientes.ClientePatchRequest;
import com.proyecto.servicios.model.clientes.ClienteRequest;
import com.proyecto.servicios.model.clientes.ClienteResponse;
import com.proyecto.servicios.model.clientes.DomicilioRequest;
import com.proyecto.servicios.repositorys.sf.clientes.ClienteRepository;
import com.proyecto.servicios.repositorys.sf.clientes.CuentaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void crearGuardaClienteYCuentaActivaConSaldoCero() {
        when(clienteRepository.existsByCurp("GALA900101MDFRNN09")).thenReturn(false);
        when(clienteRepository.existsByRfc("GALA900101ABC")).thenReturn(false);
        when(clienteRepository.existsByCorreoElectronicoIgnoreCase("ana@example.com")).thenReturn(false);
        when(clienteRepository.saveAndFlush(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente cliente = invocation.getArgument(0);
            cliente.setId(12L);
            return cliente;
        });
        when(cuentaRepository.saveAndFlush(any(Cuenta.class))).thenAnswer(invocation -> invocation.getArgument(0));
        org.mockito.Mockito.doAnswer(invocation -> {
            Cuenta cuenta = invocation.getArgument(0);
            cuenta.setNumeroCuenta("4100000000000001");
            return null;
        }).when(entityManager).refresh(any(Cuenta.class));

        ClienteResponse response = clienteService.crear(solicitudValida());

        assertEquals(12L, response.getId());
        assertEquals(1, response.getCuentas().size());
        assertEquals("4100000000000001", response.getCuentas().get(0).getNumeroCuenta());
        assertEquals("ACTIVA", response.getCuentas().get(0).getEstatus());
        assertEquals(BigDecimal.ZERO, response.getCuentas().get(0).getSaldo());
        verify(clienteRepository).saveAndFlush(any(Cliente.class));
        verify(cuentaRepository).saveAndFlush(any(Cuenta.class));
    }

    @Test
    void crearRechazaCurpDuplicada() {
        when(clienteRepository.existsByCurp("GALA900101MDFRNN09")).thenReturn(true);

        assertThrows(CurpDuplicadaException.class, () -> clienteService.crear(solicitudValida()));

        verify(clienteRepository, never()).saveAndFlush(any(Cliente.class));
        verify(cuentaRepository, never()).saveAndFlush(any(Cuenta.class));
    }

    @Test
    void crearRechazaRfcDuplicado() {
        when(clienteRepository.existsByCurp("GALA900101MDFRNN09")).thenReturn(false);
        when(clienteRepository.existsByRfc("GALA900101ABC")).thenReturn(true);

        assertThrows(RfcDuplicadoException.class, () -> clienteService.crear(solicitudValida()));

        verify(clienteRepository, never()).saveAndFlush(any(Cliente.class));
    }

    @Test
    void crearRechazaCorreoDuplicado() {
        when(clienteRepository.existsByCurp("GALA900101MDFRNN09")).thenReturn(false);
        when(clienteRepository.existsByRfc("GALA900101ABC")).thenReturn(false);
        when(clienteRepository.existsByCorreoElectronicoIgnoreCase("ana@example.com")).thenReturn(true);

        assertThrows(CorreoDuplicadoException.class, () -> clienteService.crear(solicitudValida()));

        verify(clienteRepository, never()).saveAndFlush(any(Cliente.class));
    }

    @Test
    void buscarPorIdRetornaClienteCuandoExiste() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Ana");
        cliente.setApellidoPaterno("Lopez");
        cliente.setApellidoMaterno("Garcia");
        cliente.setCurp("GALA900101MDFRNN09");
        cliente.setRfc("GALA900101ABC");
        cliente.setCorreoElectronico("ana@example.com");
        cliente.setActivo(true);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        ClienteResponse response = clienteService.buscarPorId(1L);
        assertNotNull(response);
        assertEquals("Ana", response.getNombre());
    }

    @Test
    void buscarPorIdLanzaExcepcionCuandoNoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ClienteNoEncontradoException.class, () -> clienteService.buscarPorId(99L));
    }

    @Test
    void buscarPorCuentaRetornaTitular() {
        Cliente cliente = new Cliente();
        cliente.setId(5L);
        cliente.setNombre("Carlos");
        cliente.setApellidoPaterno("Perez");
        cliente.setApellidoMaterno("Diaz");
        cliente.setCurp("PEDC900101HDFRNN01");
        cliente.setRfc("PEDC900101XYZ");
        cliente.setCorreoElectronico("carlos@example.com");

        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta("4100000000000005");
        cuenta.setCliente(cliente);

        when(cuentaRepository.findByNumeroCuenta("4100000000000005")).thenReturn(Optional.of(cuenta));

        ClienteResponse response = clienteService.buscarPorCuenta("4100000000000005");
        assertNotNull(response);
        assertEquals("Carlos", response.getNombre());
    }

    @Test
    void desactivarRealizaBajaLogicaYDesactivaCuentasActivas() {
        Cliente cliente = new Cliente();
        cliente.setId(10L);
        cliente.setActivo(true);

        Cuenta cuenta1 = new Cuenta();
        cuenta1.setNumeroCuenta("4100000000000010");
        cuenta1.setEstatus("ACTIVA");

        Cuenta cuenta2 = new Cuenta();
        cuenta2.setNumeroCuenta("4100000000000011");
        cuenta2.setEstatus("INACTIVA");

        when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.findByClienteId(10L)).thenReturn(List.of(cuenta1, cuenta2));

        clienteService.desactivar(10L);

        assertFalse(cliente.isActivo());
        assertEquals("INACTIVA", cuenta1.getEstatus());
        assertEquals("INACTIVA", cuenta2.getEstatus());
        verify(clienteRepository).save(cliente);
    }

    @Test
    void actualizarModificaDatosPermitidos() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setCurp("GALA900101MDFRNN09");
        cliente.setRfc("GALA900101ABC");
        cliente.setCorreoElectronico("antiguo@example.com");
        cliente.setDomicilio(new Domicilio());

        ClienteActualizacionRequest actualizacion = new ClienteActualizacionRequest();
        actualizacion.setNombre("Ana Maria");
        actualizacion.setApellidoPaterno("Lopez");
        actualizacion.setApellidoMaterno("Garcia");
        actualizacion.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        actualizacion.setSexo("Masculino");
        actualizacion.setNacionalidad("Mexicana");
        actualizacion.setEstadoCivil("Casado");
        actualizacion.setCorreoElectronico("nuevo@example.com");
        actualizacion.setTelefonoMovil("5587654321");
        actualizacion.setOcupacion("Gerente");
        actualizacion.setEmpresa("Nueva Empresa");
        actualizacion.setIngresoMensual(new BigDecimal("35000.00"));

        DomicilioRequest dom = new DomicilioRequest();
        dom.setCalle("Insurgentes Sur");
        dom.setNumeroExterior("100");
        dom.setColonia("Del Valle");
        dom.setMunicipio("Benito Juarez");
        dom.setEstado("CDMX");
        dom.setCodigoPostal("03100");
        dom.setPais("Mexico");
        actualizacion.setDomicilio(dom);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.findByCorreoElectronicoIgnoreCase("nuevo@example.com")).thenReturn(Optional.empty());
        when(clienteRepository.saveAndFlush(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClienteResponse response = clienteService.actualizar(1L, actualizacion);

        assertEquals("Ana Maria", response.getNombre());
        assertEquals("nuevo@example.com", response.getCorreoElectronico());
        assertEquals("GALA900101MDFRNN09", response.getCurp());
        assertEquals("GALA900101ABC", response.getRfc());
    }

    @Test
    void registradosEntreRechazaRangoInvalido() {
        LocalDate desde = LocalDate.of(2026, 12, 31);
        LocalDate hasta = LocalDate.of(2026, 1, 1);

        assertThrows(ErrorValidacionException.class, () -> clienteService.registradosEntre(desde, hasta));
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

    @Test
    void patchActualizaSoloCamposProporcionados() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Original");
        cliente.setApellidoPaterno("Paterno");
        cliente.setApellidoMaterno("Materno");
        cliente.setTelefonoMovil("5511223344");
        cliente.setOcupacion("Analista");
        cliente.setActivo(true);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.saveAndFlush(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClientePatchRequest patchRequest = new ClientePatchRequest();
        patchRequest.setTelefonoMovil("5599887766");
        patchRequest.setOcupacion("Gerente");

        ClienteResponse response = clienteService.patch(1L, patchRequest);

        assertNotNull(response);
        assertEquals("Original", cliente.getNombre());
        assertEquals("5599887766", cliente.getTelefonoMovil());
        assertEquals("Gerente", cliente.getOcupacion());
    }
}