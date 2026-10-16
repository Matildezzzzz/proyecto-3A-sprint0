# Prompt: lógica del negocio del cliente web

## Prompt usado

Eres un programador de JavaScript. Te adjunto los diseños `doc/frontend_business_logic_design.md` y `doc/business_logic_design.md`.
Genera `src/frontend_business_logic/logicaFake.js` siguiendo exactamente `frontend_business_logic_design.md`:

- Clase `LogicaFake` con `obtenerUltimaMedida()`, con la misma firma que en la lógica del negocio del servidor: devuelve una `Medida` o `null`.
- Por dentro hace un GET sin caché a `/api/mediciones.php`: 404 → `null`; otro código que no sea 2xx → error `"HTTP <código>"`.
- Recibe la función de petición en el constructor (por defecto `fetch`) para poder probarla sin red.
- JavaScript sin frameworks, exportable con `module.exports` para los tests. Sigue las Reglas Generales del diseño.
- Genera `src/frontend_business_logic/test/logicaFake.test.js` con tests para las respuestas 200, 404, 500 y sin conexión, que se ejecuten con `node --test`.

## Uso de la IA

- El código se generó con IA a partir del diseño.
- Con IA (Claude) se separó `LogicaFake` de la web en su propio componente y se escribieron sus 5 tests, siguiendo la versión 2 del agente revisor.