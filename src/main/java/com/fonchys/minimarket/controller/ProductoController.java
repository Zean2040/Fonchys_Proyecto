package com.fonchys.minimarket.controller;

import com.fonchys.minimarket.model.Categoria;
import com.fonchys.minimarket.model.Producto;
import com.fonchys.minimarket.model.Proveedor;
import com.fonchys.minimarket.model.UnidadMedida;
import com.fonchys.minimarket.repository.CategoriaRepository;
import com.fonchys.minimarket.repository.ProveedorRepository;
import com.fonchys.minimarket.service.IProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final IProductoService productoService;
    private final CategoriaRepository categoriaRepository;
    private final ProveedorRepository proveedorRepository;

    @GetMapping
    public String lista(@RequestParam(required = false) String buscar, Model model) {
        List<Producto> productos = (buscar != null && !buscar.isBlank())
            ? productoService.buscarPorNombre(buscar)
            : productoService.listarActivos();
        model.addAttribute("productos", productos);
        model.addAttribute("buscar", buscar != null ? buscar : "");
        return "productos/lista";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    public String formNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("proveedores", proveedorRepository.findAll());
        model.addAttribute("unidades", UnidadMedida.values());
        return "productos/form";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    public String formEditar(@PathVariable Long id, Model model) {
        Producto producto = productoService.buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        model.addAttribute("producto", producto);
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("proveedores", proveedorRepository.findAll());
        model.addAttribute("unidades", UnidadMedida.values());
        return "productos/form";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    public String guardar(@Valid @ModelAttribute Producto producto,
                          BindingResult result,
                          @RequestParam(required = false) Long categoriaId,
                          @RequestParam(required = false) Long proveedorId,
                          Model model,
                          RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaRepository.findAll());
            model.addAttribute("proveedores", proveedorRepository.findAll());
            model.addAttribute("unidades", UnidadMedida.values());
            return "productos/form";
        }
        // String vacío → null para campos con unique constraint
        if (producto.getCodigoBarras() != null && producto.getCodigoBarras().isBlank()) {
            producto.setCodigoBarras(null);
        }

        if (categoriaId != null) {
            Categoria cat = categoriaRepository.findById(categoriaId).orElse(null);
            producto.setCategoria(cat);
        }
        if (proveedorId != null) {
            Proveedor prov = proveedorRepository.findById(proveedorId).orElse(null);
            producto.setProveedor(prov);
        }
        if (producto.getId() != null) {
            productoService.actualizar(producto.getId(), producto);
            flash.addFlashAttribute("exito", "Producto actualizado correctamente");
        } else {
            productoService.guardar(producto);
            flash.addFlashAttribute("exito", "Producto creado correctamente");
        }
        return "redirect:/productos";
    }

    @PostMapping("/desactivar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    public String desactivar(@PathVariable Long id, RedirectAttributes flash) {
        productoService.desactivar(id);
        flash.addFlashAttribute("exito", "Producto desactivado correctamente");
        return "redirect:/productos";
    }
}
