# Prompt: receptor (Android)

## Prompt usado

Eres un programador de Android en Java. Te adjunto el diseño `doc/receptor_design.md`.
Genera el código de la app en `src/receptor/App/app/src/main/java/com/example/calleja/appbiom/` siguiendo exactamente ese diseño:

- Clases: `MainActivity`, `InterpreteMedida`, `Medida`, `LogicaFake`, `PeticionarioREST`, `TramaIBeacon` y `Utilidades`, con los nombres, métodos y tipos del diseño.
- `MainActivity` solo gestiona permisos, escaneo BLE y pantalla (`activity_main.xml`). No interpreta bytes ni hace HTTP.
- `InterpreteMedida` no puede depender de Android, para poder probarla con JUnit.
- `LogicaFake` oculta la petición HTTP (POST a `https://mcalgar2.upv.edu.es/api/mediciones.php`) y se usa como si fuera la lógica del negocio.
- Permisos de Bluetooth y Ubicación para Android 12 o posterior, sin `neverForLocation`.
- Sigue las Reglas Generales del diseño: el diseño lógico de cada método en un bloque de comentarios delimitado por líneas `--------------------`, código claro y una cabecera en cada fichero.
- Genera tests JUnit de `InterpreteMedida`, `Medida.nombreDelTipo()` y `LogicaFake.aJSON()` con tramas iBeacon conocidas, incluida una trama real del Arduino.

## Uso de la IA

- El código se generó con IA a partir del diseño y se probó en el móvil con la placa.
- Con IA (Claude) se resolvió un problema del repositorio: se había perdido el módulo `app/` y su `build.gradle` al mover carpetas. Se recuperó desde un commit anterior y se añadió `build/` al `.gitignore`.