package com.fonchys.minimarket.repository;

import com.fonchys.minimarket.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    List<Proveedor> findByActivoTrue();
    List<Proveedor> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);
}
