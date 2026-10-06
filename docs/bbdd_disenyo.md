# Diseño del Componente

## Propósito
Guardar de forma persistente las medidas recibidas por el servidor. Solo la clase `Logica` (ver `logica_design.md`) accede a esta base de datos.

## Modelo
------------------------------ medidas ------------------------------
|
|  id:        N      clave primaria, autoincremental
|  tipo:      N      no nulo, uno de { 11, 12, 13 }
|  valor:     Z      no nulo
|  contador:  N      no nulo, entre 0 y 255
|  fecha:     Text   no nulo, fecha y hora ISO 8601
|
--------------------------------------------------------------------

## Significado de `tipo`
| Código | Medida |
|---|---|
| 11 | CO2 |
| 12 | Temperatura |
| 13 | Ruido |

## Script de creación (`src/basedatos/crear_bd.sql`)
sql
CREATE TABLE IF NOT EXISTS medidas (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo      INTEGER NOT NULL CHECK (tipo IN (11, 12, 13)),
    valor     INTEGER NOT NULL,
    contador  INTEGER NOT NULL CHECK (contador BETWEEN 0 AND 255),
    fecha     TEXT    NOT NULL
);

## Consultas que usa la lógica
Insertar una medida:
sql
INSERT INTO medidas (tipo, valor, contador, fecha) VALUES (?, ?, ?, ?);

Obtener la última medida:
sql
SELECT id, tipo, valor, contador, fecha FROM medidas ORDER BY id DESC LIMIT 1;

# Aclaraciones del Diseño
- La "última medida" es la de id más alto, es decir, la última que ha llegado al servidor.
- El `contador` se guarda para poder detectar medidas repetidas o perdidas, pero no forma parte de la clave, porque vuelve a 0 después de 255.
- En este sprint hay un solo sensor, por eso no hay tabla de sensores ni de usuarios. Se añadirán en sprints posteriores.
- Las restricciones `CHECK` repiten la validación de `Logica` como segunda barrera de seguridad.

# Reglas Generales
- Lenguaje de programación: SQL (SQLite).
- El script de creación incluirá una cabecera en comentarios con: nombre, descripción, copyright, fecha, autor y aportación.
- El código SQL será lo más claro posible, con nombres de tablas y columnas autoexplicativos.
- Se generarán tests automáticos que comprueben que el script crea la tabla y que las restricciones rechazan datos no válidos (tipo inexistente, contador fuera de rango, campos nulos).