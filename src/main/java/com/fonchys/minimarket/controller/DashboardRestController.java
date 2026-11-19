package com.fonchys.minimarket.controller;

import com.fonchys.minimarket.model.Producto;
import com.fonchys.minimarket.service.IProductoService;
import com.fonchys.minimarket.service.IVentaService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API REST del dashboard. Reutiliza la misma capa de servicio que {@link DashboardController} (MVC).
 * Restringido a ADMIN, igual que la ruta MVC "/dashboard" en SecurityConfig.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DashboardRestController {

    private final IProductoService productoService;
    private final IVentaService ventaService;

    @GetMapping
    public DashboardResponseDTO resumen() {
        DashboardResponseDTO dto = new DashboardResponseDTO();
        dto.setTotalVentasHoy(ventaService.totalVentasHoy());
        dto.setCantVentasHoy(ventaService.listarVentasHoy().size());
        dto.setTotalProductos(productoService.listarActivos().size());
        dto.setProductosStockBajo(productoService.listarStockBajoIndividual());
        return dto;
    }

    @Data
    public static class DashboardResponseDTO {
        private Double totalVentasHoy;
        private int cantVentasHoy;
        private int totalProductos;
        private List<Producto> productosStockBajo;
    }
}
