package com.fonchys.minimarket.controller;

import com.fonchys.minimarket.dto.ProductoRequestDTO;
import com.fonchys.minimarket.model.Categoria;
import com.fonchys.minimarket.model.Producto;
import com.fonchys.minimarket.model.Proveedor;
import com.fonchys.minimarket.repository.CategoriaRepository;
import com.fonchys.minimarket.repository.ProveedorRepository;
import com.fonchys.minimarket.service.IProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * API REST de productos. Reutiliza la misma capa de servicio que {@link ProductoController} (MVC).
 */
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoRestController {

    private final IProductoService productoService;
    private final CategoriaRepository categoriaRepository;
    private final ProveedorRepository proveedorRepository;

    @GetMapping
    public List<Producto> listar(@RequestParam(required = false) String buscar) {
        return (buscar != null && !buscar.isBlank())
            ? productoService.buscarPorNombre(buscar)
            : productoService.listarActivos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtener(@PathVariable Long id) {
        Producto producto = productoService.buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
        return ResponseEntity.ok(producto);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    public ResponseEntity<Producto> crear(@Valid @RequestBody ProductoRequestDTO dto) {
        Producto producto = mapearAEntidad(new Producto(), dto);
        Producto guardado = productoService.guardar(producto);
        return ResponseEntity.created(URI.create("/api/productos/" + guardado.getId())).body(guardado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequestDTO dto) {
        Producto datos = mapearAEntidad(new Producto(), dto);
        Producto actualizado = productoService.actualizar(id, datos);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    private Producto mapearAEntidad(Producto producto, ProductoRequestDTO dto) {
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setPrecioCompra(dto.getPrecioCompra());
        producto.setStock(dto.getStock());
        if (dto.getStockMinimo() != null) {
            producto.setStockMinimo(dto.getStockMinimo());
        }
        if (dto.getUnidadMedida() != null) {
            producto.setUnidadMedida(dto.getUnidadMedida());
        }
        producto.setContenidoNeto(dto.getContenidoNeto());
        producto.setCodigoBarras(dto.getCodigoBarras());

        if (dto.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + dto.getCategoriaId()));
            producto.setCategoria(categoria);
        }
        if (dto.getProveedorId() != null) {
            Proveedor proveedor = proveedorRepository.findById(dto.getProveedorId())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + dto.getProveedorId()));
            producto.setProveedor(proveedor);
        }
        return producto;
    }
}
