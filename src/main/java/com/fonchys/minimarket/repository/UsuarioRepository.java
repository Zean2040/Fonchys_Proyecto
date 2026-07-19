package com.fonchys.minimarket.repository;

import com.fonchys.minimarket.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailAndActivoTrue(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<Usuario> findByEmpleadoId(Long empleadoId);

    @org.springframework.data.jpa.repository.Query("SELECT u.empleado.id FROM Usuario u WHERE u.empleado IS NOT NULL")
    java.util.List<Long> findAllEmpleadoIds();
}
