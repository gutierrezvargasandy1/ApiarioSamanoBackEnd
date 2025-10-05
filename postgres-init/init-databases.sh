#!/bin/bash
set -e

# Usuario y contraseña del superusuario definido en docker-compose
PG_USER="${POSTGRES_USER:-apiario}"
PG_PASSWORD="${POSTGRES_PASSWORD:-apiario123}"

# Bases de datos de los microservicios
DBS=("usuario_db" )

# Conexión al contenedor usando psql como superusuario
export PGPASSWORD="$PG_PASSWORD"

for DB in "${DBS[@]}"; do
    echo "Creando base de datos: $DB si no existe..."
    psql -v ON_ERROR_STOP=1 --username "$PG_USER" --dbname "postgres" <<-EOSQL
        CREATE DATABASE $DB;
        GRANT ALL PRIVILEGES ON DATABASE $DB TO $PG_USER;
EOSQL
done

echo "Todas las bases de datos han sido creadas exitosamente."
