package com.fonchys.minimarket.service;

import com.fonchys.minimarket.model.Producto;
import com.fonchys.minimarket.repository.ProductoRepository;
import com.fonchys.minimarket.service.impl.ProductoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductoService - Pruebas unitarias (TDD)")
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private Producto productoBase;

    @BeforeEach
    void setUp() {
        productoBase = new Producto();
        productoBase.setId(1L);
        productoBase.setNombre("Coca Cola 500ml");
        productoBase.setPrecio(new BigDecimal("2.50"));
        productoBase.setStock(100);
        productoBase.setActivo(true);
    }

    @Test
    @DisplayName("listarActivos: debe retornar productos activos")
    void listarActivos_debeRetornarListaNoVacia() {
        when(productoRepository.findByActivoTrue()).thenReturn(Arrays.asList(productoBase));

        List<Producto> resultado = productoService.listarActivos();

        assertThat(resultado).isNotEmpty();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Coca Cola 500ml");
        verify(productoRepository).findByActivoTrue();
    }

    @Test
    @DisplayName("guardar: debe persistir el producto y retornarlo")
    void guardar_conProductoValido_debePersistir() {
        when(productoRepository.save(any(Producto.class))).thenReturn(productoBase);

        Producto resultado = productoService.guardar(productoBase);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        verify(productoRepository).save(productoBase);
    }

    @Test
    @DisplayName("guardar: debe lanzar excepción con producto nulo")
    void guardar_conProductoNulo_debeLanzarNullPointerException() {
        assertThatThrownBy(() -> productoService.guardar(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("reducirStock: debe descontar cuando hay stock suficiente")
    void reducirStock_conStockSuficiente_debeRetornarTrue() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoBase));
        when(productoRepository.save(any())).thenReturn(productoBase);

        boolean resultado = productoService.reducirStock(1L, 10);

        assertThat(resultado).isTrue();
        assertThat(productoBase.getStock()).isEqualTo(90);
        verify(productoRepository).save(productoBase);
    }

    @Test
    @DisplayName("reducirStock: debe retornar false cuando stock es insuficiente")
    void reducirStock_conStockInsuficiente_debeRetornarFalse() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoBase));

        boolean resultado = productoService.reducirStock(1L, 200);

        assertThat(resultado).isFalse();
        assertThat(productoBase.getStock()).isEqualTo(100); // sin cambios
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("buscarPorNombre: debe lanzar excepción con nombre en blanco")
    void buscarPorNombre_conNombreBlanco_debeLanzarExcepcion() {
        assertThatThrownBy(() -> productoService.buscarPorNombre(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("vacío");
    }

    @Test
    @DisplayName("buscarPorNombre: debe buscar con nombre válido")
    void buscarPorNombre_conNombreValido_debeRetornarResultados() {
        when(productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue("coca"))
            .thenReturn(Arrays.asList(productoBase));

        List<Producto> resultado = productoService.buscarPorNombre("coca");

        assertThat(resultado).hasSize(1);
        verify(productoRepository).findByNombreContainingIgnoreCaseAndActivoTrue("coca");
    }

    @Test
    @DisplayName("desactivar: debe marcar el producto como inactivo")
    void desactivar_conIdValido_debeDesactivarProducto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoBase));
        when(productoRepository.save(any())).thenReturn(productoBase);

        productoService.desactivar(1L);

        assertThat(productoBase.getActivo()).isFalse();
        verify(productoRepository).save(productoBase);
    }

    @Test
    @DisplayName("desactivar: debe lanzar excepción si el producto no existe")
    void desactivar_conIdInexistente_debeLanzarExcepcion() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.desactivar(99L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("99");
    }
}
