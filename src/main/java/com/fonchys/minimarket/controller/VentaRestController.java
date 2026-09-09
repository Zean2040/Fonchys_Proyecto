package com.fonchys.minimarket.controller;

import com.fonchys.minimarket.dto.VentaRequestDTO;
import com.fonchys.minimarket.dto.VentaResponseDTO;
import com.fonchys.minimarket.model.Venta;
import com.fonchys.minimarket.service.IVentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API REST de ventas. Reutiliza la misma capa de servicio que {@link VentaController} (MVC).
 * Restringido a ADMIN/CAJERO, igual que la ruta MVC "/ventas/**" en SecurityConfig.
 */
@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
public class VentaRestController {

    private final IVentaService ventaService;

    @GetMapping
    public List<VentaResponseDTO> listar(@RequestParam(required = false) String fechaInicio,
                                          @RequestParam(required = false) String fechaFin,
                                          @RequestParam(required = false, defaultValue = "false") boolean historial) {
        List<Venta> ventas;
        if (fechaInicio != null && fechaFin != null && !fechaInicio.isBlank() && !fechaFin.isBlank()) {
            ventas = ventaService.listarPorRangoFecha(fechaInicio, fechaFin);
        } else if (historial) {
            ventas = ventaService.listarTodas();
        } else {
            ventas = ventaService.listarVentasHoy();
        }
        return ventas.stream().map(VentaResponseDTO::desde).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> obtener(@PathVariable Long id) {
        Venta venta = ventaService.buscarPorId(id);
        return ResponseEntity.ok(VentaResponseDTO.desde(venta));
    }

    @PostMapping
    public ResponseEntity<VentaResponseDTO> registrar(@Valid @RequestBody VentaRequestDTO dto,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        Venta venta = ventaService.registrarVenta(dto, userDetails.getUsername());
        return ResponseEntity.created(URI.create("/api/ventas/" + venta.getId()))
            .body(VentaResponseDTO.desde(venta));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> anular(@PathVariable Long id) {
        ventaService.anularVenta(id);
        return ResponseEntity.noContent().build();
    }
}
