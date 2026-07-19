# Reporte de Pruebas de Seguridad - Fonchys Minimarket

**Fecha:** 2026-07-18
**Version evaluada:** 1.0.0
**Metodologia:** OWASP Top 10 (2021)

---

## Resumen Ejecutivo

| Categoria OWASP | Riesgo identificado | Estado |
|---|---|---|
| A01 - Broken Access Control | Control de roles por endpoint | MITIGADO |
| A02 - Cryptographic Failures | Almacenamiento de passwords | MITIGADO |
| A03 - Injection | SQL Injection via JPA | MITIGADO |
| A04 - Insecure Design | Separacion de capas MVC | MITIGADO |
| A05 - Security Misconfiguration | Actuator expuesto | MITIGADO |
| A07 - Auth Failures | Sesiones concurrentes | MITIGADO |
| A08 - Software Integrity Failures | Dependencias conocidas | PENDIENTE revision |

---

## A01 - Broken Access Control

### Descripcion
Verificacion de que cada endpoint solo es accesible por los roles autorizados.

### Pruebas realizadas (`SecurityAccessTest.java`)

| Test | Escenario | Resultado esperado | Estado |
|---|---|---|---|
| `accesoNoAutenticado_dashboard_redirigirLogin` | Sin sesion accede a `/dashboard` | Redireccion 302 a `/login` | PASS |
| `accesoNoAutenticado_reportes_redirigirLogin` | Sin sesion accede a `/reportes` | Redireccion 302 a `/login` | PASS |
| `accesoNoAutenticado_usuarios_redirigirLogin` | Sin sesion accede a `/usuarios` | Redireccion 302 a `/login` | PASS |
| `cajero_noDebeAccederReportes_retornar403` | CAJERO accede a `/reportes` | HTTP 403 Forbidden | PASS |
| `cajero_noDebeAccederUsuarios_retornar403` | CAJERO accede a `/usuarios` | HTTP 403 Forbidden | PASS |
| `cajero_debeAccederVentas` | CAJERO accede a `/ventas/nueva` | HTTP 200 OK | PASS |
| `almacenero_noDebeAccederVentas_retornar403` | ALMACENERO accede a `/ventas/nueva` | HTTP 403 Forbidden | PASS |
| `admin_debeAccederTodosLosModulos` | ADMIN accede a todos los modulos | HTTP 200 OK | PASS |

### Implementacion de mitigacion
- `@PreAuthorize` en metodos de controlador
- `SecurityConfig` con `.hasRole()` y `.hasAnyRole()` por URL
- Tres roles definidos: ADMIN, CAJERO, ALMACENERO

---

## A02 - Cryptographic Failures

### Descripcion
Evaluacion del almacenamiento y transmision de credenciales.

### Hallazgos

| Item | Estado | Evidencia |
|---|---|---|
| Passwords hasheadas con BCrypt | CORRECTO | `SecurityConfig.java` - `BCryptPasswordEncoder` |
| Passwords en texto plano en BD | NO EXISTE | Verificado en `DataInitializer.java` - usa `passwordEncoder.encode()` |
| Transmision via HTTP | RIESGO en prod | En desarrollo: sin SSL. En produccion usar HTTPS |

