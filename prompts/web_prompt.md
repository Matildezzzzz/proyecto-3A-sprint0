# Prompt: web (interfaz)

## Prompt usado

Eres un programador web. Te adjunto los diseños `doc/web_design.md` y `doc/frontend_business_logic_design.md`.
Genera `src/web/index.html` y `src/web/pagina.js` siguiendo exactamente `web_design.md`:

- Pantalla con las 4 tarjetas (CO₂, Temperatura, Ruido y Contador), el panel de estado y la tabla de la última medición, con los `id` del diseño.
- Clase `Pagina`, que pide la última medida a `LogicaFake` cada 2 segundos y la pinta. No puede hacer peticiones HTTP ni usar `fetch`.
- Funciones `nombreDeTipo()`, `unidadDeTipo()` y `horaDe()`.
- `index.html` carga `../frontend_business_logic/logicaFake.js` y `pagina.js`.
- HTML, CSS y JavaScript sin frameworks, adaptado al móvil. Sigue las Reglas Generales del diseño.
- Genera `src/web/test/web.test.js` con tests de `nombreDeTipo()`, `unidadDeTipo()` y `horaDe()`, que se ejecuten con `node --test`.

## Uso de la IA

- El código se generó con IA a partir del diseño y se probó en el servidor con la demo completa.
- Con IA (Claude) se escribieron los tests de la web.