package com.proyecto.servicios.service.clientes;

import com.proyecto.servicios.entity.sf.clientes.Cliente;
import com.proyecto.servicios.entity.sf.clientes.Cuenta;
import com.proyecto.servicios.entity.sf.clientes.Domicilio;
import com.proyecto.servicios.model.clientes.ClienteActualizacionRequest;
import com.proyecto.servicios.model.clientes.ClienteDatosRequest;
import com.proyecto.servicios.model.clientes.ClientePatchRequest;
import com.proyecto.servicios.model.clientes.ClienteRequest;
import com.proyecto.servicios.model.clientes.ClienteResponse;
import com.proyecto.servicios.model.clientes.CuentaResponse;
import com.proyecto.servicios.model.clientes.SaldoResponse;
import com.proyecto.servicios.repositorys.sf.clientes.ClienteRepository;
import com.proyecto.servicios.repositorys.sf.clientes.CuentaRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

@Service
public class ClienteService {
    private static final String CUENTA_ACTIVA = "ACTIVA";
    private static final String CUENTA_INACTIVA = "INACTIVA";

    private final ClienteRepository clienteRepository;
    private final CuentaRepository cuentaRepository;
    private final EntityManager entityManager;

    @Value("${banco.cuenta.saldo-inicial:0.00}")
    private BigDecimal saldoInicialConfigurado = BigDecimal.ZERO;

    public ClienteService(ClienteRepository clienteRepository,
                          CuentaRepository cuentaRepository,
                          EntityManager entityManager) {
        this.clienteRepository = clienteRepository;
        this.cuentaRepository = cuentaRepository;
        this.entityManager = entityManager;
    }

    public void setSaldoInicialConfigurado(BigDecimal saldoInicialConfigurado) {
        this.saldoInicialConfigurado = saldoInicialConfigurado;
    }

    public BigDecimal getSaldoInicial() {
        BigDecimal saldo = saldoInicialConfigurado != null ? saldoInicialConfigurado : BigDecimal.ZERO;
        if (saldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new ErrorValidacionException("El saldo inicial del sistema no puede ser negativo");
        }
        return saldo;
    }

    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        String curp = request.getCurp().trim().toUpperCase(Locale.ROOT);
        String rfc = request.getRfc().trim().toUpperCase(Locale.ROOT);
        String correo = request.getCorreoElectronico().trim().toLowerCase(Locale.ROOT);

        if (clienteRepository.existsByCurp(curp)) {
            throw new CurpDuplicadaException(curp);
        }
        if (clienteRepository.existsByRfc(rfc)) {
            throw new RfcDuplicadoException(rfc);
        }
        if (clienteRepository.existsByCorreoElectronicoIgnoreCase(correo)) {
            throw new CorreoDuplicadoException(correo);
        }

        Cliente cliente = new Cliente();
        copiarDatos(request, cliente);
        cliente.setCurp(curp);
        cliente.setRfc(rfc);
        cliente.setCorreoElectronico(correo);
        cliente.setActivo(true);
        cliente.setFechaRegistro(LocalDateTime.now());
        cliente.setDomicilio(crearDomicilio(request));
        clienteRepository.saveAndFlush(cliente);

        Cuenta cuenta = new Cuenta();
        cuenta.setCliente(cliente);
        cuenta.setSaldo(getSaldoInicial());
        cuenta.setEstatus(CUENTA_ACTIVA);
        cuenta.setFechaCreacion(LocalDateTime.now());
        cuentaRepository.saveAndFlush(cuenta);
        entityManager.refresh(cuenta);