### Recomendacion para produccion
Configurar SSL en el servidor (Nginx como proxy reverso con certificado Let's Encrypt):
```nginx
server {
    listen 443 ssl;
    ssl_certificate /etc/letsencrypt/live/dominio.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/dominio.com/privkey.pem;
    location / { proxy_pass http://localhost:8080; }
}
```

---

## A03 - Injection (SQL Injection)

### Descripcion
Verificacion de que las consultas a BD son parametrizadas y no vulnerables a inyeccion SQL.

### Hallazgos

| Item | Estado | Evidencia |
|---|---|---|
| Uso de Spring Data JPA | CORRECTO | Repositorios heredan de `JpaRepository` |
| Consultas con `@Query` parametrizadas | CORRECTO | Usan `:param` nombrados, no concatenacion de strings |
| Entrada de usuario en busquedas | CORRECTO | `ValidacionUtil.limpiarTexto()` normaliza entrada |

### Ejemplo de consulta segura verificada
```java
// ProductoRepository.java - consulta parametrizada con :nombre
@Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(@Param("nombre") String nombre);
```

---

## A04 - Insecure Design

### Descripcion
Evaluacion de la arquitectura y separacion de responsabilidades.

### Hallazgos

| Item | Estado |
|---|---|
| Separacion MVC (Controller / Service / Repository) | CORRECTO |
| Logica de negocio en servicios, no en controladores | CORRECTO |
| Validacion de entrada con Bean Validation (`@NotBlank`, `@Email`) | CORRECTO |
| Validacion adicional con Google Guava `Preconditions` | CORRECTO |
| Soft-delete en lugar de eliminacion fisica | CORRECTO (flag `activo`) |

---

## A05 - Security Misconfiguration

### Descripcion
Verificacion de que los endpoints de administracion no esten expuestos publicamente.

### Pruebas realizadas

| Test | Escenario | Resultado esperado | Estado |
|---|---|---|---|
| `actuatorHealth_accesiblePublicamente` | `/actuator/health` sin auth | HTTP 200 | PASS |
| `actuatorMetrics_noAccesibleSinAuth` | `/actuator/metrics` sin auth | Redireccion a login | PASS |
| `actuatorMetrics_noAccesiblePorCajero` | CAJERO accede a `/actuator/metrics` | HTTP 403 | PASS |

### Configuracion aplicada
```properties
management.endpoints.web.exposure.include=health,info,metrics,logfile
management.endpoint.health.show-details=when-authorized
management.endpoint.health.roles=ADMIN
```

---

## A07 - Identification and Authentication Failures

### Descripcion
Evaluacion de mecanismos de autenticacion y gestion de sesiones.

### Hallazgos

| Item | Estado | Evidencia |
|---|---|---|
| Sesiones concurrentes limitadas a 1 | CORRECTO | `sessionManagement().maximumSessions(1)` |
| Cookie de sesion eliminada al logout | CORRECTO | `.deleteCookies("JSESSIONID")` |
| Sesion invalidada al logout | CORRECTO | `.invalidateHttpSession(true)` |
| URL de error de login no revela informacion | CORRECTO | Solo redirige a `/login?error=true` |

### Pruebas realizadas

| Test | Escenario | Resultado esperado | Estado |
|---|---|---|---|
| `loginConCredencialesInvalidas_redirigirConError` | Password incorrecto | Redireccion a `/login?error=true` | PASS |

---

## A08 - Software and Data Integrity Failures

### Descripcion
Evaluacion de dependencias de terceros por vulnerabilidades conocidas.

### Estado
- Evaluacion pendiente de ejecucion con herramienta OWASP Dependency-Check.

**Comando para ejecutar:**
```bash
mvn org.owasp:dependency-check-maven:check
# Reporte generado en: target/dependency-check-report.html
```

### Dependencias de alto riesgo a verificar
| Dependencia | Version | Accion |
|---|---|---|
| Spring Boot | 3.2.3 | Verificar CVEs activos |
| Apache POI | 5.2.5 | Verificar CVEs activos |
| MySQL Connector | Gestionado por Spring Boot | Verificar |

---

## Observaciones y Recomendaciones

### Criticas (resolver antes de produccion)
1. **HTTPS obligatorio:** Configurar SSL/TLS en el servidor de produccion para proteger credenciales en transito.
2. **Credenciales de BD en variables de entorno:** Ya implementado en `application-prod.properties`. No usar credenciales hardcodeadas.

### Medias (resolver en proxima iteracion)
3. **Headers de seguridad HTTP:** Agregar `X-Content-Type-Options`, `X-Frame-Options`, `Content-Security-Policy` via Spring Security.
4. **Rate limiting en login:** Proteger contra ataques de fuerza bruta con limite de intentos fallidos.
5. **Audit log:** Registrar quien realizo que accion (crear venta, modificar producto, etc.) con timestamp y usuario.

### Bajas
6. **Dependency-Check automatico:** Integrar `mvn dependency-check` en el pipeline de CI/CD.
