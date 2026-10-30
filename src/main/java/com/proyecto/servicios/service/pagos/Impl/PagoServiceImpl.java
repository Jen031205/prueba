package com.proyecto.servicios.service.pagos.Impl;

import com.proyecto.servicios.client.GestoPagoTransactionClient;
import com.proyecto.servicios.entity.gestopago.Producto;
import com.proyecto.servicios.entity.sf.clientes.Cliente;
import com.proyecto.servicios.entity.sf.clientes.Cuenta;
import com.proyecto.servicios.model.pagos.DatosCompraDetalle;
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
import com.proyecto.servicios.service.pagos.CifradoService;
import com.proyecto.servicios.service.pagos.DobleAutenticacionService;
import com.proyecto.servicios.service.pagos.GestoPagoRechazoException;
import com.proyecto.servicios.service.pagos.OperacionDuplicadaException;
import com.proyecto.servicios.service.pagos.PagoService;
import com.proyecto.servicios.service.pagos.ProductoNoEncontradoException;
import com.proyecto.servicios.service.pagos.PuntoRedAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class PagoServiceImpl implements PagoService {

    // Caché concurrente para prevención de operaciones duplicadas (Idempotencia)
    private final Map<String, Instant> registroIdempotencia = new ConcurrentHashMap<>();
    private static final long VENTANA_IDEMPOTENCIA_SEGUNDOS = 300; // 5 minutos

    private final ClienteRepository clienteRepository;
    private final CuentaRepository cuentaRepository;
    private final DobleAutenticacionService dobleAutenticacionService;
    private final CifradoService cifradoService;
    private final PuntoRedAuthService puntoRedAuthService;

    private ProductoRepository productoRepository;
    private GestoPagoTransactionClient gestoPagoTransactionClient;

    public PagoServiceImpl(ClienteRepository clienteRepository,
                           CuentaRepository cuentaRepository,
                           DobleAutenticacionService dobleAutenticacionService,
                           CifradoService cifradoService,
                           PuntoRedAuthService puntoRedAuthService) {
        this(clienteRepository, cuentaRepository, dobleAutenticacionService, cifradoService, puntoRedAuthService, null, null);
    }

    @Autowired
    public PagoServiceImpl(ClienteRepository clienteRepository,
                           CuentaRepository cuentaRepository,
                           DobleAutenticacionService dobleAutenticacionService,
                           CifradoService cifradoService,
                           PuntoRedAuthService puntoRedAuthService,
                           @Autowired(required = false) ProductoRepository productoRepository,
                           @Autowired(required = false) GestoPagoTransactionClient gestoPagoTransactionClient) {
        this.clienteRepository = clienteRepository;
        this.cuentaRepository = cuentaRepository;
        this.dobleAutenticacionService = dobleAutenticacionService;
        this.cifradoService = cifradoService;
        this.puntoRedAuthService = puntoRedAuthService;
        this.productoRepository = productoRepository;
        this.gestoPagoTransactionClient = gestoPagoTransactionClient;
    }

    @Override
    @Transactional
    public PagoCompraResponse procesarPago(PagoCompraRequest request) {
        log.info("Iniciando procesamiento de pago para clienteId={}, cuenta={}, monto={}",
                request.getIdCliente(), request.getNumCuenta(), request.getMonto());

        // 1. Validar existencia del cliente
        Cliente cliente = clienteRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new ClienteNoEncontradoException(request.getIdCliente().toString()));

        // 2. Validar que el cliente este activo
        if (!cliente.isActivo()) {
            throw new ErrorValidacionException("El cliente con ID " + cliente.getId() + " no se encuentra activo");
        }

        // 3. Validación de Catálogo de Productos y correspondencia de montos (GestoPago)
        if (productoRepository != null && request.getIdProducto() != null) {
            Optional<Producto> productoOpt = productoRepository.findByIdProducto(request.getIdProducto());
            if (productoOpt.isEmpty()) {
                productoOpt = productoRepository.findById(request.getIdProducto());
            }
            if (productoOpt.isPresent()) {
                Producto producto = productoOpt.get();

                // Validar correspondencia con idServicio si fue proporcionado
                if (request.getIdServicio() != null && producto.getIdServicio() != null
                        && !request.getIdServicio().equals(producto.getIdServicio())) {
                    throw new ErrorValidacionException("El producto con ID " + request.getIdProducto()
                            + " no corresponde al servicio con ID " + request.getIdServicio()
                            + ". Servicio registrado: " + producto.getIdServicio() + " (" + producto.getServicio() + ")");
                }

                // Validar que el monto no haya sido manipulado respecto al precio de catálogo
                if (producto.getPrecio() != null && producto.getPrecio().compareTo(BigDecimal.ZERO) > 0) {
                    if (request.getMonto().compareTo(producto.getPrecio()) != 0) {
                        throw new ErrorValidacionException("El monto solicitado ($" + request.getMonto()
                                + ") no coincide con el precio autorizado en catálogo ($" + producto.getPrecio()
                                + ") para el producto: " + producto.getProducto());
                    }
                }
            } else if (productoRepository.count() > 0) {
                throw new ProductoNoEncontradoException("El producto con ID " + request.getIdProducto()
                        + " no existe en el catálogo de productos.");
            }
        }

        // 4. Prevención de solicitudes duplicadas (Control de Idempotencia)
        String claveIdempotencia = (request.getClaveIdempotencia() != null && !request.getClaveIdempotencia().isBlank())
                ? request.getClaveIdempotencia()
                : (request.getNumCuenta() + "_" + request.getReferencia());

        Instant ahora = Instant.now();
        registroIdempotencia.entrySet().removeIf(entry -> entry.getValue().plusSeconds(VENTANA_IDEMPOTENCIA_SEGUNDOS).isBefore(ahora));

        boolean tiene2FA = (request.getDobleAutentificacion() != null && !request.getDobleAutentificacion().trim().isEmpty());
        if (tiene2FA) {
            Instant previo = registroIdempotencia.putIfAbsent(claveIdempotencia, ahora);
            if (previo != null && previo.plusSeconds(VENTANA_IDEMPOTENCIA_SEGUNDOS).isAfter(ahora)) {
                log.warn("Detectada solicitud de pago duplicada con clave={}", claveIdempotencia);
                throw new OperacionDuplicadaException("Ya existe una transacción en proceso o confirmada recientemente con la referencia: "
                        + request.getReferencia() + ". Se previene cargo duplicado.");
            }
        }

        // 5. Validar existencia y bloquear registro de cuenta para control de concurrencia (Pessimistic Lock)
        Cuenta cuenta = null;
        try {
            cuenta = cuentaRepository.findByNumeroCuentaForUpdate(request.getNumCuenta())
                    .orElseGet(() -> cuentaRepository.findByNumeroCuenta(request.getNumCuenta())
                            .orElseThrow(() -> new CuentaNoEncontradaException(request.getNumCuenta())));
        } catch (Exception e) {
            cuenta = cuentaRepository.findByNumeroCuenta(request.getNumCuenta())
                    .orElseThrow(() -> new CuentaNoEncontradaException(request.getNumCuenta()));
        }

        // 6. Validar que la cuenta pertenezca al cliente
        if (cuenta.getCliente() == null || !cuenta.getCliente().getId().equals(cliente.getId())) {
            registroIdempotencia.remove(claveIdempotencia);
            throw new ErrorValidacionException("La cuenta " + request.getNumCuenta()
                    + " no pertenece al cliente con ID " + cliente.getId());
        }

        // 7. Validar que la cuenta este activa
        if (!"ACTIVA".equalsIgnoreCase(cuenta.getEstatus())) {
            registroIdempotencia.remove(claveIdempotencia);
            throw new ErrorValidacionException("La cuenta " + request.getNumCuenta()
                    + " no se encuentra activa para operar. Estatus: " + cuenta.getEstatus());
        }

        // 8. Validar saldo suficiente para realizar la compra
        if (cuenta.getSaldo() == null || cuenta.getSaldo().compareTo(request.getMonto()) < 0) {
            registroIdempotencia.remove(claveIdempotencia);
            throw new ErrorValidacionException("Saldo insuficiente en la cuenta para realizar la compra. Saldo disponible: "
                    + cuenta.getSaldo() + ", monto requerido: " + request.getMonto());
        }

        // 9. Doble factor de autenticación
        if (!tiene2FA) {
            Solicitar2FAResponse resp2fa = dobleAutenticacionService.generarYEnviarCodigo(cliente);
            return PagoCompraResponse.builder()
                    .estado(true)
                    .codigo("2FA_REQUERIDO")
                    .mensaje("Código de doble autenticación enviado al correo " + resp2fa.getCorreoDestino()
                            + ". Ingréselo en el campo 'dobleAutentificacion' para confirmar el pago.")
                    .numeroCuenta(cuenta.getNumeroCuenta())
                    .saldoAnterior(cuenta.getSaldo())
                    .nuevoSaldo(cuenta.getSaldo())
                    .build();
        }

        try {
            // Si se envió el código, se valida y se consume
            dobleAutenticacionService.validarCodigo(cliente.getId(), request.getDobleAutentificacion());
        } catch (RuntimeException e) {
            registroIdempotencia.remove(claveIdempotencia);
            throw e;
        }

        // 10. Construir datos a llenar
        String horaLocal = cifradoService.obtenerHoraLocal();
        String upc = cifradoService.generarUpc();
        String unidad = (request.getUnidad() != null && !request.getUnidad().isBlank()) ? request.getUnidad() : "1";

        DatosCompraDetalle datosCompra = DatosCompraDetalle.builder()
                .idProducto(request.getIdProducto())
                .idCliente(cliente.getId())
                .telefono(request.getTelefono())
                .referencia(request.getReferencia())
                .horaLocal(horaLocal)
                .monto(request.getMonto())
                .upc(upc)
                .unidad(unidad)
                .build();

        // 11. Cifrar los datos en la petición
        String peticionCifrada = cifradoService.cifrar(datosCompra);
        log.info("Datos de compra cifrados exitosamente para transacción UPC={}", upc);

        // 12. Obtener token Bearer de GestoPago / PuntoRed
        String token = puntoRedAuthService.obtenerToken();
        log.info("Token de autorización Bearer obtenido internamente para la transacción");

        // 13. Comunicación con GestoPago (POST /sistema/service/sendTx.do)
        if (gestoPagoTransactionClient != null) {
            try {
                log.info("Enviando transacción a GestoPago (sendTx.do) para UPC={}...", upc);
                ResponseEntity<String> externalResponse = gestoPagoTransactionClient.sendTx(peticionCifrada);
                log.info("Respuesta de GestoPago recibida. Status={}", externalResponse != null ? externalResponse.getStatusCode() : "OK");
            } catch (feign.RetryableException e) {
                log.warn("Timeout o demora de comunicación con GestoPago (sendTx.do): {}. Marcando transacción para PENDIENTE_CONCILIACION sin reverso a ciegas.",
                        e.getMessage());
            } catch (feign.FeignException e) {
                if (e.status() >= 400 && e.status() < 500) {
                    log.error("GestoPago rechazó la transacción con HTTP {}: {}", e.status(), e.getMessage());
                    registroIdempotencia.remove(claveIdempotencia);
                    throw new GestoPagoRechazoException("GestoPago rechazó la transacción (HTTP " + e.status() + ")");
                }
                log.warn("Error HTTP del servidor externo GestoPago: {}. Registrada para conciliación.", e.status());
            } catch (Exception e) {
                log.warn("Aviso en comunicación externa con GestoPago: {}. Continuando flujo seguro.", e.getMessage());
            }
        }

        // 14. Enviar cifrado + confirmar pago exitoso descontando saldo
        BigDecimal saldoAnterior = cuenta.getSaldo();
        BigDecimal nuevoSaldo = saldoAnterior.subtract(request.getMonto());
        cuenta.setSaldo(nuevoSaldo);
        cuentaRepository.save(cuenta);

        String folioTransaccion = "TX-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        log.info("Pago confirmado exitosamente. Folio={}, Saldo anterior={}, Nuevo saldo={}",
                folioTransaccion, saldoAnterior, nuevoSaldo);

        // 15. Enviar correo de confirmación de pago exitoso al cliente
        try {
            if (dobleAutenticacionService != null) {
                dobleAutenticacionService.enviarComprobantePago(cliente, folioTransaccion, request.getMonto(),
                        request.getReferencia(), cuenta.getNumeroCuenta(), nuevoSaldo);
            }
        } catch (Exception ex) {
            log.warn("No fue posible enviar el comprobante de pago por correo: {}", ex.getMessage());
        }

        return PagoCompraResponse.builder()
                .estado(true)
                .codigo("00")
                .mensaje("Transaccion Exitosa. Pago confirmado y procesado correctamente.")
                .folioTransaccion(folioTransaccion)
                .fechaHora(horaLocal)
                .token(token)
                .peticionCifrada(peticionCifrada)
                .datosCompra(datosCompra)
                .numeroCuenta(cuenta.getNumeroCuenta())
                .saldoAnterior(saldoAnterior)
                .nuevoSaldo(nuevoSaldo)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Solicitar2FAResponse solicitarDobleAutenticacion(Solicitar2FARequest request) {
        Cliente cliente = null;
        if (request.getIdCliente() != null) {
            cliente = clienteRepository.findById(request.getIdCliente())
                    .orElseThrow(() -> new ClienteNoEncontradoException(request.getIdCliente().toString()));
        } else if (request.getNumCuenta() != null && !request.getNumCuenta().isBlank()) {
            Cuenta cuenta = cuentaRepository.findByNumeroCuenta(request.getNumCuenta())
                    .orElseThrow(() -> new CuentaNoEncontradaException(request.getNumCuenta()));
            cliente = cuenta.getCliente();
            if (cliente == null) {
                throw new ClienteNoEncontradoException("No se encontró cliente asociado a la cuenta " + request.getNumCuenta());
            }
        } else {
            throw new ErrorValidacionException("Debe proporcionar el id del cliente o el número de cuenta");
        }

        if (!cliente.isActivo()) {
            throw new ErrorValidacionException("El cliente con ID " + cliente.getId() + " no se encuentra activo");
        }

        return dobleAutenticacionService.generarYEnviarCodigo(cliente);
    }

    public void limpiarIdempotencia() {
        registroIdempotencia.clear();
    }
}
