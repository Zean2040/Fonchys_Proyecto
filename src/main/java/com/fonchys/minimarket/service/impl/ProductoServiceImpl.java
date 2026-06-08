package com.fonchys.minimarket.service.impl;

import com.fonchys.minimarket.model.Producto;
import com.fonchys.minimarket.repository.ProductoRepository;
import com.fonchys.minimarket.service.IProductoService;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements IProductoService {

    private static final Logger logger = LoggerFactory.getLogger(ProductoServiceImpl.class);

    private final ProductoRepository productoRepository;

    // Google Guava Cache: evita consultas repetidas a la BD
    private final Cache<String, List<Producto>> cache = CacheBuilder.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .maximumSize(50)
        .build();

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarActivos() {
        List<Producto> cached = cache.getIfPresent("activos");
        if (cached != null) {
            logger.debug("Productos cargados desde caché Guava");
            return cached;
        }
        List<Producto> productos = productoRepository.findByActivoTrue();
        cache.put("activos", productos);
        logger.info("Listando {} productos activos desde BD", productos.size());
        return productos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String nombre) {
        // Google Guava: validación con Preconditions
        Preconditions.checkArgument(StringUtils.isNotBlank(nombre), "El nombre de búsqueda no puede estar vacío");
        // Apache Commons: normalizar texto antes de buscar
        String nombreLimpio = StringUtils.normalizeSpace(nombre.trim());
        return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombreLimpio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarPorCategoria(Long categoriaId) {
        Preconditions.checkNotNull(categoriaId, "El ID de categoría no puede ser nulo");
        return productoRepository.findByCategoriaIdAndActivoTrue(categoriaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarStockBajo(int umbral) {
        return productoRepository.findByStockBajoUmbral(umbral);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarStockBajoPorProveedor(Long proveedorId, int umbral) {
        return productoRepository.findByProveedorIdAndStockBajo(proveedorId, umbral);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Producto> buscarPorId(Long id) {
        Preconditions.checkNotNull(id, "El ID no puede ser nulo");
        return productoRepository.findById(id);
    }

    @Override
    @Transactional
    public Producto guardar(Producto producto) {
        Preconditions.checkNotNull(producto, "El producto no puede ser nulo");
        // Apache Commons: capitalizar nombre correctamente
        if (StringUtils.isNotBlank(producto.getNombre())) {
            producto.setNombre(StringUtils.normalizeSpace(producto.getNombre()));
        }
        invalidarCache();
        Producto guardado = productoRepository.save(producto);
        logger.info("Producto guardado: id={}, nombre={}", guardado.getId(), guardado.getNombre());
        return guardado;
    }

    @Override
    @Transactional
    public Producto actualizar(Long id, Producto datos) {
        Producto existente = productoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        existente.setNombre(StringUtils.normalizeSpace(datos.getNombre()));
        existente.setDescripcion(datos.getDescripcion());
        existente.setPrecio(datos.getPrecio());
        existente.setStock(datos.getStock());
        existente.setCategoria(datos.getCategoria());
        existente.setProveedor(datos.getProveedor());
        existente.setCodigoBarras(datos.getCodigoBarras());

        invalidarCache();
        logger.info("Producto actualizado: id={}", id);
        return productoRepository.save(existente);
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
        producto.setActivo(false);
        productoRepository.save(producto);
        invalidarCache();
        logger.info("Producto desactivado: id={}", id);
    }

    @Override
    @Transactional
    public boolean reducirStock(Long productoId, int cantidad) {
        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productoId));

        if (producto.getStock() < cantidad) {
            logger.warn("Stock insuficiente para producto id={}: stock={}, solicitado={}", productoId, producto.getStock(), cantidad);
            return false;
        }
        producto.setStock(producto.getStock() - cantidad);
        productoRepository.save(producto);
        invalidarCache();
        return true;
    }

    private void invalidarCache() {
        cache.invalidateAll();
    }
}
