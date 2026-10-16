# Prompt: comunicación (API REST)

## Prompt usado

Eres un programador de PHP. Te adjunto los diseños `doc/communication_design.md` y `doc/business_logic_design.md`.
Genera el código de `src/communication/` siguiendo exactamente `communication_design.md`:

- `mediciones.php`: API REST con POST (guardar una medida) y GET (última medida) en `/api/mediciones.php`, con los códigos y los cuerpos JSON de las tablas del diseño (201, 200, 400, 404, 405 y 500).
- Solo traduce HTTP a llamadas a `Logica` (`guardarMedida()` y `obtenerUltimaMedida()`). No valida datos ni hace consultas.
- Carga `Logica` con `require_once __DIR__ . '/../business_logic/Logica.php'` y la conexión desde `config.php`.
- Un error 500 no devuelve el detalle: lo escribe en el log del servidor.
- `config.ejemplo.php`: plantilla de `config.php` sin datos reales. `config.php` no se sube al repositorio.
- PHP 7.1 o superior, sin frameworks. Sigue las Reglas Generales del diseño.

## Uso de la IA

- El código se generó con IA a partir del diseño y se probó desde el navegador (GET) y desde la app (POST).
- Con IA (Claude) se separó el antiguo diseño de lógica en tres (comunicación, lógica del negocio y base de datos), siguiendo la versión 2 del agente revisor, y se movió el código a `src/communication/`.