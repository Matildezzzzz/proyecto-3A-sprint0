# Prompt: lógica del negocio

## Prompt usado

Eres un programador de PHP. Te adjunto los diseños `doc/business_logic_design.md` y `doc/database_design.md`.
Genera el código de `src/business_logic/` siguiendo exactamente `business_logic_design.md`:

- `Logica.php`: `guardarMedida(tipo, valor, contador)` y `obtenerUltimaMedida()`, con las validaciones del diseño (tipos 11, 12 y 13; valor entre -32768 y 32767; contador entre 0 y 255). Los datos no válidos lanzan `InvalidArgumentException`.
- `AlmacenMediciones.php`: acceso a la tabla `medidas` de `database_design.md` con PDO y consultas con parámetros (`?`).
- La lógica no puede tener ninguna dependencia de la capa de comunicación ni de HTTP.
- Sigue las Reglas Generales del diseño: el diseño lógico de cada método en un bloque de comentarios delimitado por líneas `--------------------`, código claro y una cabecera en cada fichero.
- Genera `src/business_logic/test/test_logica.php` con tests de `Logica` y `AlmacenMediciones` sobre una base de datos SQLite en memoria con la misma tabla: guardar y recuperar, la más reciente, valores límite y negativos, datos no válidos y fallo de la base de datos. Se ejecuta con `php test/test_logica.php`.

## Uso de la IA

- El código se generó con IA a partir del diseño.
- Con IA (Claude) se generaron los 24 tests de `test_logica.php`. Al ejecutarlos se detectó que `Logica` aceptaba el tipo 14, que la tabla rechaza, y se corrigió `TIPOS_VALIDOS` a `[11, 12, 13]`.
- Con IA (Claude) se reescribió el diseño para que no mencionara HTTP, como exige el agente revisor v2.