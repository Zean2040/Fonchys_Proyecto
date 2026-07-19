package com.fonchys.minimarket.repository;

import com.fonchys.minimarket.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByActivoTrue();

    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    List<Producto> findByCategoriaIdAndActivoTrue(Long categoriaId);

    @Query("SELECT p FROM Producto p WHERE p.stock <= :umbral AND p.activo = true ORDER BY p.stock ASC")
    List<Producto> findByStockBajoUmbral(@Param("umbral") int umbral);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.stock <= p.stockMinimo ORDER BY p.stock ASC")
    List<Producto> findProductosStockBajoUmbralPropio();

    @Query("SELECT p FROM Producto p WHERE p.proveedor.id = :proveedorId AND p.stock <= :umbral AND p.activo = true ORDER BY p.stock ASC")
    List<Producto> findByProveedorIdAndStockBajo(@Param("proveedorId") Long proveedorId, @Param("umbral") int umbral);

    boolean existsByCodigoBarras(String codigoBarras);
}
