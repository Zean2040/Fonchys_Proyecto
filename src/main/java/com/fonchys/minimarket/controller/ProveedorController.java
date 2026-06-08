package com.fonchys.minimarket.controller;

import com.fonchys.minimarket.model.Producto;
import com.fonchys.minimarket.model.Proveedor;
import com.fonchys.minimarket.service.IProductoService;
import com.fonchys.minimarket.service.IProveedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private static final int UMBRAL_STOCK = 5;

    private final IProveedorService proveedorService;
    private final IProductoService productoService;

    @GetMapping
    public String listar(@RequestParam(required = false) String buscar, Model model) {
        List<Proveedor> proveedores;
        if (buscar != null && !buscar.isBlank()) {
            proveedores = proveedorService.buscarPorNombre(buscar);
            model.addAttribute("buscar", buscar);
        } else {
            proveedores = proveedorService.listarActivos();
        }

        Map<Long, List<Producto>> stockBajoPorProveedor = new HashMap<>();
        for (Proveedor p : proveedores) {
            List<Producto> bajos = productoService.listarStockBajoPorProveedor(p.getId(), UMBRAL_STOCK);
            if (!bajos.isEmpty()) {
                stockBajoPorProveedor.put(p.getId(), bajos);
            }
        }

        model.addAttribute("proveedores", proveedores);
        model.addAttribute("stockBajoPorProveedor", stockBajoPorProveedor);
        return "proveedores/lista";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuevo(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        model.addAttribute("titulo", "Nuevo Proveedor");
        return "proveedores/form";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        return proveedorService.buscarPorId(id).map(p -> {
            model.addAttribute("proveedor", p);
            model.addAttribute("titulo", "Editar Proveedor");
            return "proveedores/form";
        }).orElseGet(() -> {
            flash.addFlashAttribute("error", "Proveedor no encontrado");
            return "redirect:/proveedores";
        });
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardar(@Valid @ModelAttribute Proveedor proveedor,
                          BindingResult result,
                          Model model,
                          RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("titulo", proveedor.getId() == null ? "Nuevo Proveedor" : "Editar Proveedor");
            return "proveedores/form";
        }
        try {
            if (proveedor.getId() == null) {
                proveedorService.guardar(proveedor);
                flash.addFlashAttribute("success", "Proveedor creado correctamente");
            } else {
                proveedorService.actualizar(proveedor.getId(), proveedor);
                flash.addFlashAttribute("success", "Proveedor actualizado correctamente");
            }
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
        }
        return "redirect:/proveedores";
    }

    @PostMapping("/desactivar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String desactivar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            proveedorService.desactivar(id);
            flash.addFlashAttribute("success", "Proveedor desactivado correctamente");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/proveedores";
    }

    @GetMapping("/whatsapp/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String whatsapp(@PathVariable Long id, RedirectAttributes flash) {
        Proveedor proveedor = proveedorService.buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        if (proveedor.getTelefono() == null || proveedor.getTelefono().isBlank()) {
            flash.addFlashAttribute("error", "El proveedor no tiene número de teléfono registrado");
            return "redirect:/proveedores";
        }

        List<Producto> bajos = productoService.listarStockBajoPorProveedor(id, UMBRAL_STOCK);
        if (bajos.isEmpty()) {
            flash.addFlashAttribute("error", "No hay productos con stock bajo para este proveedor");
            return "redirect:/proveedores";
        }

        StringBuilder msg = new StringBuilder();
        msg.append("Hola ").append(proveedor.getNombre())
           .append(", le contactamos de Fonchys Minimarket para solicitar reposición de los siguientes productos:\n\n");
        for (Producto p : bajos) {
            msg.append("• ").append(p.getNombre())
               .append(" — stock actual: ").append(p.getStock()).append("\n");
        }
        msg.append("\nQuedamos a la espera de su respuesta. Gracias.");

        String telefono = proveedor.getTelefono().replaceAll("[^0-9]", "");
        String url = "https://wa.me/" + telefono
            + "?text=" + URLEncoder.encode(msg.toString(), StandardCharsets.UTF_8);

        return "redirect:" + url;
    }
}
