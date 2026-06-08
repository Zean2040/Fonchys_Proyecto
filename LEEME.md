# Fonchys Minimarket - Java Spring Boot

## Requisitos
- Java 17+
- Maven 3.8+
- MySQL 8.0+

## Configuración de MySQL

1. Crear la base de datos:
```sql
CREATE DATABASE fonchys_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Editar `src/main/resources/application.properties`:
```
spring.datasource.password=TU_PASSWORD_DE_MYSQL
```

## Ejecutar el proyecto

```bash
mvn spring-boot:run
```

Abrir en el navegador: http://localhost:8080

## Usuarios de prueba (se crean automáticamente)
| Email                  | Password   | Rol   |
|------------------------|------------|-------|
| admin@fonchys.com      | admin123   | ADMIN |
| cajero@fonchys.com     | cajero123  | CAJERO |

## Ejecutar tests
```bash
mvn test
```

## Estructura del proyecto

```
MVC:
  controller/   ← Controladores Spring MVC
  service/      ← Interfaces + implementaciones (SOLID)
  model/        ← Entidades JPA
  repository/   ← DAO con Spring Data JPA

Librerías:
  Google Guava      → Cache de productos, Preconditions
  Apache Commons    → StringUtils, NumberUtils
  Apache POI        → Exportar Excel en /reportes/ventas/excel
  Logback           → logs/ carpeta con logs diarios

TDD:
  test/service/     → ProductoServiceTest, VentaServiceTest
  test/util/        → ValidacionUtilTest
```
