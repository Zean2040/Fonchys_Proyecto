package com.fonchys.minimarket.controller;

import com.fonchys.minimarket.service.IProductoService;
import com.fonchys.minimarket.service.IVentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final IProductoService productoService;
    private final IVentaService ventaService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("totalVentasHoy", ventaService.totalVentasHoy());
        model.addAttribute("cantVentasHoy", ventaService.listarVentasHoy().size());
        model.addAttribute("productosStockBajo", productoService.listarStockBajoIndividual());
        model.addAttribute("totalProductos", productoService.listarActivos().size());
        return "dashboard/index";
    }
}
