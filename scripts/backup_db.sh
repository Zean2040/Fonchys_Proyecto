#!/bin/bash
# ============================================================
#  Script de Backup - Fonchys Minimarket (Linux/Mac)
#  Configurar en crontab: crontab -e
#  Linea cron: 0 2 * * * /ruta/backup_db.sh >> /var/log/fonchys_backup.log 2>&1
# ============================================================

DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="fonchys_db"
DB_USER="root"
DB_PASS="12345"
BACKUP_DIR="/var/backups/fonchys"
RETENTION_DAYS=30

# Crear directorio si no existe
mkdir -p "$BACKUP_DIR"

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
ARCHIVO="$BACKUP_DIR/fonchys_$TIMESTAMP.sql"

echo "[$(date '+%Y-%m-%d %H:%M:%S')] Iniciando backup de $DB_NAME..."

# Ejecutar mysqldump
mysqldump \
    -h "$DB_HOST" \
    -P "$DB_PORT" \
    -u "$DB_USER" \
    -p"$DB_PASS" \
    --single-transaction \
    --routines \
    --triggers \
    "$DB_NAME" > "$ARCHIVO"

if [ $? -eq 0 ]; then
    gzip "$ARCHIVO"
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] Backup exitoso: ${ARCHIVO}.gz ($(du -sh "${ARCHIVO}.gz" | cut -f1))"
else
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: Fallo el backup. Verifique conexion a MySQL."
    exit 1
fi

# Eliminar backups con mas de 30 dias
find "$BACKUP_DIR" -name "fonchys_*.sql.gz" -mtime +$RETENTION_DAYS -delete
echo "[$(date '+%Y-%m-%d %H:%M:%S')] Backups de mas de $RETENTION_DAYS dias eliminados."
