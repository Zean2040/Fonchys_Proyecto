package com.fonchys.minimarket.service;

import com.fonchys.minimarket.dto.VentaRequestDTO;
import com.fonchys.minimarket.model.Producto;
import com.fonchys.minimarket.model.Usuario;
import com.fonchys.minimarket.model.Venta;
import com.fonchys.minimarket.repository.ProductoRepository;
import com.fonchys.minimarket.repository.VentaRepository;
import com.fonchys.minimarket.service.impl.VentaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VentaService - Pruebas unitarias (TDD)")
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private IUsuarioService usuarioService;

    @InjectMocks
    private VentaServiceImpl ventaService;

    private Producto producto;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Coca Cola 500ml");
        producto.setPrecio(new BigDecimal("2.50"));
        producto.setStock(50);
        producto.setActivo(true);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Cajero Test");
        usuario.setEmail("cajero@fonchys.com");
        usuario.setRol(Usuario.Rol.CAJERO);
    }

    @Test
    @DisplayName("registrarVenta: debe crear venta con datos válidos")
    void registrarVenta_conDatosValidos_debeGuardarVenta() {
        when(usuarioService.buscarPorEmail("cajero@fonchys.com")).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setId(1L);
            return v;
        });

        VentaRequestDTO dto = crearDTO(1L, 2);
        Venta resultado = ventaService.registrarVenta(dto, "cajero@fonchys.com");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getDetalles()).hasSize(1);
        assertThat(resultado.getTotal()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(producto.getStock()).isEqualTo(48); // 50 - 2
    }

    @Test
    @DisplayName("registrarVenta: debe lanzar excepción con stock insuficiente")
    void registrarVenta_conStockInsuficiente_debeLanzarExcepcion() {
        producto.setStock(1); // solo 1 en stock
        when(usuarioService.buscarPorEmail("cajero@fonchys.com")).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        VentaRequestDTO dto = crearDTO(1L, 10); // pide 10

        assertThatThrownBy(() -> ventaService.registrarVenta(dto, "cajero@fonchys.com"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Stock insuficiente");
    }

    @Test
    @DisplayName("registrarVenta: debe lanzar excepción con email nulo")
    void registrarVenta_conEmailNulo_debeLanzarExcepcion() {
        VentaRequestDTO dto = crearDTO(1L, 1);

        assertThatThrownBy(() -> ventaService.registrarVenta(dto, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("registrarVenta: debe lanzar excepción con items vacíos")
    void registrarVenta_conItemsVacios_debeLanzarExcepcion() {
        when(usuarioService.buscarPorEmail(anyString())).thenReturn(Optional.of(usuario));

        VentaRequestDTO dto = new VentaRequestDTO();
        dto.setItems(List.of());

        assertThatThrownBy(() -> ventaService.registrarVenta(dto, "cajero@fonchys.com"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("al menos un producto");
    }

    @Test
    @DisplayName("anularVenta: debe restaurar el stock al anular")
    void anularVenta_debeRestaurarStock() {
        Venta venta = new Venta();
        venta.setId(1L);
        venta.setEstado(Venta.Estado.COMPLETADA);

        com.fonchys.minimarket.model.DetalleVenta detalle = new com.fonchys.minimarket.model.DetalleVenta();
        detalle.setProducto(producto);
        detalle.setCantidad(5);
        venta.getDetalles().add(detalle);

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));
        when(ventaRepository.save(any())).thenReturn(venta);

        ventaService.anularVenta(1L);

        assertThat(venta.getEstado()).isEqualTo(Venta.Estado.ANULADA);
        assertThat(producto.getStock()).isEqualTo(55); // 50 + 5 restaurados
    }

    private VentaRequestDTO crearDTO(Long productoId, int cantidad) {
        VentaRequestDTO dto = new VentaRequestDTO();
        VentaRequestDTO.ItemDTO item = new VentaRequestDTO.ItemDTO();
        item.setProductoId(productoId);
        item.setCantidad(cantidad);
        dto.setItems(List.of(item));
        return dto;
    }
}
