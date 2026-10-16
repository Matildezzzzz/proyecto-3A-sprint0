-- ---------------------------------------------------------------------
-- Fichero:     crear_bd.sql
-- Descripción: Crea la tabla de medidas de AppBiom (MySQL / MariaDB).
--              Se ejecuta una vez en phpMyAdmin (pestaña SQL) dentro de
--              la base de datos del proyecto.
-- Copyright:   (c) 2026 Matilde
-- Fecha:       14/10/2026
-- Autor:       Matilde
-- Aportación:  Diseño propio; código generado con IA y revisado.
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS medidas (
    id        INT UNSIGNED      NOT NULL AUTO_INCREMENT,
    tipo      TINYINT UNSIGNED  NOT NULL,   -- 11 CO2, 12 temperatura, 13 ruido
    valor     SMALLINT          NOT NULL,   -- -32768 .. 32767
    contador  TINYINT UNSIGNED  NOT NULL,   -- 0 .. 255
    fecha     DATETIME          NOT NULL,   -- hora de llegada al servidor
    PRIMARY KEY (id),
    CONSTRAINT chk_medidas_tipo CHECK (tipo IN (11, 12, 13))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
