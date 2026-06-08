package com.fonchys.minimarket.controller;

import com.fonchys.minimarket.model.Venta;
import com.fonchys.minimarket.service.IVentaService;
import com.fonchys.minimarket.util.ExcelExporter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReporteController {

    private final IVentaService ventaService;

    @GetMapping
    public String index(@RequestParam(required = false) String fechaInicio,
                        @RequestParam(required = false) String fechaFin,
                        Model model) {

        List<Venta> ventas;
        if (fechaInicio != null && fechaFin != null && !fechaInicio.isBlank() && !fechaFin.isBlank()) {
            ventas = ventaService.listarPorRangoFecha(fechaInicio, fechaFin);
            model.addAttribute("fechaInicio", fechaInicio);
            model.addAttribute("fechaFin", fechaFin);
        } else {
            ventas = ventaService.listarVentasHoy();
            model.addAttribute("fechaInicio", LocalDate.now().toString());
            model.addAttribute("fechaFin", LocalDate.now().toString());
        }

        BigDecimal totalIngresos = ventas.stream()
            .filter(v -> "COMPLETADA".equals(v.getEstado().name()))
            .map(Venta::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        long ventasCompletadas = ventas.stream()
            .filter(v -> "COMPLETADA".equals(v.getEstado().name()))
            .count();

        long ventasAnuladas = ventas.stream()
            .filter(v -> "ANULADA".equals(v.getEstado().name()))
            .count();

        BigDecimal ticketPromedio = ventasCompletadas > 0
            ? totalIngresos.divide(BigDecimal.valueOf(ventasCompletadas), 2, java.math.RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        model.addAttribute("ventas", ventas);
        model.addAttribute("totalIngresos", totalIngresos);
        model.addAttribute("ventasCompletadas", ventasCompletadas);
        model.addAttribute("ventasAnuladas", ventasAnuladas);
        model.addAttribute("ticketPromedio", ticketPromedio);

        return "reportes/index";
    }

    @GetMapping("/ventas/excel")
    @ResponseBody
    public ResponseEntity<byte[]> exportarVentasExcel(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin) {

        List<Venta> ventas;
        if (fechaInicio != null && fechaFin != null && !fechaInicio.isBlank() && !fechaFin.isBlank()) {
            ventas = ventaService.listarPorRangoFecha(fechaInicio, fechaFin);
        } else {
            ventas = ventaService.listarVentasHoy();
        }

        ByteArrayOutputStream out = ExcelExporter.exportarVentas(ventas);
        String filename = "ventas_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(out.toByteArray());
    }
}
