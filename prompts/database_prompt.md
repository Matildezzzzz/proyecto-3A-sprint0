# Prompt: base de datos

## Prompt usado

Eres un diseñador de bases de datos MySQL. Te adjunto el diseño `doc/database_design.md`.
Genera `src/database/crear_bd.sql` siguiendo exactamente ese diseño:

- Tabla `medidas` con las columnas, tipos, clave primaria y restricciones del diseño (`id`, `tipo`, `valor`, `contador` y `fecha`, todas `NOT NULL`, con `CHECK (tipo IN (11, 12, 13))`).
- Motor InnoDB y `utf8mb4`.
- El script tiene que poder ejecutarse en phpMyAdmin (`CREATE TABLE IF NOT EXISTS`).
- Una cabecera en comentarios con nombre, descripción, copyright, fecha, autor y aportación.

## Uso de la IA

- El script se generó con IA a partir del diseño y se ejecutó en phpMyAdmin.
- Con IA (Claude) se reescribió el diseño en el formato TABLE, DESCRIPTION, COLUMNS, PRIMARY KEY, FOREIGN KEYS y CONSTRAINTS que pide el agente revisor v2.