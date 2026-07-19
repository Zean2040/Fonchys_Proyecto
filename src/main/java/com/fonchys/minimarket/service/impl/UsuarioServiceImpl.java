package com.fonchys.minimarket.service.impl;

import com.fonchys.minimarket.model.Empleado;
import com.fonchys.minimarket.model.Usuario;
import com.fonchys.minimarket.repository.UsuarioRepository;
import com.fonchys.minimarket.service.IUsuarioService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements IUsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmailAndActivoTrue(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        return User.builder()
            .username(usuario.getEmail())
            .password(usuario.getPassword())
            .authorities(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()))
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmailAndActivoTrue(email);
    }

    @Override
    @Transactional
    public Usuario guardar(Usuario usuario, boolean encriptarPassword) {
        if (encriptarPassword) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        Usuario guardado = usuarioRepository.save(usuario);
        logger.info("Usuario guardado: id={}, email={}", guardado.getId(), guardado.getEmail());
        return guardado;
    }

    @Override
    @Transactional
    public Usuario actualizar(Long id, String nombre, String email, Usuario.Rol rol,
                              Empleado empleado, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setRol(rol);
        usuario.setEmpleado(empleado);
        if (nuevaPassword != null && !nuevaPassword.isBlank()) {
            usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        }
        Usuario actualizado = usuarioRepository.save(usuario);
        logger.info("Usuario actualizado: id={}, email={}", actualizado.getId(), actualizado.getEmail());
        return actualizado;
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
        logger.info("Usuario desactivado: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> empleadoIdsConCuenta() {
        return new HashSet<>(usuarioRepository.findAllEmpleadoIds());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeEmailEnOtroUsuario(String email, Long idActual) {
        return usuarioRepository.existsByEmailAndIdNot(email, idActual);
    }
}
