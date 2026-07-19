package com.fonchys.minimarket.repository;

import com.fonchys.minimarket.model.Marcacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MarcacionRepository extends JpaRepository<Marcacion, Long> {

    @Query("SELECT m FROM Marcacion m ORDER BY m.fechaHora DESC")
    List<Marcacion> findAllOrderByFechaHoraDesc();

    @Query("SELECT m FROM Marcacion m WHERE m.fechaHora BETWEEN :inicio AND :fin ORDER BY m.fechaHora DESC")
    List<Marcacion> findByRangoFecha(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT m FROM Marcacion m WHERE m.empleado.id = :empleadoId ORDER BY m.fechaHora DESC")
    List<Marcacion> findByEmpleadoId(@Param("empleadoId") Long empleadoId);

    @Query("SELECT m FROM Marcacion m WHERE m.empleado.id = :empleadoId AND m.fechaHora BETWEEN :inicio AND :fin ORDER BY m.fechaHora DESC")
    List<Marcacion> findByEmpleadoIdAndRangoFecha(@Param("empleadoId") Long empleadoId,
                                                   @Param("inicio") LocalDateTime inicio,
                                                   @Param("fin") LocalDateTime fin);
}
