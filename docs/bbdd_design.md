# Diseño del Componente

## Propósito

Guardar de forma persistente las medidas que llegan al servidor. Solo la clase `AlmacenMediciones` (ver `logica_design.md`) accede a la base de datos.

## Modelo

```
------------------------------ medidas ------------------------------
|
|  id:        N      clave primaria, autoincremental
|  tipo:      N      no nulo, uno de { 11, 12, 13 }
|  valor:     Z      no nulo, -32768 .. 32767
|  contador:  N      no nulo, 0 .. 255
|  fecha:     Fecha  no nula, hora de llegada al servidor
|
--------------------------------------------------------------------
```

| Columna | Tipo MySQL | Por qué |
|---|---|---|
| id | `INT UNSIGNED AUTO_INCREMENT` | Identifica cada medida y da el orden de llegada |
| tipo | `TINYINT UNSIGNED` | Código de 1 byte, igual que en el major del iBeacon |
| valor | `SMALLINT` | 16 bits con signo, igual que el minor del iBeacon |
| contador | `TINYINT UNSIGNED` | 1 byte, igual que en el major del iBeacon |
| fecha | `DATETIME` | Permite ordenar y filtrar por fecha |

Significado de `tipo`:

| Código | Medida |
|---|---|
| 11 | CO2 |
| 12 | Temperatura |
| 13 | Ruido |

## Script de creación (`crear_bd.sql`)

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

- La "última medida" es la de id más alto, es decir, la última que ha llegado al servidor.
- Los tipos de las columnas tienen el mismo tamaño que los campos del iBeacon, así que cualquier medida que emite el Arduino cabe sin perder información.
- El `contador` se guarda para poder detectar medidas repetidas o perdidas, pero no forma parte de la clave porque vuelve a 0 después de 255.
- En este sprint hay un solo sensor, por eso no hay tablas de sensores ni de usuarios. Se añadirán en sprints posteriores.
- La restricción `CHECK` repite la validación de `Logica` como segunda barrera (MySQL la aplica desde la versión 8.0.16).
- La base de datos se crea una vez ejecutando `crear_bd.sql` en phpMyAdmin.

# Reglas Generales

- Lenguaje de programación: SQL (MySQL / MariaDB).
- El script de creación incluirá una cabecera en comentarios con: nombre, descripción, copyright, fecha, autor y aportación.
- El código SQL será lo más claro posible, con nombres de tablas y columnas autoexplicativos.
- Los tests de la base de datos están en `src/logica/test/test_logica.php`: usan una tabla equivalente en SQLite en memoria y comprueban que las medidas se guardan y se recuperan bien (incluidos negativos y valores límite) y que un fallo de la base de datos se convierte en error.
