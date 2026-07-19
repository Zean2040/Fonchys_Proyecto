# Plan de Mantenimiento - Fonchys Minimarket

## 1. Tareas Automatizadas con Spring Scheduler

El servicio `MantenimientoService.java` ejecuta las siguientes tareas en segundo plano:

| Tarea | Horario | Descripcion |
|---|---|---|
| `alertaStockBajo()` | Todos los dias 08:00 AM | Detecta productos con stock <= 5 y los registra en el log |
| `heartbeatSistema()` | Cada hora | Registra que el sistema esta activo y cuenta registros en BD |
| `limpiezaDiaria()` | Todos los dias 02:00 AM | Ventana de mantenimiento nocturno, registra inicio/fin |

## 2. Backup de Base de Datos

### Windows (Task Scheduler)
1. Abrir "Programador de tareas" de Windows.
2. Crear tarea basica con accion: `C:\Fonchys-Proyecto\demo\scripts\backup_db.bat`
3. Programar: Diariamente a las 02:00 AM.
4. Los backups se guardan en `C:\fonchys\backups\` con formato `fonchys_YYYYMMDD_HHmm.sql`.
5. Los backups de mas de 30 dias se eliminan automaticamente.

**Via linea de comandos:**
```bat
schtasks /create /tn "FonchysBackup" /tr "C:\Fonchys-Proyecto\demo\scripts\backup_db.bat" /sc daily /st 02:00 /ru SYSTEM
```

### Linux/Servidor (cron)
1. Dar permiso de ejecucion: `chmod +x backup_db.sh`
2. Editar crontab: `crontab -e`
3. Agregar linea:
```
0 2 * * * /ruta/fonchys/scripts/backup_db.sh >> /var/log/fonchys_backup.log 2>&1
```
4. Los backups se guardan comprimidos (`.sql.gz`) en `/var/backups/fonchys/`.

## 3. Rotacion de Logs

Configurado en `logback-spring.xml`:
- **Archivo activo:** `logs/fonchys.log`
- **Rotacion:** Diaria automatica
- **Retencion:** 30 dias
- **Tamano maximo total:** 100 MB

No se requiere intervencion manual.

## 4. Actualizacion de Dependencias

| Frecuencia | Accion |
|---|---|
| Mensual | Revisar vulnerabilidades con `mvn dependency:check` |
| Mensual | Actualizar version de Spring Boot si hay parche de seguridad |
| Trimestral | Revisar versiones de Apache POI y Guava |

```bash
# Verificar dependencias desactualizadas
mvn versions:display-dependency-updates

# Verificar vulnerabilidades conocidas (OWASP)
mvn org.owasp:dependency-check-maven:check
```

## 5. Procedimiento ante Falla

1. **Aplicacion no responde:** Reiniciar con `java -jar fonchys-minimarket-1.0.0.jar`
2. **BD corrupta:** Restaurar ultimo backup: `mysql -u root -p fonchys_db < backup.sql`
3. **Logs llenos:** Los logs rotan automaticamente. Si el disco esta lleno: `find logs/ -mtime +7 -delete`

## 6. Cronograma Mensual

| Semana | Actividad |
|---|---|
| 1 | Revisar logs de errores del mes anterior |
| 2 | Verificar espacio en disco y tamano de BD |
| 3 | Probar restauracion de backup |
| 4 | Actualizar dependencias si hay parches criticos |
