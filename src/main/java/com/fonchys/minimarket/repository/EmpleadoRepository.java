package com.fonchys.minimarket.repository;

import com.fonchys.minimarket.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    List<Empleado> findByActivoTrue();
    List<Empleado> findByNombreContainingIgnoreCaseAndActivoTrueOrApellidoContainingIgnoreCaseAndActivoTrue(String nombre, String apellido);
}
