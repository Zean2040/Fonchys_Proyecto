package com.fonchys.minimarket;

import com.fonchys.minimarket.model.Categoria;
import com.fonchys.minimarket.model.Producto;
import com.fonchys.minimarket.model.Usuario;
import com.fonchys.minimarket.repository.CategoriaRepository;
import com.fonchys.minimarket.repository.ProductoRepository;
import com.fonchys.minimarket.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        inicializarUsuarios();
        inicializarCategorias();
        inicializarProductosDemostracion();
    }

    private void inicializarUsuarios() {
        if (!usuarioRepository.existsByEmail("admin@fonchys.com")) {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setEmail("admin@fonchys.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRol(Usuario.Rol.ADMIN);
            usuarioRepository.save(admin);
            logger.info("Usuario admin creado -> email: admin@fonchys.com / password: admin123");
        }
        if (!usuarioRepository.existsByEmail("cajero@fonchys.com")) {
            Usuario cajero = new Usuario();
            cajero.setNombre("Cajero Fonchys");
            cajero.setEmail("cajero@fonchys.com");
            cajero.setPassword(passwordEncoder.encode("cajero123"));
            cajero.setRol(Usuario.Rol.CAJERO);
            usuarioRepository.save(cajero);
            logger.info("Usuario cajero creado -> email: cajero@fonchys.com / password: cajero123");
        }
    }

    private void inicializarCategorias() {
        if (categoriaRepository.count() == 0) {
            String[] categorias = {"Bebidas", "Snacks", "Lácteos", "Limpieza", "Abarrotes", "Carnes", "Frutas y Verduras"};
            for (String nombre : categorias) {
                Categoria cat = new Categoria();
                cat.setNombre(nombre);
                categoriaRepository.save(cat);
            }
            logger.info("Categorías iniciales creadas: {}", categorias.length);
        }
    }

    private void inicializarProductosDemostracion() {
        if (productoRepository.count() == 0) {
            Categoria bebidas = categoriaRepository.findByNombre("Bebidas").orElse(null);
            Categoria snacks = categoriaRepository.findByNombre("Snacks").orElse(null);

            crearProducto("Coca Cola 500ml", new BigDecimal("2.50"), 100, bebidas);
            crearProducto("Inca Kola 500ml", new BigDecimal("2.50"), 80, bebidas);
            crearProducto("Agua San Luis 625ml", new BigDecimal("1.50"), 150, bebidas);
            crearProducto("Chifles 50g", new BigDecimal("1.00"), 60, snacks);
            crearProducto("Papas Lays 42g", new BigDecimal("1.50"), 50, snacks);
            logger.info("Productos de demostración creados");
        }
    }

    private void crearProducto(String nombre, BigDecimal precio, int stock, Categoria categoria) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setPrecio(precio);
        p.setStock(stock);
        p.setCategoria(categoria);
        productoRepository.save(p);
    }
}
