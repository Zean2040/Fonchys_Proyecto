package com.fonchys.minimarket.repository;

import com.fonchys.minimarket.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailAndActivoTrue(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<Usuario> findByEmpleadoId(Long empleadoId);

    @Query("SELECT u.empleado.id FROM Usuario u WHERE u.empleado IS NOT NULL")
    List<Long> findAllEmpleadoIds();

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.empleado ORDER BY u.nombre")
    List<Usuario> findAllConEmpleado();
}
