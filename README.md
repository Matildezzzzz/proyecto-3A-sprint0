# Proyecto 3A · Sprint 0

Prototipo de extremo a extremo del proyecto de monitorización ambiental (GTI, EPS Gandia).

Una placa Arduino emite una medida ficticia por Bluetooth LE. Un móvil Android la recibe y la envía al servidor. El servidor la guarda en una base de datos y una página web muestra la última medida guardada.

```
Arduino --BLE iBeacon--> App Android --HTTP POST--> API REST --> Lógica del negocio --> MySQL
                                                       ^
Web --> Lógica del negocio del cliente --HTTP GET------+
```

Autora: Matilde Calleja García (ver `author.md`).

## Estructura del repositorio

| Componente | Diseño | Código | Lenguaje |
|---|---|---|---|
| Emisor (Arduino) | `doc/emisor_design.md` | `src/emisor/` | C++ (Adafruit nRF52840, Bluefruit) |
| Receptor (Android) | `doc/receptor_design.md` | `src/receptor/App/` | Java |
| Comunicación (API REST) | `doc/communication_design.md` | `src/communication/` | PHP |
| Lógica del negocio | `doc/business_logic_design.md` | `src/business_logic/` | PHP + PDO |
| Base de datos | `doc/database_design.md` | `src/database/` | SQL (MySQL / MariaDB) |
| Lógica del negocio del cliente web | `doc/frontend_business_logic_design.md` | `src/frontend_business_logic/` | JavaScript |
| Web (interfaz) | `doc/web_design.md` | `src/web/` | HTML, CSS y JavaScript |

Los prompts usados para generar cada componente con IA a partir de su diseño están en `prompts/`.

## Cómo desplegarlo

### 1. Base de datos

En phpMyAdmin, dentro de la base de datos del proyecto, ejecuta `src/database/crear_bd.sql` en la pestaña SQL. Solo hay que hacerlo una vez.

### 2. Servidor (Plesk)

1. Copia `src/communication/config.ejemplo.php` como `config.php`, en la misma carpeta, y rellena los datos de tu base de datos. `config.php` no se sube a GitHub.
2. Sube al servidor (`https://mcalgar2.upv.edu.es/`) manteniendo las carpetas al mismo nivel:

   | Del repositorio | Al servidor |
   |---|---|
   | `src/communication/` (con `config.php`) | `api/` |
   | `src/business_logic/` (sin `test/`) | `business_logic/` |
   | `src/frontend_business_logic/logicaFake.js` | `frontend_business_logic/` |
   | `src/web/index.html` y `src/web/pagina.js` | `web/` |

3. Comprueba la API abriendo `https://mcalgar2.upv.edu.es/api/mediciones.php`. Tiene que devolver un JSON: la última medida, o un 404 si todavía no hay ninguna.
4. La web queda en `https://mcalgar2.upv.edu.es/web/`.

### 3. Emisor (Arduino)

1. Abre `src/emisor/emisor.ino` con el IDE de Arduino y la placa Adafruit nRF52840 (biblioteca Bluefruit).
2. Sube el programa. Por el monitor serie (115200 baudios) se ve cada medida publicada.

La placa se anuncia como **`GTI3A-Matilde`**, con el UUID `EPSG-GTI-PROY-3A`.

### 4. Receptor (Android)

1. Abre la carpeta `src/receptor/App` con Android Studio y sincroniza Gradle (*File → Sync Project with Gradle Files*).
2. Ejecuta la configuración `app` en un móvil con Bluetooth LE.
3. Acepta los permisos de Bluetooth y Ubicación, y activa los dos en el móvil.
4. Pulsa **Buscar nuestro dispositivo**. La app muestra la medida recibida y si se ha enviado al servidor. El botón "Buscar todos" solo lista dispositivos y no envía nada.

La URL del servidor está en `MainActivity.java` (`URL_SERVIDOR`).

## Demo: cambiar la medida ficticia

1. En `src/emisor/Medidor.h`, cambia el número que devuelve `medirCO2()` (por defecto `123`). Puede ser cualquier entero entre -32768 y 32767.
2. Sube el programa a la placa.
3. Con la app buscando nuestro dispositivo, abre la web: en unos segundos aparece el valor nuevo en la tarjeta CO₂ y en la tabla. El contador cambia con cada medida recibida.

## Tests

Todos los tests se ejecutan en el ordenador, sin el servidor y sin la placa.

| Componente | Desde la carpeta | Comando | Qué prueba |
|---|---|---|---|
| Emisor | `src/emisor/test` | `g++ -std=c++11 test_codificacion.cpp -o test && ./test` | Codificación del major y el minor del iBeacon |
| Receptor | `src/receptor/App` | `./gradlew connectedAndroidTest` (con el móvil conectado) | `InterpreteMedida`, `Medida` y `LogicaFake.aJSON` con tramas iBeacon reales |
| Lógica del negocio y base de datos | `src/business_logic` | `php test/test_logica.php` | Guardar y recuperar medidas, límites, datos no válidos y fallos de la base de datos (SQLite en memoria) |
| Lógica del negocio del cliente web | `src/frontend_business_logic` | `node --test` | `obtenerUltimaMedida()` con respuestas 200, 404 y 500 simuladas y sin conexión |
| Web | `src/web` | `node --test` | `nombreDeTipo()`, `unidadDeTipo()` y `horaDe()` |

Los tests de Android también se pueden ejecutar desde Android Studio: clic derecho en `InterpreteMedidaTest` → **Run**.

## Criterio de aceptación del Sprint 0

Se cambia la medida ficticia en el código del Arduino, se sube a la placa, y la misma medida aparece en la página web después de pasar por el móvil, la API REST, la lógica del negocio y la base de datos.

## Ramas

- `main`: versiones entregadas.
- `develop`: desarrollo.