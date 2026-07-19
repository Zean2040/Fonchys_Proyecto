@echo off
REM ============================================================
REM  Script de Despliegue - Fonchys Minimarket (Windows)
REM  Uso: deploy.bat [dev|prod]
REM ============================================================

SET PERFIL=%1
IF "%PERFIL%"=="" SET PERFIL=dev

SET JAR_NAME=fonchys-minimarket-1.0.0.jar
SET JAR_PATH=target\%JAR_NAME%
SET LOG_DIR=logs
SET PID_FILE=fonchys.pid

echo ============================================================
echo  Desplegando Fonchys Minimarket - Perfil: %PERFIL%
echo ============================================================

REM 1. Compilar con Maven
echo [1/4] Compilando proyecto con Maven...
call mvnw.cmd clean package -DskipTests
IF %ERRORLEVEL% NEQ 0 (
    echo ERROR: Fallo la compilacion. Revise los errores de Maven.
    exit /b 1
)
echo OK: Compilacion exitosa.

REM 2. Crear directorio de logs
IF NOT EXIST "%LOG_DIR%" mkdir "%LOG_DIR%"

REM 3. Detener instancia anterior si existe
IF EXIST "%PID_FILE%" (
    echo [2/4] Deteniendo instancia anterior...
    SET /p OLD_PID=<"%PID_FILE%"
    taskkill /PID %OLD_PID% /F >nul 2>&1
    del "%PID_FILE%"
    timeout /t 3 /nobreak >nul
) ELSE (
    echo [2/4] No hay instancia previa en ejecucion.
)

REM 4. Iniciar la aplicacion
echo [3/4] Iniciando aplicacion (perfil: %PERFIL%)...
IF "%PERFIL%"=="prod" (
    REM Variables de entorno para produccion - ajustar segun servidor
    SET DB_HOST=localhost
    SET DB_PORT=3306
    SET DB_USER=fonchys_user
    SET DB_PASS=cambiar_password_segura
    start /B java -jar "%JAR_PATH%" --spring.profiles.active=prod > "%LOG_DIR%\startup.log" 2>&1
) ELSE (
    start /B java -jar "%JAR_PATH%" > "%LOG_DIR%\startup.log" 2>&1
)

REM Guardar PID
FOR /F "tokens=2" %%i IN ('tasklist /FI "IMAGENAME eq java.exe" /NH') DO (
    echo %%i > "%PID_FILE%"
    GOTO pid_guardado
)
:pid_guardado

echo [4/4] Verificando inicio (esperar 15 segundos)...
timeout /t 15 /nobreak >nul

REM Verificar salud
curl -s http://localhost:8080/actuator/health >nul 2>&1
IF %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================================
    echo  DESPLIEGUE EXITOSO
    echo  URL: http://localhost:8080
    echo  Health: http://localhost:8080/actuator/health
    echo ============================================================
) ELSE (
    echo ADVERTENCIA: La aplicacion puede estar iniciando. Revise %LOG_DIR%\startup.log
)
