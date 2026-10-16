# Diseño del Componente

## Propósito

Guardar de forma persistente las medidas que llegan al servidor. Solo la clase `AlmacenMediciones` (ver `business_logic_design.md`) accede a la base de datos.

## Tablas

### TABLE: medidas

**DESCRIPTION:** Cada fila es una medida enviada por el sensor (a través de la app Android) y recibida por el servidor.

**COLUMNS:**

| Columna | Tipo | Nulo | Descripción |
|---|---|---|---|
| id | INT UNSIGNED AUTO_INCREMENT | NO | Identificador de la medida. Da el orden de llegada |
| tipo | TINYINT UNSIGNED | NO | Código de la medida: 11 CO2, 12 temperatura, 13 ruido. Ocupa 1 byte, igual que en el major del iBeacon |
| valor | SMALLINT | NO | Valor de la medida, de -32768 a 32767. 16 bits con signo, igual que el minor del iBeacon |
| contador | TINYINT UNSIGNED | NO | Contador del sensor, de 0 a 255. Cambia en cada medida nueva |
| fecha | DATETIME | NO | Fecha y hora en que la medida llegó al servidor |

**PRIMARY KEY:** `id`

**FOREIGN KEYS:** ninguna (en este sprint solo hay una tabla).

**CONSTRAINTS:**

- `chk_medidas_tipo`: `CHECK (tipo IN (11, 12, 13))`
- Todas las columnas son `NOT NULL`.
- Los rangos de `valor` (-32768..32767) y `contador` (0..255) los garantizan sus tipos de columna.

## Script de creación (`src/database/crear_bd.sql`)

```sql
CREATE TABLE IF NOT EXISTS medidas (
    id        INT UNSIGNED      NOT NULL AUTO_INCREMENT,
    tipo      TINYINT UNSIGNED  NOT NULL,
    valor     SMALLINT          NOT NULL,
    contador  TINYINT UNSIGNED  NOT NULL,
    fecha     DATETIME          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_medidas_tipo CHECK (tipo IN (11, 12, 13))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
```

## Consultas que usa `AlmacenMediciones`

Insertar una medida:

```sql
INSERT INTO medidas (tipo, valor, contador, fecha) VALUES (?, ?, ?, ?);
```

Obtener la última medida:

```sql
SELECT id, tipo, valor, contador, fecha FROM medidas ORDER BY id DESC LIMIT 1;
```

# Aclaraciones del Diseño

- La "última medida" es la de `id` más alto, es decir, la última que ha llegado al servidor.
- Los tipos de las columnas tienen el mismo tamaño que los campos del iBeacon, así que cualquier medida que emite el Arduino cabe sin perder información.
- El `contador` se guarda para poder detectar medidas repetidas o perdidas, pero no forma parte de la clave porque vuelve a 0 después de 255.
- En este sprint hay un solo sensor, por eso no hay tablas de sensores ni de usuarios ni claves foráneas. Se añadirán en sprints posteriores.
- La restricción `chk_medidas_tipo` repite la validación de `Logica` como segunda barrera (MySQL la aplica desde la versión 8.0.16).
- La base de datos se crea una vez ejecutando `crear_bd.sql` en phpMyAdmin.

# Reglas Generales

- Lenguaje de programación: SQL (MySQL / MariaDB).
- El encabezado de cada función o procedimiento SQL incluirá su diseño lógico dentro de un bloque de comentarios delimitado por líneas `--------------------`. En este sprint el script no tiene funciones ni procedimientos, solo la tabla.
- El código SQL será lo más claro posible, con nombres de tablas y columnas autoexplicativos.
- Se generarán tests automáticos que comprueben que las medidas se guardan y se recuperan bien (incluidos negativos y valores límite) y que un fallo de la base de datos se convierte en error. Están en `src/business_logic/test/test_logica.php` y usan una tabla equivalente en SQLite en memoria.
- El script de creación incluirá una cabecera en comentarios con: nombre, descripción, copyright, fecha, autor y aportación.