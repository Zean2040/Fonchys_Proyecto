package com.fonchys.minimarket.service;

import com.fonchys.minimarket.model.Usuario;
import com.fonchys.minimarket.repository.UsuarioRepository;
import com.fonchys.minimarket.service.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias — UsuarioService
 * Verifica encriptación de contraseñas, búsquedas y desactivación de cuentas.
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
class UsuarioServiceTest {

    private static final String SEP  = "═".repeat(60);
    private static final String LINE = "─".repeat(60);

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @BeforeAll
    static void inicio() {
        System.out.println("\n" + SEP);
        System.out.println("  PRUEBAS UNITARIAS — UsuarioService");
        System.out.println("  Capa: Servicio | Framework: JUnit 5 + Mockito");
        System.out.println(SEP);
    }

    @AfterAll
    static void fin() {
        System.out.println("\n" + SEP);
        System.out.println("  FIN — UsuarioService");
        System.out.println(SEP + "\n");
    }

    @BeforeEach
    void antes(TestInfo info) {
        System.out.println("\n" + LINE);
        System.out.println("  EJECUTANDO: " + info.getDisplayName());
        System.out.println(LINE);
    }

    @AfterEach
    void despues(TestInfo info) {
        System.out.println("  ✔  PASÓ: " + info.getDisplayName());
    }

    private Usuario usuarioEjemplo(Long id, String email, String password, Usuario.Rol rol) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombre("Nombre Test");
        u.setEmail(email);
        u.setPassword(password);
        u.setRol(rol);
        u.setActivo(true);
        return u;
    }

    @Test
    @DisplayName("[Usuario-01] guardar con encriptación aplica BCrypt a la contraseña")
    void guardar_conEncriptacion_encriptaPasswordConBCrypt() {
        Usuario usuario = usuarioEjemplo(null, "cajero@fonchys.com", "1234", Usuario.Rol.CAJERO);
        String hashBCrypt = "$2a$10$hasheado.con.bcrypt";

        when(passwordEncoder.encode("1234")).thenReturn(hashBCrypt);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        System.out.println("  Contraseña original: '1234'");
        System.out.println("  Se espera: password guardado como hash BCrypt, nunca en texto plano");

        Usuario guardado = usuarioService.guardar(usuario, true);

        assertEquals(hashBCrypt, guardado.getPassword());
        assertNotEquals("1234", guardado.getPassword());
        verify(passwordEncoder).encode("1234");
        System.out.println("  Password guardado: " + guardado.getPassword());
        System.out.println("  Contraseña original '1234' nunca fue almacenada ✔");
    }

    @Test
    @DisplayName("[Usuario-02] guardar sin encriptación guarda la contraseña tal como viene")
    void guardar_sinEncriptacion_guardaPasswordSinCambios() {
        Usuario usuario = usuarioEjemplo(null, "sistema@fonchys.com", "hash-ya-existente", Usuario.Rol.ADMIN);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        System.out.println("  Caso de uso: usuario con password ya hasheado (importación/migración)");
        System.out.println("  Se espera: password no se vuelve a encriptar");

        Usuario guardado = usuarioService.guardar(usuario, false);

        assertEquals("hash-ya-existente", guardado.getPassword());
        verify(passwordEncoder, never()).encode(any());
        System.out.println("  Password guardado sin cambios: " + guardado.getPassword() + " ✔");
    }

    @Test
    @DisplayName("[Usuario-03] buscarPorEmail con usuario activo devuelve el usuario")
    void buscarPorEmail_usuarioActivoExiste_devuelveUsuario() {
        Usuario usuario = usuarioEjemplo(1L, "admin@fonchys.com", "hash", Usuario.Rol.ADMIN);
        when(usuarioRepository.findByEmailAndActivoTrue("admin@fonchys.com"))
            .thenReturn(Optional.of(usuario));
        System.out.println("  Mock: admin@fonchys.com existe y está activo");

        Optional<Usuario> resultado = usuarioService.buscarPorEmail("admin@fonchys.com");

        assertTrue(resultado.isPresent());
        assertEquals("admin@fonchys.com", resultado.get().getEmail());
        assertEquals(Usuario.Rol.ADMIN, resultado.get().getRol());
        System.out.println("  Encontrado: " + resultado.get().getEmail() + " | Rol: " + resultado.get().getRol());
    }

    @Test
    @DisplayName("[Usuario-04] buscarPorEmail con usuario inexistente devuelve Optional vacío")
    void buscarPorEmail_usuarioNoExiste_devuelveVacio() {
        when(usuarioRepository.findByEmailAndActivoTrue("noexiste@fonchys.com"))
            .thenReturn(Optional.empty());
        System.out.println("  Mock: noexiste@fonchys.com NO está registrado");

        Optional<Usuario> resultado = usuarioService.buscarPorEmail("noexiste@fonchys.com");

        assertFalse(resultado.isPresent());
        System.out.println("  Resultado: Optional.empty() — usuario no encontrado ✔");
    }

    @Test
    @DisplayName("[Usuario-05] actualizar sin nueva contraseña mantiene el hash anterior")
    void actualizar_sinNuevaPassword_mantienHashAnterior() {
        String hashOriginal = "$2a$10$hashoriginal";
        Usuario usuario = usuarioEjemplo(2L, "cajero@fonchys.com", hashOriginal, Usuario.Rol.CAJERO);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        System.out.println("  Password actual en BD: " + hashOriginal);
        System.out.println("  Nueva contraseña enviada: '' (vacía) — no debe cambiar");

        usuarioService.actualizar(2L, "Cajero Actualizado", "cajero@fonchys.com",
            Usuario.Rol.CAJERO, null, "");

        assertEquals(hashOriginal, usuario.getPassword());
        verify(passwordEncoder, never()).encode(any());
        System.out.println("  Hash en BD sin cambios: " + usuario.getPassword() + " ✔");
    }

    @Test
    @DisplayName("[Usuario-06] desactivar pone activo=false sin eliminar el registro de la BD")
    void desactivar_cambiaActivoAFalseSinEliminar() {
        Usuario usuario = usuarioEjemplo(3L, "almacenero@fonchys.com", "hash", Usuario.Rol.ALMACENERO);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        System.out.println("  Usuario activo: " + usuario.getActivo());
        System.out.println("  Se espera: activo=false, registro conservado en BD (no se borra)");

        usuarioService.desactivar(3L);

        assertFalse(usuario.getActivo());
        verify(usuarioRepository, never()).delete(any());
        verify(usuarioRepository).save(usuario);
        System.out.println("  activo=" + usuario.getActivo() + " | delete() nunca fue llamado ✔");
    }
}
