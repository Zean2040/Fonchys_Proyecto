# Plan de Monitoreo - Fonchys Minimarket

## 1. Endpoints de Salud (Spring Actuator)

Con la dependencia `spring-boot-starter-actuator` habilitada, los siguientes endpoints estan disponibles:

| Endpoint | URL | Acceso | Descripcion |
|---|---|---|---|
| Health | `GET /actuator/health` | Publico | Estado general del sistema y BD |
| Info | `GET /actuator/info` | ADMIN | Version e informacion del sistema |
| Metrics | `GET /actuator/metrics` | ADMIN | Metricas de JVM, HTTP, memoria |
| Logfile | `GET /actuator/logfile` | ADMIN | Contenido del log activo |

### Respuesta esperada de `/actuator/health`
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" }
  }
}
```

## 2. Logs de Aplicacion

### Archivos de log
- **Log activo:** `logs/fonchys.log`
- **Logs historicos:** `logs/fonchys.YYYY-MM-DD.log`
- **Rotacion:** Diaria automatica via Logback
- **Retencion:** 30 dias / 100 MB maximo total

### Niveles de log por modulo
| Paquete | Nivel | Proposito |
|---|---|---|
| `com.fonchys` | INFO | Operaciones de negocio (ventas, productos) |
| `com.fonchys.service.MantenimientoService` | INFO/WARN | Alertas de stock, heartbeat |
| `org.springframework.security` | WARN | Fallos de autenticacion |
| `org.hibernate.SQL` | WARN | Solo errores de BD |

### Comandos para revisar logs en tiempo real
```bash
# Windows
Get-Content logs\fonchys.log -Wait -Tail 50

# Linux
tail -f logs/fonchys.log
grep "ERROR" logs/fonchys.log
grep "ALERTA" logs/fonchys.log
```

## 3. Alertas Automaticas (MantenimientoService)

El servicio `MantenimientoService.java` genera las siguientes entradas de log:

- `[HEALTH]` — Heartbeat cada hora: confirma que la app esta activa y cuantos productos hay en BD.
- `[MANTENIMIENTO] ALERTA:` — Se genera a las 8 AM si hay productos con stock <= 5.
- `[MANTENIMIENTO]` — Registra inicio y fin de ventana nocturna de mantenimiento.

**Buscar alertas de stock en logs:**
```bash
grep "ALERTA" logs/fonchys.log
```

## 4. Metricas JVM (via Actuator)

Acceder a `http://localhost:8080/actuator/metrics` con cuenta ADMIN para ver:

- `jvm.memory.used` — Memoria heap en uso
- `jvm.threads.live` — Hilos activos
- `http.server.requests` — Requests por endpoint y tiempo de respuesta
- `hikaricp.connections.active` — Conexiones activas al pool de BD

**Ejemplo — revisar memoria:**
```
GET /actuator/metrics/jvm.memory.used
```

## 5. Indicadores de Salud del Sistema

| Indicador | Umbral de alerta | Accion |
|---|---|---|
| Memoria heap | > 80% | Reiniciar aplicacion |
| Stock bajo | <= 5 unidades | Revisar log de alerta (8 AM) |
| Disco backups | > 90% | Eliminar backups antiguos manualmente |
| Log file size | > 100 MB total | Logback rota automaticamente |
| Conexiones BD | > 10 activas | Revisar consultas lentas |

## 6. Verificacion Manual Periodica

| Frecuencia | Tarea |
|---|---|
| Diaria | Revisar `GET /actuator/health` (debe ser `"status": "UP"`) |
| Diaria | Verificar log de la noche anterior para errores |
| Semanal | Revisar alertas de stock en logs |
| Mensual | Revisar metricas de tiempo de respuesta HTTP |