        cliente.getCuentas().add(cuenta);
        return toResponse(cliente);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listarActivos() {
        return clienteRepository.findByActivoTrue().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> registradosEntre(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || desde.isAfter(hasta)) {
            throw new ErrorValidacionException("El rango de fechas debe ser válido y completo (desde <= hasta)");
        }
        return clienteRepository.findByFechaRegistroBetween(desde.atStartOfDay(), hasta.atTime(LocalTime.MAX))
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        return clienteRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ClienteNoEncontradoException("id " + id));
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorCurp(String curp) {
        return clienteRepository.findByCurp(curp.trim().toUpperCase(Locale.ROOT)).map(this::toResponse)
                .orElseThrow(() -> new ClienteNoEncontradoException("CURP " + curp));
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorRfc(String rfc) {
        return clienteRepository.findByRfc(rfc.trim().toUpperCase(Locale.ROOT)).map(this::toResponse)
                .orElseThrow(() -> new ClienteNoEncontradoException("RFC " + rfc));
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorCorreo(String correo) {
        return clienteRepository.findByCorreoElectronicoIgnoreCase(correo.trim()).map(this::toResponse)
                .orElseThrow(() -> new ClienteNoEncontradoException("correo " + correo));
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorCuenta(String numeroCuenta) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaException(numeroCuenta));
        return toResponse(cuenta.getCliente());
    }

    @Transactional
    public ClienteResponse actualizar(Long id, ClienteActualizacionRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException("id " + id));

        String correo = request.getCorreoElectronico().trim().toLowerCase(Locale.ROOT);
        clienteRepository.findByCorreoElectronicoIgnoreCase(correo).ifPresent(otro -> {
            if (!otro.getId().equals(id)) {
                throw new CorreoDuplicadoException(correo);
            }
        });

        copiarDatos(request, cliente);
        cliente.setCorreoElectronico(correo);

        Domicilio domicilio = cliente.getDomicilio();
        if (domicilio == null) {
            domicilio = new Domicilio();
            cliente.setDomicilio(domicilio);
        }
        copiarDomicilio(request, domicilio);

        return toResponse(clienteRepository.saveAndFlush(cliente));
    }

