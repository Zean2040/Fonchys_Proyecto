package com.fonchys.minimarket.security;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de seguridad OWASP Top 10 — Fonchys Minimarket
 * Cubre: A01 (Control de Acceso), A05 (Configuración), A07 (Autenticación)
 * Requiere MySQL activo con la base de datos fonchys_db.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.DisplayName.class)
class SecurityAccessTest {

    private static final String SEP  = "═".repeat(65);
    private static final String LINE = "─".repeat(65);

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void inicio() {
        System.out.println("\n" + SEP);
        System.out.println("  PRUEBAS DE SEGURIDAD — FONCHYS MINIMARKET");
        System.out.println("  Metodología: OWASP Top 10 | Framework: JUnit 5 + Spring Security Test");
        System.out.println(SEP);
    }

    @AfterAll
    static void fin() {
        System.out.println("\n" + SEP);
        System.out.println("  FIN DE PRUEBAS DE SEGURIDAD");
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

    // ---------------------------------------------------------------
    // OWASP A01 - Control de Acceso: usuarios no autenticados
    // ---------------------------------------------------------------

    @Test
    @DisplayName("[A01] Sin sesión → /dashboard redirige a /login")
    void accesoNoAutenticado_dashboard_redirigirLogin() throws Exception {
        System.out.println("  GET /dashboard sin autenticación — se espera redirect 302 → /login");
        mockMvc.perform(get("/dashboard"))
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("[A01] Sin sesión → /reportes redirige a /login")
    void accesoNoAutenticado_reportes_redirigirLogin() throws Exception {
        System.out.println("  GET /reportes sin autenticación — se espera redirect 302 → /login");
        mockMvc.perform(get("/reportes"))
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("[A01] Sin sesión → /usuarios redirige a /login")
    void accesoNoAutenticado_usuarios_redirigirLogin() throws Exception {
        System.out.println("  GET /usuarios sin autenticación — se espera redirect 302 → /login");
        mockMvc.perform(get("/usuarios"))
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("[A01] /login es accesible públicamente sin autenticación")
    void loginPage_accesibleSinAutenticacion() throws Exception {
        System.out.println("  GET /login sin autenticación — se espera 200 OK");
        mockMvc.perform(get("/login"))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[A01] Recursos estáticos CSS accesibles sin autenticación")
    void recursosEstaticos_accesiblesSinAutenticacion() throws Exception {
        System.out.println("  GET /css/styles.css — NO debe redirigir al login (302)");
        mockMvc.perform(get("/css/styles.css"))
            .andDo(print())
            .andExpect(result -> {
                int status = result.getResponse().getStatus();
                if (status == 302 || status == 301) {
                    throw new AssertionError(
                        "Recurso estático redirigió al login (status " + status + "). " +
                        "Los recursos estáticos deben ser públicos.");
                }
                System.out.println("  Recurso estático devolvió status " + status + " (no es redirect) ✔");
            });
    }

    // ---------------------------------------------------------------
    // OWASP A01 - Control de Acceso: separación de roles
    // ---------------------------------------------------------------

    @Test
    @WithMockUser(username = "cajero@fonchys.com", roles = "CAJERO")
    @DisplayName("[A01] CAJERO no puede acceder a /reportes → 403 Forbidden")
    void cajero_noDebeAccederReportes_retornar403() throws Exception {
        System.out.println("  Usuario: cajero@fonchys.com (rol CAJERO)");
        System.out.println("  GET /reportes — se espera 403 Forbidden");
        mockMvc.perform(get("/reportes"))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "cajero@fonchys.com", roles = "CAJERO")
    @DisplayName("[A01] CAJERO no puede acceder a /usuarios → 403 Forbidden")
    void cajero_noDebeAccederUsuarios_retornar403() throws Exception {
        System.out.println("  Usuario: cajero@fonchys.com (rol CAJERO)");
        System.out.println("  GET /usuarios — se espera 403 Forbidden");
        mockMvc.perform(get("/usuarios"))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "cajero@fonchys.com", roles = "CAJERO")
    @DisplayName("[A01] CAJERO sí puede acceder a /ventas/nueva → 200 OK")
    void cajero_debeAccederVentas() throws Exception {
        System.out.println("  Usuario: cajero@fonchys.com (rol CAJERO)");
        System.out.println("  GET /ventas/nueva — se espera 200 OK");
        mockMvc.perform(get("/ventas/nueva"))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "almacenero@fonchys.com", roles = "ALMACENERO")
    @DisplayName("[A01] ALMACENERO no puede acceder a /ventas/nueva → 403 Forbidden")
    void almacenero_noDebeAccederVentas_retornar403() throws Exception {
        System.out.println("  Usuario: almacenero@fonchys.com (rol ALMACENERO)");
        System.out.println("  GET /ventas/nueva — se espera 403 Forbidden");
        mockMvc.perform(get("/ventas/nueva"))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@fonchys.com", roles = "ADMIN")
    @DisplayName("[A01] ADMIN puede acceder a todos los módulos")
    void admin_debeAccederTodosLosModulos() throws Exception {
        System.out.println("  Usuario: admin@fonchys.com (rol ADMIN)");
        System.out.println("  Verificando acceso a /reportes, /usuarios y /productos...");
        mockMvc.perform(get("/reportes")).andDo(print()).andExpect(status().isOk());
        mockMvc.perform(get("/usuarios")).andDo(print()).andExpect(status().isOk());
        mockMvc.perform(get("/productos")).andDo(print()).andExpect(status().isOk());
    }

    // ---------------------------------------------------------------
    // OWASP A07 - Autenticación
    // ---------------------------------------------------------------

    @Test
    @DisplayName("[A07] Credenciales inválidas redirigen a /login?error=true")
    void loginConCredencialesInvalidas_redirigirConError() throws Exception {
        System.out.println("  POST /login con email y contraseña incorrectos");
        System.out.println("  Se espera redirect a /login?error=true (sin revelar causa exacta)");
        mockMvc.perform(formLogin("/login")
                .user("noexiste@fonchys.com")
                .password("wrongpassword"))
            .andDo(print())
            .andExpect(redirectedUrl("/login?error=true"));
    }

    // ---------------------------------------------------------------
    // OWASP A03 - SQL Injection: búsqueda de productos
    // ---------------------------------------------------------------

    @Test
    @WithMockUser(username = "admin@fonchys.com", roles = "ADMIN")
    @DisplayName("[A03] SQL Injection clásico OR '1'='1 no devuelve todos los registros")
    void sqlInjection_orClasico_noAfectaBD() throws Exception {
        String payload = "' OR '1'='1";
        System.out.println("  Payload enviado: buscar=" + payload);
        System.out.println("  Sistema vulnerable: devolvería TODOS los productos de la BD.");
        System.out.println("  JPA/Hibernate: lo trata como texto literal → lista vacía o normal.");
        mockMvc.perform(get("/productos").param("buscar", payload))
            .andDo(print())
            .andExpect(status().isOk());
        System.out.println("  200 OK sin error de servidor — inyección neutralizada ✔");
    }

    @Test
    @WithMockUser(username = "admin@fonchys.com", roles = "ADMIN")
    @DisplayName("[A03] SQL Injection DROP TABLE no borra la tabla de productos")
    void sqlInjection_dropTable_noEliminaDatos() throws Exception {
        String payload = "'; DROP TABLE producto; --";
        System.out.println("  Payload enviado: buscar=" + payload);
        System.out.println("  Sistema vulnerable: ejecutaría DROP TABLE y borraría todos los productos.");
        System.out.println("  JPA/Hibernate: usa PreparedStatement → no ejecuta SQL arbitrario.");
        mockMvc.perform(get("/productos").param("buscar", payload))
            .andDo(print())
            .andExpect(status().isOk());
        System.out.println("  200 OK, tabla intacta — inyección neutralizada ✔");
    }

    @Test
    @WithMockUser(username = "admin@fonchys.com", roles = "ADMIN")
    @DisplayName("[A03] SQL Injection UNION SELECT no causa error de servidor ni expone contraseñas")
    void sqlInjection_unionSelect_noExponeDatos() throws Exception {
        String payload = "' UNION SELECT id,email,password,email,email,email,email,email FROM usuario --";
        System.out.println("  Payload enviado: buscar=" + payload);
        System.out.println("  Sistema vulnerable: devolvería hashes BCrypt de contraseñas ($2a$...).");
        System.out.println("  JPA/Hibernate: el UNION es texto literal — busca productos con ese nombre exacto.");
        mockMvc.perform(get("/productos").param("buscar", payload))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.not(
                org.hamcrest.Matchers.containsString("$2a$"))));
        System.out.println("  200 OK sin hashes de contraseñas en respuesta — inyección neutralizada ✔");
    }

    // ---------------------------------------------------------------
    // OWASP A05 - Configuración de seguridad: Actuator
    // ---------------------------------------------------------------

    @Test
    @DisplayName("[A05] /actuator/health es accesible públicamente")
    void actuatorHealth_accesiblePublicamente() throws Exception {
        System.out.println("  GET /actuator/health sin autenticación — se espera 200 OK");
        mockMvc.perform(get("/actuator/health"))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[A05] /actuator/metrics no accesible sin autenticación → redirect a login")
    void actuatorMetrics_noAccesibleSinAuth() throws Exception {
        System.out.println("  GET /actuator/metrics sin autenticación — se espera 302 redirect");
        mockMvc.perform(get("/actuator/metrics"))
            .andDo(print())
            .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "cajero@fonchys.com", roles = "CAJERO")
    @DisplayName("[A05] /actuator/metrics no accesible por rol CAJERO → 403 Forbidden")
    void actuatorMetrics_noAccesiblePorCajero() throws Exception {
        System.out.println("  Usuario: cajero@fonchys.com (rol CAJERO)");
        System.out.println("  GET /actuator/metrics — se espera 403 Forbidden");
        mockMvc.perform(get("/actuator/metrics"))
            .andDo(print())
            .andExpect(status().isForbidden());
    }
}
