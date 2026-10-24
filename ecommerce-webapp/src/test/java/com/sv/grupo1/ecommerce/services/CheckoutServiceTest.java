package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.ecommerce.dao.DetallePedidoRepository;
import com.sv.grupo1.ecommerce.dao.MovimientoInventarioRepository;
import com.sv.grupo1.ecommerce.dao.MunicipioRepository;
import com.sv.grupo1.ecommerce.dao.PagoRepository;
import com.sv.grupo1.ecommerce.dao.PedidoRepository;
import com.sv.grupo1.ecommerce.dao.ProductoRepository;
import com.sv.grupo1.ecommerce.dao.UsuarioDireccionRepository;
import com.sv.grupo1.ecommerce.dao.UsuarioRepository;
import com.sv.grupo1.ecommerce.dto.CheckoutDTO;
import com.sv.grupo1.ecommerce.dto.RespuestaPagoPasarelaDTO;
import com.sv.grupo1.ecommerce.dto.ResultadoCompraDTO;
import com.sv.grupo1.ecommerce.entities.catalogo.Municipio;
import com.sv.grupo1.ecommerce.entities.core.DetallePedido;
import com.sv.grupo1.ecommerce.entities.core.MovimientoInventario;
import com.sv.grupo1.ecommerce.entities.core.Pago;
import com.sv.grupo1.ecommerce.entities.core.Pedido;
import com.sv.grupo1.ecommerce.entities.core.Producto;
import com.sv.grupo1.ecommerce.entities.seguridad.Usuario;
import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioDireccion;
import com.sv.grupo1.ecommerce.enums.EstadoPasarela;
import com.sv.grupo1.ecommerce.services.impl.CheckoutServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CheckoutServiceTest {

    private static final String CORREO = "cliente@example.com";

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final UsuarioDireccionRepository usuarioDireccionRepository = mock(UsuarioDireccionRepository.class);
    private final MunicipioRepository municipioRepository = mock(MunicipioRepository.class);
    private final ProductoRepository productoRepository = mock(ProductoRepository.class);
    private final PedidoRepository pedidoRepository = mock(PedidoRepository.class);
    private final DetallePedidoRepository detallePedidoRepository = mock(DetallePedidoRepository.class);
    private final PagoRepository pagoRepository = mock(PagoRepository.class);
    private final MovimientoInventarioRepository movimientoInventarioRepository = mock(MovimientoInventarioRepository.class);
    private final CarritoService carritoService = mock(CarritoService.class);
    private final PasarelaPagoService pasarelaPagoService = mock(PasarelaPagoService.class);

    private final CheckoutService checkoutService = new CheckoutServiceImpl(
            usuarioRepository,
            usuarioDireccionRepository,
            municipioRepository,
            productoRepository,
            pedidoRepository,
            detallePedidoRepository,
            pagoRepository,
            movimientoInventarioRepository,
            carritoService,
            pasarelaPagoService
    );

    @Test
    void noDebeFinalizarCompraConCarritoVacio() {
        Usuario usuario = crearUsuario(1);
        when(usuarioRepository.findByCorreoInicioSesionIgnoreCase(CORREO)).thenReturn(Optional.of(usuario));
        when(carritoService.obtenerCarrito(1)).thenReturn(List.of());

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> checkoutService.finalizarCompra(CORREO, crearCheckoutDTO(3))
        );

        assertEquals("Tu carrito está vacío.", error.getMessage());
        verify(pedidoRepository, never()).save(any());
        verifyNoInteractions(pasarelaPagoService);
    }

    @Test
    void noDebeFinalizarCompraSinStockSuficiente() {
        Usuario usuario = crearUsuario(1);
        Producto producto = crearProducto(10, 1);
        when(usuarioRepository.findByCorreoInicioSesionIgnoreCase(CORREO)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(10)).thenReturn(Optional.of(producto));
        when(carritoService.obtenerCarrito(1)).thenReturn(List.of(crearItemCarrito(10, 3)));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> checkoutService.finalizarCompra(CORREO, crearCheckoutDTO(3))
        );

        assertEquals("Stock insuficiente para 'LAPTOP HP'. Disponible: 1 unidades.", error.getMessage());
        verify(pedidoRepository, never()).save(any());
        verifyNoInteractions(pasarelaPagoService);
    }

    @Test
    void noDebeAceptarDireccionDeOtroUsuario() {
        Usuario usuario = crearUsuario(1);
        UsuarioDireccion direccionAjena = new UsuarioDireccion();
        direccionAjena.setIdDireccion(3);
        direccionAjena.setUsuario(crearUsuario(2));
        when(usuarioRepository.findByCorreoInicioSesionIgnoreCase(CORREO)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(10)).thenReturn(Optional.of(crearProducto(10, 5)));
        when(carritoService.obtenerCarrito(1)).thenReturn(List.of(crearItemCarrito(10, 2)));
        when(usuarioDireccionRepository.findById(3)).thenReturn(Optional.of(direccionAjena));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> checkoutService.finalizarCompra(CORREO, crearCheckoutDTO(3))
        );

        assertEquals("La dirección seleccionada no pertenece a tu cuenta.", error.getMessage());
        verify(pedidoRepository, never()).save(any());
        verifyNoInteractions(pasarelaPagoService);
    }

    @Test
    void debeConfirmarPedidoYDescontarStockCuandoElPagoEsAprobado() {
        Usuario usuario = crearUsuario(1);
        Producto producto = crearProducto(10, 5);
        prepararCompraValida(usuario, producto, 2);
        when(pasarelaPagoService.procesarPago(any(), eq("PED-7")))
                .thenReturn(crearRespuestaPasarela(EstadoPasarela.APPROVED, "Transacción aprobada", "pay_123"));

        ResultadoCompraDTO resultado = checkoutService.finalizarCompra(CORREO, crearCheckoutDTO(3));

        assertTrue(resultado.isAprobado());
        assertEquals(7, resultado.getIdPedido());
        assertEquals(3, producto.getStockDisponible());

        ArgumentCaptor<Pedido> pedidoCaptor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository, atLeastOnce()).save(pedidoCaptor.capture());
        assertEquals('C', pedidoCaptor.getValue().getEstadoPedido());
        assertEquals(new BigDecimal("200.00"), pedidoCaptor.getValue().getTotal());

        ArgumentCaptor<Pago> pagoCaptor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(pagoCaptor.capture());
        assertEquals('E', pagoCaptor.getValue().getEstadoTransaccion());
        assertEquals("TARJETA **** 1234", pagoCaptor.getValue().getMetodoPago());
        assertEquals("pay_123", pagoCaptor.getValue().getReferenciaPasarela());
        assertNotNull(pagoCaptor.getValue().getFechaPago());

        ArgumentCaptor<MovimientoInventario> movimientoCaptor = ArgumentCaptor.forClass(MovimientoInventario.class);
        verify(movimientoInventarioRepository).save(movimientoCaptor.capture());
        assertEquals("SALIDA", movimientoCaptor.getValue().getTipoMovimiento());
        assertEquals(2, movimientoCaptor.getValue().getCantidad());
        assertEquals(7, movimientoCaptor.getValue().getIdPedido());

        verify(carritoService).vaciarCarrito(1);
    }

    @Test
    void debeAnularPedidoYConservarStockCuandoElPagoEsRechazado() {
        Usuario usuario = crearUsuario(1);
        Producto producto = crearProducto(10, 5);
        prepararCompraValida(usuario, producto, 2);
        when(pasarelaPagoService.procesarPago(any(), eq("PED-7")))
                .thenReturn(crearRespuestaPasarela(EstadoPasarela.DECLINED, "Fondos insuficientes", "pay_456"));

        ResultadoCompraDTO resultado = checkoutService.finalizarCompra(CORREO, crearCheckoutDTO(3));

        assertFalse(resultado.isAprobado());
        assertEquals("Fondos insuficientes", resultado.getMensaje());
        assertEquals(5, producto.getStockDisponible());

        ArgumentCaptor<Pedido> pedidoCaptor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository, atLeastOnce()).save(pedidoCaptor.capture());
        assertEquals('A', pedidoCaptor.getValue().getEstadoPedido());

        ArgumentCaptor<Pago> pagoCaptor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(pagoCaptor.capture());
        assertEquals('R', pagoCaptor.getValue().getEstadoTransaccion());
        assertNull(pagoCaptor.getValue().getFechaPago());

        verify(movimientoInventarioRepository, never()).save(any());
        verify(carritoService, never()).vaciarCarrito(any());
    }

    @Test
    void debeAnularPedidoCuandoElPagoQuedaPendiente() {
        Usuario usuario = crearUsuario(1);
        Producto producto = crearProducto(10, 5);
        prepararCompraValida(usuario, producto, 1);
        when(pasarelaPagoService.procesarPago(any(), eq("PED-7")))
                .thenReturn(crearRespuestaPasarela(EstadoPasarela.PENDING, "Pago en validación asíncrona", "pay_789"));

        ResultadoCompraDTO resultado = checkoutService.finalizarCompra(CORREO, crearCheckoutDTO(3));

        assertFalse(resultado.isAprobado());
        assertEquals(5, producto.getStockDisponible());

        ArgumentCaptor<Pago> pagoCaptor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(pagoCaptor.capture());
        assertEquals('P', pagoCaptor.getValue().getEstadoTransaccion());
        verify(carritoService, never()).vaciarCarrito(any());
    }

    @Test
    void noDebeFinalizarCompraSinDireccionNiDatosDeDireccionNueva() {
        Usuario usuario = crearUsuario(1);
        Producto producto = crearProducto(10, 5);
        prepararCompraValida(usuario, producto, 1);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> checkoutService.finalizarCompra(CORREO, crearCheckoutDTO(null))
        );

        assertEquals("Selecciona una dirección de entrega o ingresa una nueva.", error.getMessage());
        verify(pedidoRepository, never()).save(any());
        verifyNoInteractions(pasarelaPagoService);
    }

    @Test
    void debeCrearDireccionNuevaCuandoNoSeEligeUnaExistente() {
        Usuario usuario = crearUsuario(1);
        Producto producto = crearProducto(10, 5);
        prepararCompraValida(usuario, producto, 1);
        Municipio municipio = new Municipio();
        municipio.setIdMunicipio(4);
        when(municipioRepository.findById(4)).thenReturn(Optional.of(municipio));
        when(usuarioDireccionRepository.save(any(UsuarioDireccion.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
        when(pasarelaPagoService.procesarPago(any(), eq("PED-7")))
                .thenReturn(crearRespuestaPasarela(EstadoPasarela.APPROVED, "Transacción aprobada", "pay_321"));

        CheckoutDTO dto = crearCheckoutDTO(null);
        dto.setDireccionLinea1("  Calle Nueva 45  ");
        dto.setIdMunicipio(4);
        dto.setCodigoPostal("1501");

        ResultadoCompraDTO resultado = checkoutService.finalizarCompra(CORREO, dto);

        assertTrue(resultado.isAprobado());
        ArgumentCaptor<UsuarioDireccion> direccionCaptor = ArgumentCaptor.forClass(UsuarioDireccion.class);
        verify(usuarioDireccionRepository).save(direccionCaptor.capture());
        assertEquals("Calle Nueva 45", direccionCaptor.getValue().getDireccionLinea1());
        assertEquals("1501", direccionCaptor.getValue().getCodigoPostal());
        assertSame(usuario, direccionCaptor.getValue().getUsuario());
        assertSame(municipio, direccionCaptor.getValue().getMunicipio());
    }

    @Test
    void noDebeMostrarConfirmacionDePedidoDeOtroUsuario() {
        when(usuarioRepository.findByCorreoInicioSesionIgnoreCase(CORREO)).thenReturn(Optional.of(crearUsuario(1)));
        when(pedidoRepository.findById(7)).thenReturn(Optional.of(crearPedido(crearUsuario(2), 'C')));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> checkoutService.obtenerConfirmacion(CORREO, 7)
        );

        assertEquals("El pedido no pertenece a tu cuenta.", error.getMessage());
    }

    @Test
    void noDebeMostrarConfirmacionDePedidoNoConfirmado() {
        Usuario usuario = crearUsuario(1);
        when(usuarioRepository.findByCorreoInicioSesionIgnoreCase(CORREO)).thenReturn(Optional.of(usuario));
        when(pedidoRepository.findById(7)).thenReturn(Optional.of(crearPedido(usuario, 'A')));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> checkoutService.obtenerConfirmacion(CORREO, 7)
        );

        assertEquals("El pedido no fue confirmado.", error.getMessage());
    }

    private void prepararCompraValida(Usuario usuario, Producto producto, int cantidad) {
        UsuarioDireccion direccion = new UsuarioDireccion();
        direccion.setIdDireccion(3);
        direccion.setUsuario(usuario);

        when(usuarioRepository.findByCorreoInicioSesionIgnoreCase(CORREO)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(producto.getIdProducto())).thenReturn(Optional.of(producto));
        when(carritoService.obtenerCarrito(usuario.getIdUsuario()))
                .thenReturn(List.of(crearItemCarrito(producto.getIdProducto(), cantidad)));
        when(usuarioDireccionRepository.findById(3)).thenReturn(Optional.of(direccion));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocacion -> {
            Pedido pedido = invocacion.getArgument(0);
            pedido.setIdPedido(7);
            return pedido;
        });
    }

    private Usuario crearUsuario(Integer idUsuario) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(idUsuario);
        usuario.setCorreoInicioSesion(CORREO);
        return usuario;
    }

    private Pedido crearPedido(Usuario usuario, Character estado) {
        Pedido pedido = new Pedido();
        pedido.setIdPedido(7);
        pedido.setUsuario(usuario);
        pedido.setEstadoPedido(estado);
        return pedido;
    }

    private Producto crearProducto(Integer idProducto, int stock) {
        Producto producto = new Producto();
        producto.setIdProducto(idProducto);
        producto.setNombreProducto("LAPTOP HP");
        producto.setCodigoSku("ELC-COM-LAP-HP-0001");
        producto.setPrecioVenta(new BigDecimal("100.00"));
        producto.setStockDisponible(stock);
        producto.setEstado('A');
        return producto;
    }

    private DetallePedido crearItemCarrito(Integer idProducto, int cantidad) {
        Producto producto = new Producto();
        producto.setIdProducto(idProducto);

        DetallePedido item = new DetallePedido();
        item.setProducto(producto);
        item.setCantidad(cantidad);
        return item;
    }

    private CheckoutDTO crearCheckoutDTO(Integer idDireccion) {
        CheckoutDTO dto = new CheckoutDTO();
        dto.setIdDireccion(idDireccion);
        dto.setNombreTitular("Cliente Prueba");
        dto.setNumeroTarjeta("4111111111111234");
        dto.setFechaExpiracion("12/29");
        dto.setCvv("123");
        return dto;
    }

    private RespuestaPagoPasarelaDTO crearRespuestaPasarela(EstadoPasarela estado, String mensaje, String transaccion) {
        RespuestaPagoPasarelaDTO respuesta = new RespuestaPagoPasarelaDTO();
        respuesta.setStatus(estado);
        respuesta.setResponseMessage(mensaje);
        respuesta.setTransactionId(transaccion);
        return respuesta;
    }
}
