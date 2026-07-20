package com.fonchys.minimarket.service;

import com.fonchys.minimarket.dto.VentaRequestDTO;
import com.fonchys.minimarket.model.Producto;
import com.fonchys.minimarket.model.Usuario;
import com.fonchys.minimarket.model.Venta;
import com.fonchys.minimarket.repository.ProductoRepository;
import com.fonchys.minimarket.repository.VentaRepository;
import com.fonchys.minimarket.service.impl.VentaServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias — VentaService
 * Mockito simula repositorios y servicios: no se necesita base de datos.
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
class VentaServiceTest {

    private static final String SEP  = "═".repeat(60);
    private static final String LINE = "─".repeat(60);

    @Mock private VentaRepository ventaRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private IUsuarioService usuarioService;

    @InjectMocks
    private VentaServiceImpl ventaService;

    @BeforeAll
    static void inicio() {
        System.out.println("\n" + SEP);
        System.out.println("  PRUEBAS UNITARIAS — VentaService");
        System.out.println("  Capa: Servicio | Framework: JUnit 5 + Mockito");
        System.out.println(SEP);
    }

    @AfterAll
    static void fin() {
        System.out.println("\n" + SEP);
        System.out.println("  FIN — VentaService");
        System.out.println(SEP + "\n");
    }

    @BeforeEach
    void antes(TestInfo info) {
        System.out.println("\n" + LINE);
        System.out.println("  EJECUTANDO: " + info.getDisplayName());
        System.out.println(LINE);
    }

    @AfterEach
    void despues(TestInfo info) {
        System.out.println("  ✔  PASÓ: " + info.getDisplayName());
    }

    private Producto productoConStock(Long id, String nombre, int stock, BigDecimal precio) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre(nombre);
        p.setStock(stock);
        p.setPrecio(precio);
        p.setActivo(true);
        return p;
    }

    private VentaRequestDTO dtoConItem(Long productoId, int cantidad) {
        VentaRequestDTO.ItemDTO item = new VentaRequestDTO.ItemDTO();
        item.setProductoId(productoId);
        item.setCantidad(cantidad);
        VentaRequestDTO dto = new VentaRequestDTO();
        dto.setItems(List.of(item));
        return dto;
    }

    @Test
    @DisplayName("[Venta-01] registrarVenta exitosa descuenta stock y calcula total correctamente")
    void registrarVenta_exitosa_calculaTotalYDescuentaStock() {
        Producto producto = productoConStock(1L, "Coca Cola 500ml", 20, new BigDecimal("3.50"));
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("cajero@fonchys.com");

        when(usuarioService.buscarPorEmail("cajero@fonchys.com")).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        System.out.println("  Producto: Coca Cola 500ml | Precio: S/.3.50 | Stock inicial: 20");
        System.out.println("  Cantidad vendida: 2 | Total esperado: S/.7.00");

        Venta venta = ventaService.registrarVenta(dtoConItem(1L, 2), "cajero@fonchys.com");

        assertNotNull(venta);
        assertEquals(new BigDecimal("7.00"), venta.getTotal());
        assertEquals(18, producto.getStock());
        assertEquals(1, venta.getDetalles().size());
        System.out.println("  Total calculado: S/." + venta.getTotal() + " | Stock restante: " + producto.getStock());
    }

    @Test
    @DisplayName("[Venta-02] registrarVenta con stock insuficiente lanza excepción y no registra venta")
    void registrarVenta_conStockInsuficiente_lanzaExcepcion() {
        Producto producto = productoConStock(1L, "Leche Gloria", 2, new BigDecimal("4.50"));
        Usuario usuario = new Usuario();
        usuario.setEmail("cajero@fonchys.com");

        when(usuarioService.buscarPorEmail("cajero@fonchys.com")).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        System.out.println("  Stock disponible: 2 | Cantidad solicitada: 5");
        System.out.println("  Se espera: RuntimeException — venta no debe registrarse");

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> ventaService.registrarVenta(dtoConItem(1L, 5), "cajero@fonchys.com"));

        assertTrue(ex.getMessage().contains("Stock insuficiente"));
        verify(ventaRepository, never()).save(any());
        System.out.println("  Excepción: " + ex.getMessage());
        System.out.println("  ventaRepository.save() nunca fue llamado — BD intacta ✔");
    }

    @Test
    @DisplayName("[Venta-03] registrarVenta sin items lanza excepción de validación")
    void registrarVenta_sinItems_lanzaExcepcionDeValidacion() {
        VentaRequestDTO dto = new VentaRequestDTO();
        dto.setItems(List.of());
        System.out.println("  DTO enviado: lista de items vacía");
        System.out.println("  Guava Preconditions valida que debe haber al menos 1 item");

        assertThrows(IllegalArgumentException.class,
            () -> ventaService.registrarVenta(dto, "cajero@fonchys.com"));

        verify(usuarioService, never()).buscarPorEmail(any());
        System.out.println("  Excepción lanzada — ningún repositorio fue consultado ✔");
    }

    @Test
    @DisplayName("[Venta-04] anularVenta restaura el stock de los productos involucrados")
    void anularVenta_restauraStockDeProductos() {
        Producto producto = productoConStock(1L, "Agua San Luis", 8, new BigDecimal("1.50"));

        com.fonchys.minimarket.model.DetalleVenta detalle = new com.fonchys.minimarket.model.DetalleVenta();
        detalle.setProducto(producto);
        detalle.setCantidad(3);

        Venta venta = new Venta();
        venta.setId(10L);
        venta.setEstado(Venta.Estado.COMPLETADA);
        venta.getDetalles().add(detalle);

        when(ventaRepository.findById(10L)).thenReturn(Optional.of(venta));
        when(productoRepository.save(any())).thenReturn(producto);
        when(ventaRepository.save(any())).thenReturn(venta);

        System.out.println("  Venta ID=10 | Producto: Agua San Luis | Stock actual: 8 | Cantidad anulada: 3");
        System.out.println("  Se espera: stock restaurado a 11, estado = ANULADA");

        ventaService.anularVenta(10L);

        assertEquals(11, producto.getStock());
        assertEquals(Venta.Estado.ANULADA, venta.getEstado());
        System.out.println("  Stock restaurado: " + producto.getStock() + " | Estado: " + venta.getEstado() + " ✔");
    }

    @Test
    @DisplayName("[Venta-05] anularVenta ya anulada lanza excepción — no se anula dos veces")
    void anularVenta_yaAnulada_lanzaExcepcion() {
        Venta venta = new Venta();
        venta.setId(5L);
        venta.setEstado(Venta.Estado.ANULADA);

        when(ventaRepository.findById(5L)).thenReturn(Optional.of(venta));
        System.out.println("  Venta ID=5 ya está en estado ANULADA");
        System.out.println("  Se espera: RuntimeException — no se puede anular dos veces");

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> ventaService.anularVenta(5L));

        assertTrue(ex.getMessage().contains("ya se encuentra anulada"));
        System.out.println("  Excepción: " + ex.getMessage() + " ✔");
    }
}
