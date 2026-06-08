package com.fonchys.minimarket.service;

import com.fonchys.minimarket.model.Usuario;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface IUsuarioService extends UserDetailsService {

    List<Usuario> listarTodos();

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorEmail(String email);

    Usuario guardar(Usuario usuario, boolean encriptarPassword);

    void desactivar(Long id);
}
