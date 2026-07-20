package com.fonchys.minimarket.controller;

import com.fonchys.minimarket.dto.VentaRequestDTO;
import com.fonchys.minimarket.model.Venta;
import com.fonchys.minimarket.repository.CategoriaRepository;
import com.fonchys.minimarket.service.IProductoService;
import com.fonchys.minimarket.service.IVentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final IVentaService ventaService;
    private final IProductoService productoService;
    private final CategoriaRepository categoriaRepository;

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("ventas", ventaService.listarVentasHoy());
        model.addAttribute("totalHoy", String.format("%.2f", ventaService.totalVentasHoy()));
        return "ventas/lista";
    }

    @GetMapping("/nueva")
    public String formNueva(Model model) {
        model.addAttribute("productos", productoService.listarActivos());
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "ventas/nueva";
    }

    @PostMapping("/registrar")
    public String registrar(@ModelAttribute VentaRequestDTO dto,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes flash) {
        try {
            if (dto.getItems() == null || dto.getItems().isEmpty()) {
                flash.addFlashAttribute("error", "El carrito está vacío. Agrega al menos un producto.");
                return "redirect:/ventas/nueva";
            }
            Venta venta = ventaService.registrarVenta(dto, userDetails.getUsername());
            return "redirect:/ventas/comprobante/" + venta.getId();
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/ventas/nueva";
        }
    }

    @GetMapping("/comprobante/{id}")
    public String comprobante(@PathVariable Long id, Model model) {
        Venta venta = ventaService.buscarPorId(id);
        model.addAttribute("venta", venta);
        return "ventas/comprobante";
    }

    @PostMapping("/anular/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String anular(@PathVariable Long id, RedirectAttributes flash) {
        try {
            ventaService.anularVenta(id);
            flash.addFlashAttribute("exito", "Venta #" + id + " anulada correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ventas";
    }

    @GetMapping("/historial")
    public String historial(@RequestParam(required = false) String fechaInicio,
                            @RequestParam(required = false) String fechaFin,
                            Model model) {
        if (fechaInicio != null && fechaFin != null && !fechaInicio.isBlank() && !fechaFin.isBlank()) {
            model.addAttribute("ventas", ventaService.listarPorRangoFecha(fechaInicio, fechaFin));
            model.addAttribute("fechaInicio", fechaInicio);
            model.addAttribute("fechaFin", fechaFin);
        } else {
            model.addAttribute("ventas", ventaService.listarTodas());
        }
        return "ventas/historial";
    }
}
