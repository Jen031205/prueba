package com.proyecto.servicios.service.pagos;

import com.proyecto.servicios.client.GestoPagoTransactionClient;
import com.proyecto.servicios.entity.gestopago.Producto;
import com.proyecto.servicios.entity.sf.clientes.Cliente;
import com.proyecto.servicios.entity.sf.clientes.Cuenta;
import com.proyecto.servicios.model.pagos.PagoCompraRequest;
import com.proyecto.servicios.model.pagos.PagoCompraResponse;
import com.proyecto.servicios.model.pagos.Solicitar2FARequest;
import com.proyecto.servicios.model.pagos.Solicitar2FAResponse;
import com.proyecto.servicios.repositorys.gestopago.ProductoRepository;
import com.proyecto.servicios.repositorys.sf.clientes.ClienteRepository;
import com.proyecto.servicios.repositorys.sf.clientes.CuentaRepository;
import com.proyecto.servicios.service.clientes.ClienteNoEncontradoException;
import com.proyecto.servicios.service.clientes.CuentaNoEncontradaException;
import com.proyecto.servicios.service.clientes.ErrorValidacionException;
import com.proyecto.servicios.service.pagos.Impl.PagoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private DobleAutenticacionService dobleAutenticacionService;

    @Mock
    private CifradoService cifradoService;

    @Mock
    private PuntoRedAuthService puntoRedAuthService;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private GestoPagoTransactionClient gestoPagoTransactionClient;

    @InjectMocks
    private PagoServiceImpl pagoService;

    private Cliente cliente;
    private Cuenta cuenta;
    private PagoCompraRequest request;

    @BeforeEach
    void setUp() {
        pagoService.limpiarIdempotencia();
        cliente = new Cliente();
        cliente.setId(10L);
        cliente.setNombre("Carlos");
        cliente.setApellidoPaterno("Sanchez");
        cliente.setCorreoElectronico("carlos@empresa.com");
        cliente.setTelefonoMovil("5512345678");
        cliente.setActivo(true);

        cuenta = new Cuenta();
        cuenta.setId(20L);
        cuenta.setNumeroCuenta("4000123456789012");
        cuenta.setSaldo(new BigDecimal("1500.00"));
        cuenta.setEstatus("ACTIVA");
        cuenta.setCliente(cliente);

        request = new PagoCompraRequest();
        request.setIdCliente(10L);
        request.setNumCuenta("4000123456789012");
        request.setIdProducto(823);
        request.setMonto(new BigDecimal("250.00"));
        request.setTelefono("5512345678");
        request.setReferencia("FAC-998811");
        request.setDobleAutentificacion("123456");
        request.setUnidad("1");
    }

    @Test
    void procesarPagoExitoso() {
        when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.findByNumeroCuenta("4000123456789012")).thenReturn(Optional.of(cuenta));
        when(dobleAutenticacionService.validarCodigo(10L, "123456")).thenReturn(true);
        when(cifradoService.obtenerHoraLocal()).thenReturn("2026-10-23 18:00:00");
        when(cifradoService.generarUpc()).thenReturn("1234567890");
        when(cifradoService.cifrar(any())).thenReturn("cifradoBase64Fake");
        when(puntoRedAuthService.obtenerToken()).thenReturn("jwt-token-fake");

        PagoCompraResponse response = pagoService.procesarPago(request);

        assertNotNull(response);
        assertTrue(response.getEstado());
        assertEquals("00", response.getCodigo());
        assertEquals(new BigDecimal("1500.00"), response.getSaldoAnterior());
        assertEquals(new BigDecimal("1250.00"), response.getNuevoSaldo());
        assertEquals("jwt-token-fake", response.getToken());
        assertEquals("cifradoBase64Fake", response.getPeticionCifrada());
        assertNotNull(response.getDatosCompra());
        assertEquals("1234567890", response.getDatosCompra().getUpc());
        assertEquals("10", response.getDatosCompra().getIdCliente().toString());

        verify(cuentaRepository).save(cuenta);
        assertEquals(new BigDecimal("1250.00"), cuenta.getSaldo());
    }

    @Test
    void procesarPagoSin2FAEnviaCodigoAlCorreo() {
        request.setDobleAutentificacion(null);

        when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.findByNumeroCuenta("4000123456789012")).thenReturn(Optional.of(cuenta));
        when(dobleAutenticacionService.generarYEnviarCodigo(cliente)).thenReturn(
                Solicitar2FAResponse.builder()
                        .estado(true)
                        .mensaje("Código generado")
                        .correoDestino("ca***@empresa.com")
                        .expiraEnMinutos(10)
                        .build()
        );

        PagoCompraResponse response = pagoService.procesarPago(request);

        assertNotNull(response);
        assertTrue(response.getEstado());
        assertEquals("2FA_REQUERIDO", response.getCodigo());
        assertTrue(response.getMensaje().contains("ca***@empresa.com"));
    }

    @Test
    void procesarPagoFallaClienteNoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());
        request.setIdCliente(99L);

        assertThrows(ClienteNoEncontradoException.class, () -> pagoService.procesarPago(request));
    }

    @Test
    void procesarPagoFallaClienteInactivo() {
        cliente.setActivo(false);
        when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente));

        ErrorValidacionException ex = assertThrows(ErrorValidacionException.class,
                () -> pagoService.procesarPago(request));
        assertTrue(ex.getMessage().contains("no se encuentra activo"));
    }

    @Test
    void procesarPagoFallaCuentaNoExiste() {
        when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.findByNumeroCuenta("4000123456789012")).thenReturn(Optional.empty());

        assertThrows(CuentaNoEncontradaException.class, () -> pagoService.procesarPago(request));
    }

    @Test
    void procesarPagoFallaCuentaNoPerteneceAlCliente() {
        Cliente otroCliente = new Cliente();
        otroCliente.setId(99L);
        cuenta.setCliente(otroCliente);

        when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.findByNumeroCuenta("4000123456789012")).thenReturn(Optional.of(cuenta));

        ErrorValidacionException ex = assertThrows(ErrorValidacionException.class,
                () -> pagoService.procesarPago(request));
        assertTrue(ex.getMessage().contains("no pertenece al cliente"));
    }

    @Test
    void procesarPagoFallaCuentaInactiva() {
        cuenta.setEstatus("BLOQUEADA");
        when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.findByNumeroCuenta("4000123456789012")).thenReturn(Optional.of(cuenta));

        ErrorValidacionException ex = assertThrows(ErrorValidacionException.class,
                () -> pagoService.procesarPago(request));
        assertTrue(ex.getMessage().contains("no se encuentra activa"));
    }

    @Test
    void procesarPagoFallaSaldoInsuficiente() {
        cuenta.setSaldo(new BigDecimal("100.00"));
        when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.findByNumeroCuenta("4000123456789012")).thenReturn(Optional.of(cuenta));

        ErrorValidacionException ex = assertThrows(ErrorValidacionException.class,
                () -> pagoService.procesarPago(request));
        assertTrue(ex.getMessage().contains("Saldo insuficiente"));
    }

    @Test
    void procesarPagoFalla2FAInvalido() {
        when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.findByNumeroCuenta("4000123456789012")).thenReturn(Optional.of(cuenta));
        when(dobleAutenticacionService.validarCodigo(10L, "123456"))
                .thenThrow(new ErrorValidacionException("El código de doble autenticación ingresado es incorrecto"));

        ErrorValidacionException ex = assertThrows(ErrorValidacionException.class,
                () -> pagoService.procesarPago(request));
        assertTrue(ex.getMessage().contains("doble autenticación"));
    }

    @Test
    void solicitar2FAExitoso() {
        when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente));
        when(dobleAutenticacionService.generarYEnviarCodigo(cliente)).thenReturn(
                Solicitar2FAResponse.builder()
                        .estado(true)
                        .mensaje("Código enviado")
                        .correoDestino("ca***@empresa.com")
                        .expiraEnMinutos(10)
                        .build()
        );

        Solicitar2FARequest req2fa = new Solicitar2FARequest(10L, null);
        Solicitar2FAResponse resp = pagoService.solicitarDobleAutenticacion(req2fa);

        assertNotNull(resp);
        assertTrue(resp.getEstado());
        assertEquals("ca***@empresa.com", resp.getCorreoDestino());
    }

    @Test
    void procesarPagoFallaMontoNoCoincideConPrecioEnCatalogo() {
        Producto producto = new Producto();
        producto.setIdProducto(823);
        producto.setIdServicio(50);
        producto.setProducto("Paquete Datos 100");
        producto.setPrecio(new BigDecimal("100.00"));

        when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente));
        when(productoRepository.findByIdProducto(823)).thenReturn(Optional.of(producto));

        request.setMonto(new BigDecimal("99.00"));
        ErrorValidacionException ex = assertThrows(ErrorValidacionException.class,
                () -> pagoService.procesarPago(request));
        assertTrue(ex.getMessage().contains("no coincide con el precio autorizado"));
    }

    @Test
    void procesarPagoFallaServicioNoCoincide() {
        Producto producto = new Producto();
        producto.setIdProducto(823);
        producto.setIdServicio(50);
        producto.setProducto("Paquete Datos 100");
        producto.setPrecio(new BigDecimal("250.00"));

        when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente));
        when(productoRepository.findByIdProducto(823)).thenReturn(Optional.of(producto));

        request.setIdServicio(99);
        ErrorValidacionException ex = assertThrows(ErrorValidacionException.class,
                () -> pagoService.procesarPago(request));
        assertTrue(ex.getMessage().contains("no corresponde al servicio con ID 99"));
    }

    @Test
    void procesarPagoFallaOperacionDuplicada() {
        when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.findByNumeroCuenta("4000123456789012")).thenReturn(Optional.of(cuenta));
        when(dobleAutenticacionService.validarCodigo(10L, "123456")).thenReturn(true);
        when(cifradoService.obtenerHoraLocal()).thenReturn("2026-10-23 18:00:00");
        when(cifradoService.generarUpc()).thenReturn("1234567890");
        when(cifradoService.cifrar(any())).thenReturn("cifradoBase64Fake");
        when(puntoRedAuthService.obtenerToken()).thenReturn("jwt-token-fake");

        // Primera ejecución exitosa
        pagoService.procesarPago(request);

        // Segunda ejecución inmediata con la misma referencia debe fallar por duplicada
        OperacionDuplicadaException ex = assertThrows(OperacionDuplicadaException.class,
                () -> pagoService.procesarPago(request));
        assertTrue(ex.getMessage().contains("Se previene cargo duplicado"));
    }
}
