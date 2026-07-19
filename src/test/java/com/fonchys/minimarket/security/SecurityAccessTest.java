package com.fonchys.minimarket.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de seguridad: verifica control de acceso por roles y proteccion
 * contra accesos no autorizados (OWASP A01 - Broken Access Control).
 * Requiere MySQL corriendo con la base de datos fonchys_db.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    // ---------------------------------------------------------------
    // OWASP A01 - Control de Acceso: usuarios no autenticados
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Usuario no autenticado debe ser redirigido al login al acceder al dashboard")
    void accesoNoAutenticado_dashboard_redirigirLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Usuario no autenticado debe ser redirigido al login al intentar ver reportes")
    void accesoNoAutenticado_reportes_redirigirLogin() throws Exception {
        mockMvc.perform(get("/reportes"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Usuario no autenticado debe ser redirigido al login al acceder a usuarios")
    void accesoNoAutenticado_usuarios_redirigirLogin() throws Exception {
        mockMvc.perform(get("/usuarios"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("La pagina de login debe ser accesible publicamente")
    void loginPage_accesibleSinAutenticacion() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Los recursos CSS deben ser accesibles publicamente")
    void recursosEstaticos_accesiblesSinAutenticacion() throws Exception {
        mockMvc.perform(get("/css/styles.css"))
            .andExpect(status().isNotFound()); // 404 si no existe, pero NO 302 a login
    }

    // ---------------------------------------------------------------
    // OWASP A01 - Control de Acceso: separacion de roles
    // ---------------------------------------------------------------

    @Test
    @WithMockUser(username = "cajero@fonchys.com", roles = "CAJERO")
    @DisplayName("CAJERO no debe poder acceder a reportes (solo ADMIN)")
    void cajero_noDebeAccederReportes_retornar403() throws Exception {
        mockMvc.perform(get("/reportes"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "cajero@fonchys.com", roles = "CAJERO")
    @DisplayName("CAJERO no debe poder acceder a gestion de usuarios")
    void cajero_noDebeAccederUsuarios_retornar403() throws Exception {
        mockMvc.perform(get("/usuarios"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "cajero@fonchys.com", roles = "CAJERO")
    @DisplayName("CAJERO debe poder acceder a ventas")
    void cajero_debeAccederVentas() throws Exception {
        mockMvc.perform(get("/ventas/nueva"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "almacenero@fonchys.com", roles = "ALMACENERO")
    @DisplayName("ALMACENERO no debe poder acceder a ventas (solo ADMIN y CAJERO)")
    void almacenero_noDebeAccederVentas_retornar403() throws Exception {
        mockMvc.perform(get("/ventas/nueva"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@fonchys.com", roles = "ADMIN")
    @DisplayName("ADMIN debe poder acceder a todos los modulos")
    void admin_debeAccederTodosLosModulos() throws Exception {
        mockMvc.perform(get("/reportes")).andExpect(status().isOk());
        mockMvc.perform(get("/usuarios")).andExpect(status().isOk());
        mockMvc.perform(get("/productos")).andExpect(status().isOk());
    }

    // ---------------------------------------------------------------
    // OWASP A07 - Autenticacion: endpoint de login
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Credenciales incorrectas deben redirigir a login con error")
    void loginConCredencialesInvalidas_redirigirConError() throws Exception {
        mockMvc.perform(formLogin("/login")
                .user("noexiste@fonchys.com")
                .password("wrongpassword"))
            .andExpect(redirectedUrl("/login?error=true"));
    }

    // ---------------------------------------------------------------
    // OWASP A05 - Security Misconfiguration: Actuator
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Actuator health debe ser publicamente accesible")
    void actuatorHealth_accesiblePublicamente() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Actuator metrics no debe ser accesible sin autenticacion")
    void actuatorMetrics_noAccesibleSinAuth() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "cajero@fonchys.com", roles = "CAJERO")
    @DisplayName("Actuator metrics no debe ser accesible por rol CAJERO")
    void actuatorMetrics_noAccesiblePorCajero() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
            .andExpect(status().isForbidden());
    }
}
