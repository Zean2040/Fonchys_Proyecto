package com.fonchys.minimarket.controller;

import com.fonchys.minimarket.dto.VentaResponseDTO;
import com.fonchys.minimarket.model.Venta;
import com.fonchys.minimarket.service.IVentaService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API REST de reportes de ventas. Reutiliza la misma capa de servicio que {@link ReporteController} (MVC).
 * Restringido a ADMIN, igual que la ruta MVC "/reportes/**" en SecurityConfig.
 */
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReporteRestController {

    private final IVentaService ventaService;

    @GetMapping("/ventas")
    public ReporteVentasDTO ventas(@RequestParam(required = false) String fechaInicio,
                                    @RequestParam(required = false) String fechaFin) {
        List<Venta> ventas;
        if (fechaInicio != null && fechaFin != null && !fechaInicio.isBlank() && !fechaFin.isBlank()) {
            ventas = ventaService.listarPorRangoFecha(fechaInicio, fechaFin);
        } else {
            ventas = ventaService.listarTodas();
        }

        BigDecimal totalIngresos = ventas.stream()
            .filter(v -> v.getEstado() == Venta.Estado.COMPLETADA)
            .map(Venta::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        long ventasCompletadas = ventas.stream().filter(v -> v.getEstado() == Venta.Estado.COMPLETADA).count();
        long ventasAnuladas = ventas.stream().filter(v -> v.getEstado() == Venta.Estado.ANULADA).count();

        BigDecimal ticketPromedio = ventasCompletadas > 0
            ? totalIngresos.divide(BigDecimal.valueOf(ventasCompletadas), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        ReporteVentasDTO dto = new ReporteVentasDTO();
        dto.setVentas(ventas.stream().map(VentaResponseDTO::desde).collect(Collectors.toList()));
        dto.setTotalIngresos(totalIngresos);
        dto.setVentasCompletadas(ventasCompletadas);
        dto.setVentasAnuladas(ventasAnuladas);
        dto.setTicketPromedio(ticketPromedio);
        return dto;
    }

    @Data
    public static class ReporteVentasDTO {
        private List<VentaResponseDTO> ventas;
        private BigDecimal totalIngresos;
        private long ventasCompletadas;
        private long ventasAnuladas;
        private BigDecimal ticketPromedio;
    }
}
