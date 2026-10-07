package com.proyecto.servicios.service.clientes;

import com.proyecto.servicios.entity.sf.clientes.BiometriaCliente;
import com.proyecto.servicios.entity.sf.clientes.Cliente;
import com.proyecto.servicios.model.clientes.biometria.BiometriaRegistroRequest;
import com.proyecto.servicios.model.clientes.biometria.BiometriaResponse;
import com.proyecto.servicios.model.clientes.biometria.BiometriaValidacionRequest;
import com.proyecto.servicios.model.clientes.biometria.BiometriaValidacionResponse;
import com.proyecto.servicios.repositorys.sf.clientes.BiometriaClienteRepository;
import com.proyecto.servicios.repositorys.sf.clientes.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BiometriaServiceTest {

    @Mock
    private BiometriaClienteRepository biometriaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private BiometriaService biometriaService;

    @Test
    void registrarGuardaBiometriaExitosamente() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setActivo(true);

        BiometriaRegistroRequest request = new BiometriaRegistroRequest();
        request.setTipoBiometria("FACIAL_MEDIAPIPE");
        request.setConfianzaDeteccion(new BigDecimal("98.50"));
        request.setRostrosDetectados(1);
        request.setEmbedding("[0.12, 0.34, -0.56, 0.78, 0.90]");
        request.setFotoBase64("data:image/jpeg;base64,abc123foto");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(biometriaRepository.findByClienteId(1L)).thenReturn(Optional.empty());
        when(biometriaRepository.saveAndFlush(any(BiometriaCliente.class))).thenAnswer(i -> {
            BiometriaCliente b = i.getArgument(0);
            b.setId(100L);
            return b;
        });

        BiometriaResponse response = biometriaService.registrar(1L, request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(1L, response.getClienteId());
        assertEquals("FACIAL_MEDIAPIPE", response.getTipoBiometria());
        assertEquals(new BigDecimal("98.50"), response.getConfianzaDeteccion());
        assertEquals(1, response.getRostrosDetectados());
        assertTrue(response.isTieneFoto());
        assertEquals(5, response.getDimensionVector());
        verify(biometriaRepository).saveAndFlush(any(BiometriaCliente.class));
    }

    @Test
    void registrarFallaSiClienteNoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        BiometriaRegistroRequest request = new BiometriaRegistroRequest();
        request.setEmbedding("[0.1, 0.2, 0.3, 0.4]");

        assertThrows(ClienteNoEncontradoException.class, () -> biometriaService.registrar(99L, request));
    }

    @Test
    void registrarFallaSiClienteEstaInactivo() {
        Cliente cliente = new Cliente();
        cliente.setId(2L);
        cliente.setActivo(false);

        when(clienteRepository.findById(2L)).thenReturn(Optional.of(cliente));

        BiometriaRegistroRequest request = new BiometriaRegistroRequest();
        request.setEmbedding("[0.1, 0.2, 0.3, 0.4]");

        ErrorValidacionException ex = assertThrows(ErrorValidacionException.class,
                () -> biometriaService.registrar(2L, request));
        assertTrue(ex.getMessage().contains("inactivo"));
    }

    @Test
    void registrarFallaSiSeDetectaMasDeUnRostro() {
        Cliente cliente = new Cliente();
        cliente.setId(3L);
        cliente.setActivo(true);

        when(clienteRepository.findById(3L)).thenReturn(Optional.of(cliente));

        BiometriaRegistroRequest request = new BiometriaRegistroRequest();
        request.setRostrosDetectados(2);
        request.setEmbedding("[0.1, 0.2, 0.3, 0.4]");

        ErrorValidacionException ex = assertThrows(ErrorValidacionException.class,
                () -> biometriaService.registrar(3L, request));
        assertTrue(ex.getMessage().contains("más de un rostro"));
    }

    @Test
    void registrarFallaSiEmbeddingTieneMenosDe4Dimensiones() {
        Cliente cliente = new Cliente();
        cliente.setId(4L);
        cliente.setActivo(true);

        when(clienteRepository.findById(4L)).thenReturn(Optional.of(cliente));

        BiometriaRegistroRequest request = new BiometriaRegistroRequest();
        request.setEmbedding("[0.1, 0.2]");

        ErrorValidacionException ex = assertThrows(ErrorValidacionException.class,
                () -> biometriaService.registrar(4L, request));
        assertTrue(ex.getMessage().contains("al menos 4 dimensiones"));
    }

    @Test
    void consultarRetornaBiometriaExistente() {
        when(clienteRepository.existsById(1L)).thenReturn(true);

        BiometriaCliente b = new BiometriaCliente();
        b.setId(10L);
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        b.setCliente(cliente);
        b.setTipoBiometria("FACIAL_MEDIAPIPE");
        b.setConfianzaDeteccion(new BigDecimal("99.10"));
        b.setRostrosDetectados(1);
        b.setEmbedding("[0.5, 0.5, 0.5, 0.5]");
        b.setFechaCaptura(LocalDateTime.now());
        b.setEstatus("VIGENTE");

        when(biometriaRepository.findByClienteId(1L)).thenReturn(Optional.of(b));

        BiometriaResponse res = biometriaService.consultarPorClienteId(1L);
        assertNotNull(res);
        assertEquals(10L, res.getId());
        assertEquals(4, res.getDimensionVector());
    }

    @Test
    void consultarFallaSiClienteNoTieneBiometria() {
        when(clienteRepository.existsById(1L)).thenReturn(true);
        when(biometriaRepository.findByClienteId(1L)).thenReturn(Optional.empty());

        assertThrows(BiometriaNoEncontradaException.class, () -> biometriaService.consultarPorClienteId(1L));
    }

    @Test
    void validarRetornaCoincidenciaPositivaConSimilitudAlta() {
        when(clienteRepository.existsById(1L)).thenReturn(true);

        BiometriaCliente guardada = new BiometriaCliente();
        guardada.setEmbedding("[0.50, 0.50, 0.50, 0.50]");
        when(biometriaRepository.findByClienteId(1L)).thenReturn(Optional.of(guardada));

        BiometriaValidacionRequest request = new BiometriaValidacionRequest();
        request.setEmbedding("[0.50, 0.50, 0.49, 0.51]"); // Muy similar
        request.setUmbral(new BigDecimal("0.85"));

        BiometriaValidacionResponse response = biometriaService.validar(1L, request);

        assertTrue(response.isCoincide());
        assertTrue(response.getSimilitud() > 0.95);
        assertTrue(response.getMensaje().contains("exitosa"));
    }

    @Test
    void validarRetornaRechazoConSimilitudBaja() {
        when(clienteRepository.existsById(1L)).thenReturn(true);

        BiometriaCliente guardada = new BiometriaCliente();
        guardada.setEmbedding("[1.0, 0.0, 0.0, 0.0]");
        when(biometriaRepository.findByClienteId(1L)).thenReturn(Optional.of(guardada));

        BiometriaValidacionRequest request = new BiometriaValidacionRequest();
        request.setEmbedding("[0.0, 1.0, 0.0, 0.0]"); // Ortogonal (similitud 0)
        request.setUmbral(new BigDecimal("0.85"));

        BiometriaValidacionResponse response = biometriaService.validar(1L, request);

        assertFalse(response.isCoincide());
        assertEquals(0.0, response.getSimilitud(), 0.001);
        assertTrue(response.getMensaje().contains("rechazada"));
    }

    @Test
    void validarFallaSiDimensionesDifieren() {
        when(clienteRepository.existsById(1L)).thenReturn(true);

        BiometriaCliente guardada = new BiometriaCliente();
        guardada.setEmbedding("[1.0, 0.0, 0.0, 0.0]");
        when(biometriaRepository.findByClienteId(1L)).thenReturn(Optional.of(guardada));

        BiometriaValidacionRequest request = new BiometriaValidacionRequest();
        request.setEmbedding("[1.0, 0.0, 0.0]"); // 3 dimensiones en lugar de 4

        assertThrows(ErrorValidacionException.class, () -> biometriaService.validar(1L, request));
    }
}
