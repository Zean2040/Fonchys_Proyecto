package com.fonchys.minimarket.controller;

import com.fonchys.minimarket.model.Proveedor;
import com.fonchys.minimarket.service.IProveedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * API REST de proveedores. Reutiliza la misma capa de servicio que {@link ProveedorController} (MVC).
 * Restringido a ADMIN, igual que la ruta MVC "/proveedores/**" en SecurityConfig.
 */
@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProveedorRestController {

    private final IProveedorService proveedorService;

    @GetMapping
    public List<Proveedor> listar(@RequestParam(required = false) String buscar) {
        return (buscar != null && !buscar.isBlank())
            ? proveedorService.buscarPorNombre(buscar)
            : proveedorService.listarActivos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proveedor> obtener(@PathVariable Long id) {
        Proveedor proveedor = proveedorService.buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + id));
        return ResponseEntity.ok(proveedor);
    }

    @PostMapping
    public ResponseEntity<Proveedor> crear(@Valid @RequestBody Proveedor proveedor) {
        proveedor.setId(null);
        Proveedor guardado = proveedorService.guardar(proveedor);
        return ResponseEntity.created(URI.create("/api/proveedores/" + guardado.getId())).body(guardado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Proveedor> actualizar(@PathVariable Long id, @Valid @RequestBody Proveedor datos) {
        Proveedor actualizado = proveedorService.actualizar(id, datos);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        proveedorService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
