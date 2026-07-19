package com.fonchys.minimarket.service;

import com.fonchys.minimarket.model.Empleado;
import com.fonchys.minimarket.model.Marcacion;
import com.fonchys.minimarket.model.TipoMarcacion;

import java.util.List;
import java.util.Optional;

public interface IEmpleadoService {
    List<Empleado> listarActivos();
    List<Empleado> buscarPorNombre(String texto);
    Optional<Empleado> buscarPorId(Long id);
    Empleado guardar(Empleado empleado);
    Empleado actualizar(Long id, Empleado datos);
    void desactivar(Long id);
    Marcacion registrarMarcacion(Long empleadoId, TipoMarcacion tipo, String observacion);
    List<Marcacion> listarMarcaciones(Long empleadoId, String fechaInicio, String fechaFin);
    List<Marcacion> listarTodasMarcaciones(String fechaInicio, String fechaFin);
}
