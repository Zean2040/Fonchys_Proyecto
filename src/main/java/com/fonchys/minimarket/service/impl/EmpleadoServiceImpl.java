package com.fonchys.minimarket.service.impl;

import com.fonchys.minimarket.model.Empleado;
import com.fonchys.minimarket.model.Marcacion;
import com.fonchys.minimarket.model.TipoMarcacion;
import com.fonchys.minimarket.repository.EmpleadoRepository;
import com.fonchys.minimarket.repository.MarcacionRepository;
import com.fonchys.minimarket.service.IEmpleadoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmpleadoServiceImpl implements IEmpleadoService {

    private static final Logger logger = LoggerFactory.getLogger(EmpleadoServiceImpl.class);
    private static final DateTimeFormatter FECHA_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final EmpleadoRepository empleadoRepository;
    private final MarcacionRepository marcacionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Empleado> listarActivos() {
        return empleadoRepository.findByActivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Empleado> buscarPorNombre(String texto) {
        return empleadoRepository
            .findByNombreContainingIgnoreCaseAndActivoTrueOrApellidoContainingIgnoreCaseAndActivoTrue(texto, texto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Empleado> buscarPorId(Long id) {
        return empleadoRepository.findById(id);
    }

    @Override
    @Transactional
    public Empleado guardar(Empleado empleado) {
        Empleado guardado = empleadoRepository.save(empleado);
        logger.info("Empleado creado: id={}, nombre={}", guardado.getId(), guardado.getNombreCompleto());
        return guardado;
    }

    @Override
    @Transactional
    public Empleado actualizar(Long id, Empleado datos) {
        Empleado empleado = empleadoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Empleado no encontrado: " + id));
        empleado.setNombre(datos.getNombre());
        empleado.setApellido(datos.getApellido());
        empleado.setCargo(datos.getCargo());
        empleado.setTelefono(datos.getTelefono());
        empleado.setEmail(datos.getEmail());
        Empleado actualizado = empleadoRepository.save(empleado);
        logger.info("Empleado actualizado: id={}", actualizado.getId());
        return actualizado;
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        Empleado empleado = empleadoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Empleado no encontrado: " + id));
        empleado.setActivo(false);
        empleadoRepository.save(empleado);
        logger.info("Empleado desactivado: id={}", id);
    }

    @Override
    @Transactional
    public Marcacion registrarMarcacion(Long empleadoId, TipoMarcacion tipo, String observacion) {
        Empleado empleado = empleadoRepository.findById(empleadoId)
            .orElseThrow(() -> new RuntimeException("Empleado no encontrado: " + empleadoId));
        Marcacion marcacion = new Marcacion();
        marcacion.setEmpleado(empleado);
        marcacion.setTipo(tipo);
        marcacion.setFechaHora(LocalDateTime.now());
        if (observacion != null && !observacion.isBlank()) {
            marcacion.setObservacion(observacion.trim());
        }
        Marcacion guardada = marcacionRepository.save(marcacion);
        logger.info("Marcación registrada: empleado={}, tipo={}", empleado.getNombreCompleto(), tipo);
        return guardada;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Marcacion> listarMarcaciones(Long empleadoId, String fechaInicio, String fechaFin) {
        if (fechaInicio != null && !fechaInicio.isBlank() && fechaFin != null && !fechaFin.isBlank()) {
            LocalDateTime inicio = LocalDate.parse(fechaInicio, FECHA_FORMAT).atStartOfDay();
            LocalDateTime fin = LocalDate.parse(fechaFin, FECHA_FORMAT).atTime(LocalTime.MAX);
            return marcacionRepository.findByEmpleadoIdAndRangoFecha(empleadoId, inicio, fin);
        }
        return marcacionRepository.findByEmpleadoId(empleadoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Marcacion> listarTodasMarcaciones(String fechaInicio, String fechaFin) {
        if (fechaInicio != null && !fechaInicio.isBlank() && fechaFin != null && !fechaFin.isBlank()) {
            LocalDateTime inicio = LocalDate.parse(fechaInicio, FECHA_FORMAT).atStartOfDay();
            LocalDateTime fin = LocalDate.parse(fechaFin, FECHA_FORMAT).atTime(LocalTime.MAX);
            return marcacionRepository.findByRangoFecha(inicio, fin);
        }
        return marcacionRepository.findAllOrderByFechaHoraDesc();
    }
}
