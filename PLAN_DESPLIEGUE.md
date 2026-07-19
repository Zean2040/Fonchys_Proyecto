# Plan de Despliegue - Fonchys Minimarket

## 1. Requisitos del Servidor

| Componente | Version minima | Notas |
|---|---|---|
| Java JDK | 17 | `java -version` para verificar |
| MySQL | 8.0 | Motor de base de datos |
| Maven | 3.8+ | O usar `mvnw` incluido |
| RAM | 512 MB minimo | 1 GB recomendado |
| Disco | 500 MB | Para app, logs y backups |

## 2. Despliegue en Entorno Local (Desarrollo)

### Paso 1 — Crear la base de datos
```sql
CREATE DATABASE fonchys_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Paso 2 — Compilar y ejecutar
```bash
# Con Maven Wrapper (incluido en el proyecto)
cd demo
./mvnw spring-boot:run          # Linux/Mac
mvnw.cmd spring-boot:run        # Windows
```

### Paso 3 — Verificar
Abrir `http://localhost:8080` en el navegador.
- Usuario admin: `admin@fonchys.com` / `admin123`
- Health check: `http://localhost:8080/actuator/health`

## 3. Despliegue en Servidor de Produccion

### Paso 1 — Compilar el JAR
```bash
./mvnw clean package -DskipTests
# Resultado: target/fonchys-minimarket-1.0.0.jar
```

### Paso 2 — Transferir al servidor
```bash
scp target/fonchys-minimarket-1.0.0.jar usuario@servidor:/opt/fonchys/
```

### Paso 3 — Configurar variables de entorno en el servidor
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_USER=fonchys_user
export DB_PASS=password_segura_aqui
export SPRING_PROFILES_ACTIVE=prod
```

### Paso 4 — Crear usuario de BD dedicado (no usar root en prod)
```sql
CREATE USER 'fonchys_user'@'localhost' IDENTIFIED BY 'password_segura_aqui';
GRANT SELECT, INSERT, UPDATE, DELETE ON fonchys_db.* TO 'fonchys_user'@'localhost';
FLUSH PRIVILEGES;
```

### Paso 5 — Ejecutar la aplicacion
```bash
# Ejecucion directa
java -jar fonchys-minimarket-1.0.0.jar --spring.profiles.active=prod

# Ejecucion en background (Linux)
nohup java -jar fonchys-minimarket-1.0.0.jar \
  --spring.profiles.active=prod \
  > /opt/fonchys/logs/startup.log 2>&1 &
echo $! > /opt/fonchys/fonchys.pid
```

### Paso 6 — Verificar despliegue
```bash
curl http://localhost:8080/actuator/health
# Respuesta esperada: {"status":"UP"}
```

## 4. Script Automatizado (Windows)

```bat
cd demo
scripts\deploy.bat prod
```

El script realiza: compilacion Maven -> cierre de instancia anterior -> inicio con perfil correcto -> verificacion de salud.

## 5. Configuracion como Servicio del Sistema Operativo

### Windows (NSSM - Non-Sucking Service Manager)
```bat
nssm install FonchysMinimarket "java" "-jar C:\fonchys\fonchys-minimarket-1.0.0.jar --spring.profiles.active=prod"
nssm set FonchysMinimarket AppDirectory C:\fonchys
nssm start FonchysMinimarket
```

### Linux (systemd)
Crear archivo `/etc/systemd/system/fonchys.service`:
```ini
[Unit]
Description=Fonchys Minimarket
After=network.target mysql.service

[Service]
Type=simple
User=fonchys
WorkingDirectory=/opt/fonchys
ExecStart=java -jar fonchys-minimarket-1.0.0.jar --spring.profiles.active=prod
EnvironmentFile=/opt/fonchys/.env
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
systemctl enable fonchys
systemctl start fonchys
systemctl status fonchys
```

## 6. Rollback ante Falla

```bash
# Detener version actual
kill $(cat fonchys.pid)

# Restaurar version anterior
java -jar fonchys-minimarket-ANTERIOR.jar --spring.profiles.active=prod &

# Si la BD fue alterada, restaurar backup
mysql -u fonchys_user -p fonchys_db < /var/backups/fonchys/fonchys_FECHA.sql
```