    @Transactional
    public ClienteResponse patch(Long id, ClientePatchRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException("id " + id));

        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            cliente.setNombre(request.getNombre().trim());
        }
        if (request.getSegundoNombre() != null) {
            cliente.setSegundoNombre(request.getSegundoNombre().trim().isEmpty() ? null : request.getSegundoNombre().trim());
        }
        if (request.getApellidoPaterno() != null && !request.getApellidoPaterno().isBlank()) {
            cliente.setApellidoPaterno(request.getApellidoPaterno().trim());
        }
        if (request.getApellidoMaterno() != null && !request.getApellidoMaterno().isBlank()) {
            cliente.setApellidoMaterno(request.getApellidoMaterno().trim());
        }
        if (request.getFechaNacimiento() != null) {
            if (request.getFechaNacimiento().isAfter(LocalDate.now().minusYears(18))) {
                throw new ErrorValidacionException("El cliente debe tener al menos 18 años");
            }
            cliente.setFechaNacimiento(request.getFechaNacimiento());
        }
        if (request.getSexo() != null && !request.getSexo().isBlank()) {
            cliente.setSexo(request.getSexo());
        }
        if (request.getNacionalidad() != null && !request.getNacionalidad().isBlank()) {
            cliente.setNacionalidad(request.getNacionalidad().trim());
        }
        if (request.getEstadoCivil() != null && !request.getEstadoCivil().isBlank()) {
            cliente.setEstadoCivil(request.getEstadoCivil());
        }
        if (request.getCorreoElectronico() != null && !request.getCorreoElectronico().isBlank()) {
            String correo = request.getCorreoElectronico().trim().toLowerCase(Locale.ROOT);
            clienteRepository.findByCorreoElectronicoIgnoreCase(correo).ifPresent(otro -> {
                if (!otro.getId().equals(id)) {
                    throw new CorreoDuplicadoException(correo);
                }
            });
            cliente.setCorreoElectronico(correo);
        }
        if (request.getTelefonoMovil() != null && !request.getTelefonoMovil().isBlank()) {
            cliente.setTelefonoMovil(request.getTelefonoMovil().trim());
        }
        if (request.getTelefonoAlternativo() != null) {
            cliente.setTelefonoAlternativo(request.getTelefonoAlternativo().trim().isEmpty() ? null : request.getTelefonoAlternativo().trim());
        }
        if (request.getOcupacion() != null && !request.getOcupacion().isBlank()) {
            cliente.setOcupacion(request.getOcupacion().trim());
        }
        if (request.getEmpresa() != null && !request.getEmpresa().isBlank()) {
            cliente.setEmpresa(request.getEmpresa().trim());
        }
        if (request.getIngresoMensual() != null) {
            cliente.setIngresoMensual(request.getIngresoMensual());
        }
        if (request.getActivo() != null) {
            cliente.setActivo(request.getActivo());
            if (!request.getActivo()) {
                cuentaRepository.findByClienteId(id).stream()
                        .filter(cuenta -> CUENTA_ACTIVA.equals(cuenta.getEstatus()))
                        .forEach(cuenta -> cuenta.setEstatus(CUENTA_INACTIVA));
            }
        }

        if (request.getDomicilio() != null) {
            Domicilio domicilio = cliente.getDomicilio();
            if (domicilio == null) {
                domicilio = new Domicilio();
                cliente.setDomicilio(domicilio);
            }
            var dReq = request.getDomicilio();
            if (dReq.getCalle() != null && !dReq.getCalle().isBlank()) {
                domicilio.setCalle(dReq.getCalle().trim());
            }
            if (dReq.getNumeroExterior() != null && !dReq.getNumeroExterior().isBlank()) {
                domicilio.setNumeroExterior(dReq.getNumeroExterior().trim());
            }
            if (dReq.getNumeroInterior() != null) {
                domicilio.setNumeroInterior(dReq.getNumeroInterior().trim().isEmpty() ? null : dReq.getNumeroInterior().trim());
            }
            if (dReq.getColonia() != null && !dReq.getColonia().isBlank()) {
                domicilio.setColonia(dReq.getColonia().trim());
            }
            if (dReq.getMunicipio() != null && !dReq.getMunicipio().isBlank()) {
                domicilio.setMunicipio(dReq.getMunicipio().trim());
            }
            if (dReq.getEstado() != null && !dReq.getEstado().isBlank()) {
                domicilio.setEstado(dReq.getEstado().trim());
            }
            if (dReq.getCodigoPostal() != null && !dReq.getCodigoPostal().isBlank()) {
                domicilio.setCodigoPostal(dReq.getCodigoPostal().trim());
            }
            if (dReq.getPais() != null && !dReq.getPais().isBlank()) {
                domicilio.setPais(dReq.getPais().trim());
            }
        }

        return toResponse(clienteRepository.saveAndFlush(cliente));
    }

    @Transactional
    public void desactivar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException("id " + id));
        cliente.setActivo(false);
        clienteRepository.save(cliente);

        cuentaRepository.findByClienteId(id).stream()
                .filter(cuenta -> CUENTA_ACTIVA.equals(cuenta.getEstatus()))
                .forEach(cuenta -> cuenta.setEstatus(CUENTA_INACTIVA));
    }

    @Transactional(readOnly = true)
    public CuentaResponse buscarCuenta(String numeroCuenta) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaException(numeroCuenta));
        return toCuentaResponse(cuenta);
    }

    @Transactional(readOnly = true)
    public SaldoResponse consultarSaldo(String numeroCuenta) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaException(numeroCuenta));
        return new SaldoResponse(cuenta.getNumeroCuenta(), cuenta.getSaldo());
    }

    @Transactional(readOnly = true)
    public List<CuentaResponse> listarCuentasActivas() {
        return cuentaRepository.findByEstatus(CUENTA_ACTIVA).stream().map(this::toCuentaResponse).toList();
    }

    private Domicilio crearDomicilio(ClienteDatosRequest request) {
        Domicilio domicilio = new Domicilio();
        copiarDomicilio(request, domicilio);
        return domicilio;
    }

    private void copiarDomicilio(ClienteDatosRequest request, Domicilio domicilio) {
        var origen = request.getDomicilio();
        domicilio.setCalle(origen.getCalle().trim());
        domicilio.setNumeroExterior(origen.getNumeroExterior().trim());
        domicilio.setNumeroInterior(origen.getNumeroInterior() == null ? null : origen.getNumeroInterior().trim());
        domicilio.setColonia(origen.getColonia().trim());
        domicilio.setMunicipio(origen.getMunicipio().trim());
        domicilio.setEstado(origen.getEstado().trim());
        domicilio.setCodigoPostal(origen.getCodigoPostal().trim());
        domicilio.setPais(origen.getPais().trim());
    }

    private void copiarDatos(ClienteDatosRequest origen, Cliente destino) {
        destino.setNombre(origen.getNombre().trim());
        destino.setSegundoNombre(origen.getSegundoNombre() == null || origen.getSegundoNombre().trim().isEmpty()
                ? null : origen.getSegundoNombre().trim());
                
        destino.setApellidoPaterno(origen.getApellidoPaterno().trim());
        destino.setApellidoMaterno(origen.getApellidoMaterno().trim());
        destino.setFechaNacimiento(origen.getFechaNacimiento());
        destino.setSexo(origen.getSexo());
        destino.setNacionalidad(origen.getNacionalidad().trim());
        destino.setEstadoCivil(origen.getEstadoCivil());
        destino.setTelefonoMovil(origen.getTelefonoMovil().trim());
        destino.setTelefonoAlternativo(origen.getTelefonoAlternativo() == null || origen.getTelefonoAlternativo().trim().isEmpty()
                ? null : origen.getTelefonoAlternativo().trim());
        destino.setOcupacion(origen.getOcupacion().trim());
        destino.setEmpresa(origen.getEmpresa().trim());
        destino.setIngresoMensual(origen.getIngresoMensual());
    }

    private ClienteResponse toResponse(Cliente cliente) {
        ClienteResponse respuesta = new ClienteResponse();
        respuesta.setId(cliente.getId());
        respuesta.setNombre(cliente.getNombre());
        respuesta.setSegundoNombre(cliente.getSegundoNombre());
        respuesta.setApellidoPaterno(cliente.getApellidoPaterno());
        respuesta.setApellidoMaterno(cliente.getApellidoMaterno());
        respuesta.setFechaNacimiento(cliente.getFechaNacimiento());
        respuesta.setCurp(cliente.getCurp());
        respuesta.setRfc(cliente.getRfc());
        respuesta.setSexo(cliente.getSexo());
        respuesta.setNacionalidad(cliente.getNacionalidad());
        respuesta.setEstadoCivil(cliente.getEstadoCivil());
        respuesta.setCorreoElectronico(cliente.getCorreoElectronico());
        respuesta.setTelefonoMovil(cliente.getTelefonoMovil());
        respuesta.setTelefonoAlternativo(cliente.getTelefonoAlternativo());
        respuesta.setOcupacion(cliente.getOcupacion());
        respuesta.setEmpresa(cliente.getEmpresa());
        respuesta.setIngresoMensual(cliente.getIngresoMensual());
        respuesta.setActivo(cliente.isActivo());
        respuesta.setFechaRegistro(cliente.getFechaRegistro());

        if (cliente.getDomicilio() != null) {
            ClienteResponse.DomicilioResponse domicilio = new ClienteResponse.DomicilioResponse();
            domicilio.setCalle(cliente.getDomicilio().getCalle());
            domicilio.setNumeroExterior(cliente.getDomicilio().getNumeroExterior());
            domicilio.setNumeroInterior(cliente.getDomicilio().getNumeroInterior());
            domicilio.setColonia(cliente.getDomicilio().getColonia());
            domicilio.setMunicipio(cliente.getDomicilio().getMunicipio());
            domicilio.setEstado(cliente.getDomicilio().getEstado());
            domicilio.setCodigoPostal(cliente.getDomicilio().getCodigoPostal());
            domicilio.setPais(cliente.getDomicilio().getPais());
            respuesta.setDomicilio(domicilio);
        }

        if (cliente.getCuentas() != null) {
            respuesta.setCuentas(cliente.getCuentas().stream().map(cuenta -> {
                ClienteResponse.CuentaResumen resumen = new ClienteResponse.CuentaResumen();
                resumen.setNumeroCuenta(cuenta.getNumeroCuenta());
                resumen.setSaldo(cuenta.getSaldo());
                resumen.setEstatus(cuenta.getEstatus());
                resumen.setFechaCreacion(cuenta.getFechaCreacion());
                return resumen;
            }).toList());
        }

        return respuesta;
    }

    private CuentaResponse toCuentaResponse(Cuenta cuenta) {
        CuentaResponse respuesta = new CuentaResponse();
        respuesta.setNumeroCuenta(cuenta.getNumeroCuenta());
        respuesta.setClienteId(cuenta.getCliente() != null ? cuenta.getCliente().getId() : null);
        respuesta.setSaldo(cuenta.getSaldo());
        respuesta.setEstatus(cuenta.getEstatus());
        respuesta.setFechaCreacion(cuenta.getFechaCreacion());
        return respuesta;
    }
}