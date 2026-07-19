package com.fonchys.minimarket.service;

import com.fonchys.minimarket.model.Producto;

import java.util.List;
import java.util.Optional;

public interface IProductoService {

    List<Producto> listarActivos();

    List<Producto> buscarPorNombre(String nombre);

    List<Producto> listarPorCategoria(Long categoriaId);

    List<Producto> listarStockBajo(int umbral);
    List<Producto> listarStockBajoIndividual();
    List<Producto> listarStockBajoPorProveedor(Long proveedorId, int umbral);

    Optional<Producto> buscarPorId(Long id);

    Producto guardar(Producto producto);

    Producto actualizar(Long id, Producto producto);

    void desactivar(Long id);

    boolean reducirStock(Long productoId, int cantidad);
}
