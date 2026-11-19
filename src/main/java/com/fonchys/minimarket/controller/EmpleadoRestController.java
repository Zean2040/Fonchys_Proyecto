package com.fonchys.minimarket.controller;

import com.fonchys.minimarket.model.Empleado;
import com.fonchys.minimarket.service.IEmpleadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * API REST de empleados. Reutiliza la misma capa de servicio que {@link EmpleadoController} (MVC).
 * Restringido a ADMIN, igual que la ruta MVC "/empleados/**" en SecurityConfig.
 */
@RestController
@RequestMapping("/api/empleados")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EmpleadoRestController {

    private final IEmpleadoService empleadoService;

    @GetMapping
    public List<Empleado> listar(@RequestParam(required = false) String buscar) {
        return (buscar != null && !buscar.isBlank())
            ? empleadoService.buscarPorNombre(buscar)
            : empleadoService.listarActivos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empleado> obtener(@PathVariable Long id) {
        return empleadoService.buscarPorId(id)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + id));
    }

    @PostMapping
    public ResponseEntity<Empleado> crear(@Valid @RequestBody Empleado empleado) {
        empleado.setId(null);
        Empleado guardado = empleadoService.guardar(empleado);
        return ResponseEntity.created(URI.create("/api/empleados/" + guardado.getId())).body(guardado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Empleado> actualizar(@PathVariable Long id, @Valid @RequestBody Empleado datos) {
        Empleado actualizado = empleadoService.actualizar(id, datos);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        empleadoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
