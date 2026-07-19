package com.fonchys.minimarket.service;

import com.fonchys.minimarket.model.Empleado;
import com.fonchys.minimarket.model.Usuario;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IUsuarioService extends UserDetailsService {

    List<Usuario> listarTodos();

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorEmail(String email);

    Usuario guardar(Usuario usuario, boolean encriptarPassword);

    Usuario actualizar(Long id, String nombre, String email, Usuario.Rol rol,
                       Empleado empleado, String nuevaPassword);

    void desactivar(Long id);

    Set<Long> empleadoIdsConCuenta();

    boolean existeEmailEnOtroUsuario(String email, Long idActual);
}
