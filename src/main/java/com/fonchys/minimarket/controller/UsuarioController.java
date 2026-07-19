package com.fonchys.minimarket.controller;

import com.fonchys.minimarket.model.Empleado;
import com.fonchys.minimarket.model.Usuario;
import com.fonchys.minimarket.service.IEmpleadoService;
import com.fonchys.minimarket.service.IUsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/usuarios")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UsuarioController {

    private final IUsuarioService usuarioService;
    private final IEmpleadoService empleadoService;

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String formNuevo(@RequestParam(required = false) Long empleadoId, Model model) {
        Usuario usuario = new Usuario();
        Set<Long> conCuenta = usuarioService.empleadoIdsConCuenta();

        if (empleadoId != null) {
            empleadoService.buscarPorId(empleadoId).ifPresent(emp -> {
                usuario.setNombre(emp.getNombreCompleto());
                if (emp.getEmail() != null && !emp.getEmail().isBlank()) {
                    usuario.setEmail(emp.getEmail());
                }
                usuario.setEmpleado(emp);
            });
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", Usuario.Rol.values());
        model.addAttribute("empleados", empleadosSinCuenta(conCuenta, null));
        return "usuarios/form";
    }

    @GetMapping("/editar/{id}")
    public String formEditar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        return usuarioService.buscarPorId(id).map(usuario -> {
            Set<Long> conCuenta = usuarioService.empleadoIdsConCuenta();
            // Excluir el empleado ya vinculado para que pueda seguir seleccionado
            if (usuario.getEmpleado() != null) {
                conCuenta.remove(usuario.getEmpleado().getId());
            }
            model.addAttribute("usuario", usuario);
            model.addAttribute("roles", Usuario.Rol.values());
            model.addAttribute("empleados", empleadosSinCuenta(conCuenta, null));
            return "usuarios/form";
        }).orElseGet(() -> {
            flash.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/usuarios";
        });
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam(required = false) Long id,
                          @RequestParam String nombre,
                          @RequestParam String email,
                          @RequestParam String rol,
                          @RequestParam(required = false) String password,
                          @RequestParam(required = false) Long empleadoId,
                          RedirectAttributes flash) {
        // Validaciones manuales
        if (nombre == null || nombre.isBlank()) {
            flash.addFlashAttribute("error", "El nombre es obligatorio.");
            return id == null ? "redirect:/usuarios/nuevo" : "redirect:/usuarios/editar/" + id;
        }
        if (email == null || email.isBlank()) {
            flash.addFlashAttribute("error", "El email es obligatorio.");
            return id == null ? "redirect:/usuarios/nuevo" : "redirect:/usuarios/editar/" + id;
        }
        if (id == null && (password == null || password.isBlank())) {
            flash.addFlashAttribute("error", "La contraseña es obligatoria para nuevos usuarios.");
            return "redirect:/usuarios/nuevo";
        }

        // Verificar email duplicado
        boolean emailDuplicado = (id == null)
            ? usuarioService.buscarPorEmail(email).isPresent()
            : usuarioService.existeEmailEnOtroUsuario(email, id);
        if (emailDuplicado) {
            flash.addFlashAttribute("error", "El email «" + email + "» ya está en uso por otro usuario.");
            return id == null ? "redirect:/usuarios/nuevo" : "redirect:/usuarios/editar/" + id;
        }

        try {
            Empleado empleado = (empleadoId != null)
                ? empleadoService.buscarPorId(empleadoId).orElse(null)
                : null;

            if (id == null) {
                Usuario nuevo = new Usuario();
                nuevo.setNombre(nombre.trim());
                nuevo.setEmail(email.trim().toLowerCase());
                nuevo.setPassword(password);
                nuevo.setRol(Usuario.Rol.valueOf(rol));
                nuevo.setEmpleado(empleado);
                usuarioService.guardar(nuevo, true);
                flash.addFlashAttribute("exito", "Usuario «" + email + "» creado correctamente.");
            } else {
                usuarioService.actualizar(id, nombre.trim(), email.trim().toLowerCase(),
                    Usuario.Rol.valueOf(rol), empleado, password);
                flash.addFlashAttribute("exito", "Usuario actualizado correctamente.");
            }
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id, RedirectAttributes flash) {
        usuarioService.desactivar(id);
        flash.addFlashAttribute("exito", "Usuario desactivado correctamente.");
        return "redirect:/usuarios";
    }

    private List<Empleado> empleadosSinCuenta(Set<Long> idsConCuenta, Long excluir) {
        return empleadoService.listarActivos().stream()
            .filter(e -> !idsConCuenta.contains(e.getId()))
            .collect(Collectors.toList());
    }
}
