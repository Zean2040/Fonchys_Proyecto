package com.fonchys.minimarket.service;

import com.fonchys.minimarket.model.Producto;
import com.fonchys.minimarket.repository.ProductoRepository;
import com.fonchys.minimarket.service.impl.ProductoServiceImpl;
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
 * Pruebas unitarias — ProductoService
 * Mockito simula el repositorio: no se necesita base de datos ni Spring.
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
class ProductoServiceTest {

    private static final String SEP  = "═".repeat(60);
    private static final String LINE = "─".repeat(60);

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;

    @BeforeAll
    static void inicio() {
        System.out.println("\n" + SEP);
        System.out.println("  PRUEBAS UNITARIAS — ProductoService");
        System.out.println("  Capa: Servicio | Framework: JUnit 5 + Mockito");
        System.out.println(SEP);
    }

    @AfterAll
    static void fin() {
        System.out.println("\n" + SEP);
        System.out.println("  FIN — ProductoService");
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

    private Producto productoEjemplo(Long id, String nombre, int stock) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre(nombre);
        p.setStock(stock);
        p.setPrecio(new BigDecimal("10.00"));
        p.setActivo(true);
        return p;
    }

    @Test
    @DisplayName("[Producto-01] listarActivos devuelve todos los productos activos del repositorio")
    void listarActivos_devuelveProductosDelRepositorio() {
        List<Producto> esperados = List.of(
            productoEjemplo(1L, "Coca Cola", 20),
            productoEjemplo(2L, "Pan de Molde", 10)
        );
        when(productoRepository.findByActivoTrue()).thenReturn(esperados);
        System.out.println("  Mock: repositorio devuelve 2 productos activos");

        List<Producto> resultado = productoService.listarActivos();

        assertEquals(2, resultado.size());
        assertEquals("Coca Cola", resultado.get(0).getNombre());
        verify(productoRepository, atLeastOnce()).findByActivoTrue();
        System.out.println("  Recibidos: " + resultado.size() + " productos — nombres correctos");
    }

    @Test
    @DisplayName("[Producto-02] buscarPorNombre con texto válido devuelve coincidencias")
    void buscarPorNombre_conNombreValido_devuelveCoindicencias() {
        List<Producto> esperados = List.of(productoEjemplo(1L, "Coca Cola", 20));
        when(productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue("coca"))
            .thenReturn(esperados);
        System.out.println("  Búsqueda: 'coca' → mock devuelve 1 producto");

        List<Producto> resultado = productoService.buscarPorNombre("coca");

        assertEquals(1, resultado.size());
        assertEquals("Coca Cola", resultado.get(0).getNombre());
        System.out.println("  Encontrado: " + resultado.get(0).getNombre());
    }

    @Test
    @DisplayName("[Producto-03] buscarPorNombre con nombre vacío lanza excepción — Guava valida el input")
    void buscarPorNombre_conNombreVacio_lanzaExcepcion() {
        System.out.println("  Entrada: nombre vacío ''");
        System.out.println("  Guava Preconditions.checkArgument lanza IllegalArgumentException");

        assertThrows(IllegalArgumentException.class,
            () -> productoService.buscarPorNombre(""));

        verify(productoRepository, never()).findByNombreContainingIgnoreCaseAndActivoTrue(any());
        System.out.println("  Excepción lanzada — repositorio nunca fue invocado");
    }

    @Test
    @DisplayName("[Producto-04] buscarPorId con ID existente devuelve el producto")
    void buscarPorId_conIdExistente_devuelveProducto() {
        Producto producto = productoEjemplo(5L, "Leche Gloria", 15);
        when(productoRepository.findById(5L)).thenReturn(Optional.of(producto));
        System.out.println("  Mock: producto ID=5 'Leche Gloria' existe en BD");

        Optional<Producto> resultado = productoService.buscarPorId(5L);

        assertTrue(resultado.isPresent());
        assertEquals("Leche Gloria", resultado.get().getNombre());
        System.out.println("  Encontrado: " + resultado.get().getNombre() + " (ID=" + resultado.get().getId() + ")");
    }

    @Test
    @DisplayName("[Producto-05] buscarPorId con ID inexistente devuelve Optional vacío")
    void buscarPorId_conIdInexistente_devuelveVacio() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());
        System.out.println("  Mock: producto ID=99 NO existe en BD");

        Optional<Producto> resultado = productoService.buscarPorId(99L);

        assertFalse(resultado.isPresent());
        System.out.println("  Resultado: Optional.empty() — producto no encontrado");
    }

    @Test
    @DisplayName("[Producto-06] reducirStock con stock suficiente descuenta la cantidad correctamente")
    void reducirStock_conStockSuficiente_reduceCorrectamente() {
        Producto producto = productoEjemplo(1L, "Agua San Luis", 20);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);
        System.out.println("  Stock inicial: 20 | Cantidad a reducir: 5 | Stock esperado: 15");

        boolean resultado = productoService.reducirStock(1L, 5);

        assertTrue(resultado);
        assertEquals(15, producto.getStock());
        verify(productoRepository).save(producto);
        System.out.println("  retornó true | Stock resultante: " + producto.getStock() + " unidades ✔");
    }

    @Test
    @DisplayName("[Producto-07] reducirStock con stock insuficiente retorna false sin modificar la BD")
    void reducirStock_conStockInsuficiente_retornaFalse() {
        Producto producto = productoEjemplo(1L, "Agua San Luis", 3);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        System.out.println("  Stock disponible: 3 | Cantidad solicitada: 10");
        System.out.println("  Se espera: false, stock no cambia, save() nunca es llamado");

        boolean resultado = productoService.reducirStock(1L, 10);

        assertFalse(resultado);
        assertEquals(3, producto.getStock());
        verify(productoRepository, never()).save(any());
        System.out.println("  retornó false | Stock sigue en 3 | BD no fue modificada ✔");
    }
}
