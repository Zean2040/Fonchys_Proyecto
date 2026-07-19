@echo off
REM ============================================================
REM  Script de Backup - Fonchys Minimarket (Windows)
REM  Ejecutar diariamente via Task Scheduler a las 2:00 AM
REM  Tarea: schtasks /create /tn "FonchysBackup" /tr "C:\ruta\backup_db.bat"
REM         /sc daily /st 02:00
REM ============================================================

SET DB_HOST=localhost
SET DB_PORT=3306
SET DB_NAME=fonchys_db
SET DB_USER=root
SET DB_PASS=12345
SET BACKUP_DIR=C:\fonchys\backups
SET MYSQLDUMP_PATH=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe

REM Crear carpeta de backups si no existe
IF NOT EXIST "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

REM Nombre del archivo con fecha y hora
FOR /f "tokens=1-4 delims=/ " %%a IN ("%date%") DO SET HOY=%%d%%b%%c
FOR /f "tokens=1-2 delims=: " %%a IN ("%time%") DO SET HORA=%%a%%b
SET HORA=%HORA: =0%
SET ARCHIVO=%BACKUP_DIR%\fonchys_%HOY%_%HORA%.sql

REM Ejecutar mysqldump
"%MYSQLDUMP_PATH%" -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASS% %DB_NAME% > "%ARCHIVO%"

IF %ERRORLEVEL% EQU 0 (
    echo [%date% %time%] Backup exitoso: %ARCHIVO% >> "%BACKUP_DIR%\backup.log"
    echo Backup completado: %ARCHIVO%
) ELSE (
    echo [%date% %time%] ERROR en backup >> "%BACKUP_DIR%\backup.log"
    echo ERROR: Fallo el backup. Revise %BACKUP_DIR%\backup.log
    exit /b 1
)

REM Eliminar backups con mas de 30 dias
forfiles /p "%BACKUP_DIR%" /m "fonchys_*.sql" /d -30 /c "cmd /c del @path" 2>nul
echo Limpieza de backups antiguos completada.
