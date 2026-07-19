package com.fonchys.minimarket.controller;

import com.fonchys.minimarket.model.Empleado;
import com.fonchys.minimarket.model.Marcacion;
import com.fonchys.minimarket.model.TipoMarcacion;
import com.fonchys.minimarket.service.IEmpleadoService;
import com.fonchys.minimarket.service.IUsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

    private final IEmpleadoService empleadoService;
    private final IUsuarioService usuarioService;

    @GetMapping
    public String listar(@RequestParam(required = false) String buscar, Model model) {
        List<Empleado> empleados = (buscar != null && !buscar.isBlank())
            ? empleadoService.buscarPorNombre(buscar)
            : empleadoService.listarActivos();
        model.addAttribute("empleados", empleados);
        model.addAttribute("buscar", buscar != null ? buscar : "");
        model.addAttribute("empleadosConCuenta", usuarioService.empleadoIdsConCuenta());
        return "empleados/lista";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuevo(Model model) {
        model.addAttribute("empleado", new Empleado());
        model.addAttribute("titulo", "Nuevo Empleado");
        return "empleados/form";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        return empleadoService.buscarPorId(id).map(e -> {
            model.addAttribute("empleado", e);
            model.addAttribute("titulo", "Editar Empleado");
            boolean tieneCuenta = usuarioService.empleadoIdsConCuenta().contains(e.getId());
            model.addAttribute("tieneCuenta", tieneCuenta);
            return "empleados/form";
        }).orElseGet(() -> {
            flash.addFlashAttribute("error", "Empleado no encontrado");
            return "redirect:/empleados";
        });
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardar(@Valid @ModelAttribute Empleado empleado,
                          BindingResult result,
                          Model model,
                          RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("titulo", empleado.getId() == null ? "Nuevo Empleado" : "Editar Empleado");
            return "empleados/form";
        }
        try {
            if (empleado.getId() == null) {
                empleadoService.guardar(empleado);
                flash.addFlashAttribute("exito", "Empleado registrado correctamente.");
            } else {
                empleadoService.actualizar(empleado.getId(), empleado);
                flash.addFlashAttribute("exito", "Empleado actualizado correctamente.");
            }
        } catch (DataIntegrityViolationException e) {
            flash.addFlashAttribute("error",
                "El email ya está registrado en otro empleado. Usa un email diferente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
        }
        return "redirect:/empleados";
    }

    @PostMapping("/desactivar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String desactivar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            empleadoService.desactivar(id);
            flash.addFlashAttribute("exito", "Empleado desactivado correctamente");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/empleados";
    }

    @PostMapping("/marcar")
    @PreAuthorize("hasRole('ADMIN')")
    public String marcar(@RequestParam Long empleadoId,
                         @RequestParam String tipo,
                         @RequestParam(required = false) String observacion,
                         RedirectAttributes flash) {
        try {
            TipoMarcacion tipoMarcacion = TipoMarcacion.valueOf(tipo);
            Marcacion m = empleadoService.registrarMarcacion(empleadoId, tipoMarcacion, observacion);
            flash.addFlashAttribute("exito",
                "Marcación registrada: " + m.getEmpleado().getNombreCompleto() + " — " + tipo);
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al registrar marcación: " + e.getMessage());
        }
        return "redirect:/empleados";
    }

    @GetMapping("/historial")
    public String historial(@RequestParam(required = false) Long empleadoId,
                            @RequestParam(required = false) String fechaInicio,
                            @RequestParam(required = false) String fechaFin,
                            Model model) {
        List<Marcacion> marcaciones;
        if (empleadoId != null) {
            marcaciones = empleadoService.listarMarcaciones(empleadoId, fechaInicio, fechaFin);
            empleadoService.buscarPorId(empleadoId)
                .ifPresent(e -> model.addAttribute("empleadoSeleccionado", e));
        } else {
            marcaciones = empleadoService.listarTodasMarcaciones(fechaInicio, fechaFin);
        }
        model.addAttribute("marcaciones", marcaciones);
        model.addAttribute("empleados", empleadoService.listarActivos());
        model.addAttribute("empleadoId", empleadoId);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        return "empleados/historial";
    }
}
