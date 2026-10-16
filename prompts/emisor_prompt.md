# Prompt: emisor (Arduino)

## Prompt usado

Eres un programador de C++ para Arduino. Te adjunto el diseño `doc/emisor_design.md`.
Genera el código de `src/emisor/` siguiendo exactamente ese diseño:

- Ficheros: `emisor.ino`, `LED.h`, `PuertoSerie.h`, `Medidor.h`, `Codificacion.h`, `Publicador.h` y `EmisoraBLE.h`, para la placa Adafruit nRF52840 con la biblioteca Bluefruit.
- Respeta los nombres de clases, métodos, parámetros y tipos del diseño, y su algoritmo paso a paso.
- El anuncio iBeacon tiene que caber en 31 bytes: flags e iBeacon en el paquete principal y el nombre `GTI3A-Matilde` en la respuesta al escaneo.
- `Codificacion.h` no puede depender de Arduino ni de BLE, para poder probarlo en el ordenador.
- Sigue las Reglas Generales del diseño: el diseño lógico de cada función en un bloque de comentarios delimitado por líneas `--------------------`, código claro y una cabecera en cada fichero (nombre, descripción, copyright, fecha, autor y aportación).
- Genera también `src/emisor/test/test_codificacion.cpp` con tests de `calcularMajor()` y `valorAMinor()` que se ejecuten con `g++ -std=c++11 test_codificacion.cpp -o test && ./test`, incluidos el contador 255/256 y los valores negativos.

## Uso de la IA

- El diseño se obtuvo por ingeniería inversa del código de la asignatura. El código se generó con IA a partir de ese diseño y se revisó probándolo en la placa con nRF Connect.
- Se corrigió que el iBeacon no cabía en el anuncio (se quitaron el nombre y el TxPower del paquete principal).
- Con IA (Claude) se revisó el nombre del beacon para que fuera único (`GTI3A-Matilde`) y coincidiera con la app.