# Diseño del Componente

## Arquitectura del receptor

```
anuncio iBeacon (BLE)
        |
        v
  MainActivity  (permisos, escaneo BLE, pantalla)
        |  datos de fabricante Apple 0x004C
        v
  InterpreteMedida --> Medida --> LogicaFake --> PeticionarioREST --HTTP POST--> servidor
```

Cada clase tiene una sola responsabilidad:

- `MainActivity`: permisos, escaneo BLE y pantalla. No interpreta bytes ni hace HTTP.
- `InterpreteMedida`: reconoce nuestro iBeacon y lo convierte en `Medida` (no depende de Android).
- `Medida`: datos de una medida.
- `LogicaFake`: ofrece `guardarMedida()` como si fuera la lógica del negocio y oculta la comunicación HTTP.
- `PeticionarioREST`: hace la petición HTTP en segundo plano (código de la asignatura).

## Tipos

```
N      Números naturales
Z      Enteros con signo
B      Booleanos { true, false }
Text   Texto
[T]    Lista de elementos de tipo T

DatosFabricante = [Z]   datos de fabricante Apple SIN el id 4C 00 (23 bytes):
    [0]      = 0x02
    [1]      = 0x15
    [2..17]  = uuid (16 caracteres ASCII)
    [18]     = tipo de medida   (byte alto del major)
    [19]     = contador         (byte bajo del major)
    [20..21] = valor            (minor, 16 bits con signo)
    [22]     = tx_power

AlTerminar = (ok: B, mensaje: Text) --> resultado() -->
```

## Clase Medida

```
------------------------------ Medida -------------------------------
|
|  tipo:     N
|  valor:    Z
|  contador: N
|
tipo: N, valor: Z, contador: N --> Medida() -->
|
                 N <-- getTipo() <--
                 Z <-- getValor() <--
                 N <-- getContador() <--
              Text <-- nombreDelTipo() <--
|
--------------------------------------------------------------------
```

nombreDelTipo(): 11 → "CO2", 12 → "Temperatura", 13 → "Ruido", otro → "Desconocido".

## Clase InterpreteMedida

```
------------------------- InterpreteMedida --------------------------
|
|  uuid_esperado: Text
|
uuid_esperado: Text --> InterpreteMedida() -->
|
datos: DatosFabricante --> esDeNuestroSensor() --> B
|
datos: DatosFabricante --> leerUUID() --> Text
|
datos: DatosFabricante --> interpretar() --> Medida
|
--------------------------------------------------------------------
```

Algoritmo de esDeNuestroSensor(datos):

1. Si datos es nulo o tiene menos de 23 bytes: devolver false.
2. Si datos[0] != 0x02 o datos[1] != 0x15: devolver false.
3. Devolver leerUUID(datos) == uuid_esperado.

Algoritmo de leerUUID(datos):

1. Devolver datos[2..17] convertidos a texto ASCII.

Algoritmo de interpretar(datos):

1. tipo = datos[18] sin signo
2. contador = datos[19] sin signo
3. valor = datos[20..21] como entero de 16 bits con signo
4. Devolver Medida(tipo, valor, contador)

## Clase LogicaFake

```
---------------------------- LogicaFake -----------------------------
|
|  url_servidor: Text   ("https://mcalgar2.upv.edu.es/api/mediciones.php")
|
url_servidor: Text --> LogicaFake() -->
|
medida: Medida --> aJSON() --> Text           (estático)
|
medida: Medida, al_terminar: AlTerminar --> guardarMedida() -->
|
--------------------------------------------------------------------
```

Algoritmo de aJSON(medida):

1. Devolver `{"tipo":<tipo>,"valor":<valor>,"contador":<contador>}`

Algoritmo de guardarMedida(medida, al_terminar):

1. PeticionarioREST.hacerPeticionREST("POST", url_servidor, aJSON(medida), respuesta)
2. Si el código de la respuesta es 201: al_terminar(true, "Medida guardada").
3. Si no: al_terminar(false, "HTTP <código>: <cuerpo>").

## Clase PeticionarioREST

```
-------------------------- PeticionarioREST -------------------------
|
metodo: Text, url: Text, cuerpo: Text,
respuesta: RespuestaREST --> hacerPeticionREST() -->
|
          B <-- doInBackground() <--          (hilo secundario)
|
correcto: B --> onPostExecute() -->            (hilo de la interfaz)
|
--------------------------------------------------------------------

RespuestaREST = (codigo: Z, cuerpo: Text) --> callback() -->
```

Algoritmo de doInBackground():

1. Abrir la conexión con el método y Content-Type JSON, tiempo máximo 10 s.
2. Si no es GET: escribir el cuerpo.
3. Leer el código y el cuerpo de la respuesta (o de la respuesta de error).
4. Devolver true si el código está entre 200 y 399. Si hay excepción: código -1 y el mensaje como cuerpo.

Algoritmo de onPostExecute(correcto):

1. respuesta.callback(codigo, cuerpo)

## Clase MainActivity

```
---------------------------- MainActivity ---------------------------
|
|  interprete:               InterpreteMedida
|  logica:                   LogicaFake
|  escaneando:               B
|  enviar_al_servidor:       B
|  ultimo_contador_enviado:  Z       (-1 al empezar)
|  dispositivos_encontrados: [Text]
|
                  onCreate() -->
vista: View --> botonBuscarNuestroDispositivoBTLEPulsado() -->
vista: View --> botonBuscarDispositivosBTLEPulsado() -->
vista: View --> botonDetenerBusquedaDispositivosBTLEPulsado() -->
           [Text] <-- permisosNecesarios() <--
                B <-- tienePermisos() <--
                  pedirPermisos() -->
codigo: N, permisos: [Text], resultados: [Z]
                  --> onRequestPermissionsResult() -->
                B <-- ubicacionActivada() <--
nombre: Text | nulo --> iniciarEscaneo() -->
resultado: ResultadoEscaneo --> procesarResultado() -->
datos: DatosFabricante --> tratarMedidaRecibida() -->
texto: Text --> mostrarEstado() -->
texto: Text, color: Z --> mostrarEnvio() -->
                  actualizarBotones() -->
                  detenerEscaneo() -->
                  onDestroy() -->
|
--------------------------------------------------------------------
```

Pantalla (`activity_main.xml`):

| Vista | id | Contenido |
|---|---|---|
| Botón | `botonBuscarNuestroDispositivoBTLE` | Escanea solo `GTI3A-2026` y activa el envío al servidor |
| Botón | `botonBuscarDispositivosBTLE` | Escanea todo lo de alrededor, sin enviar nada |
| Botón | `botonDetenerBusquedaDispositivosBTLE` | Detiene la búsqueda |
| Texto | `textoEstado` | "Buscando...", "Detenido", "Sensor encontrado ✓" o el error |
| Texto | `textoUltimaMedida` | `<tipo> = <valor> (contador <n>)`, ej. `CO2 = 123 (contador 5)` |
| Texto | `textoResultadoEnvio` | "Enviando..." (ámbar), "Enviado al servidor ✓" (verde) o "Error al enviar ✗" (rojo) |

Los botones llaman a sus métodos con `android:onClick`. Al buscar todos, los dispositivos se escriben en Logcat y el estado muestra cuántos hay.

Algoritmo de onCreate():

1. Cargar activity_main.xml y buscar sus vistas.
2. Obtener el adaptador Bluetooth.
3. Si faltan permisos: pedirPermisos().

Algoritmo de botonBuscarDispositivosBTLEPulsado(vista):

1. enviar_al_servidor = false
2. iniciarEscaneo(nulo)

Algoritmo de botonBuscarNuestroDispositivoBTLEPulsado(vista):

1. enviar_al_servidor = true
2. iniciarEscaneo("GTI3A-2026")

Algoritmo de botonDetenerBusquedaDispositivosBTLEPulsado(vista):

1. detenerEscaneo()
2. mostrarEstado("Detenido")

Algoritmo de permisosNecesarios():

1. Android 12 o posterior: BLUETOOTH_SCAN, BLUETOOTH_CONNECT.
2. Siempre: ACCESS_FINE_LOCATION.

Algoritmo de iniciarEscaneo(nombre):

1. Si faltan permisos: pedirPermisos() y terminar.
2. Si el Bluetooth o la Ubicación están apagados: avisar en pantalla y terminar.
3. Si nombre es nulo: escanear sin filtro. Si no: escanear con filtro por nombre.
4. Escaneo de baja latencia y sin límite de tiempo.
5. Por cada resultado: procesarResultado(resultado).

Algoritmo de procesarResultado(resultado):

1. Si la dirección del dispositivo no está en dispositivos_encontrados: añadirla, escribirla en Logcat y, si no enviar_al_servidor, mostrar cuántos hay.
2. Si no enviar_al_servidor: terminar.
3. datos = datos de fabricante 0x004C del resultado.
4. Si interprete.esDeNuestroSensor(datos): tratarMedidaRecibida(datos).

Algoritmo de tratarMedidaRecibida(datos):

1. medida = interprete.interpretar(datos)
2. Si medida.contador == ultimo_contador_enviado: terminar.
3. ultimo_contador_enviado = medida.contador
4. Mostrar la medida y "Enviando..." en pantalla.
5. logica.guardarMedida(medida, al_terminar), que muestra "Enviado al servidor ✓" o "Error al enviar ✗".

Algoritmo de actualizarBotones():

1. Si escaneando: solo "Detener búsqueda" activo.
2. Si no: los dos botones de buscar activos y "Detener búsqueda" desactivado.

Algoritmo de detenerEscaneo():

1. Si se está escaneando: detener el escaneo.
2. escaneando = false
3. actualizarBotones()

# Aclaraciones del Diseño

- `ScanRecord.getManufacturerSpecificData()` devuelve los datos sin los 2 bytes del id del fabricante (4C 00), por eso la trama empieza por 02 15 y mide 23 bytes.
- El UUID se compara como texto ASCII (`EPSG-GTI-PROY-3A`), no como UUID estándar con guiones.
- El valor se lee con signo para poder recibir medidas negativas (temperatura -12).
- El mismo anuncio llega muchas veces por segundo: solo se envía al servidor cuando cambia el contador.
- El nombre `GTI3A-2026` debe ir en la respuesta al escaneo del Arduino (`ScanResponse.addName()`), porque en el anuncio principal no cabe junto al iBeacon (31 bytes como máximo). Android junta ambos paquetes, así que el filtro por nombre lo encuentra.
- El escaneo no tiene límite de tiempo para que en la demo cualquier medida nueva se reciba sin volver a pulsar.
- El permiso de ubicación es obligatorio en todas las versiones de Android y `BLUETOOTH_SCAN` NO lleva `neverForLocation`: con ese flag Android filtra los iBeacons de los resultados.
- El servidor es `https://mcalgar2.upv.edu.es/api/mediciones.php` (constante `URL_SERVIDOR`). El manifiesto necesita el permiso `INTERNET`.
- `LogicaFake.guardarMedida()` se llama desde el hilo de la interfaz porque `PeticionarioREST` es un `AsyncTask`.

# Reglas Generales

- Lenguaje de programación: Java (Android).
- El encabezado de cada función/método incluirá su diseño lógico dentro de un bloque de comentarios delimitado por líneas `--------------------`.
- El código será lo más claro y autoexplicativo posible, de modo que prácticamente no requiera comentarios adicionales.
- Se generarán tests unitarios (JUnit) para todas las funciones y métodos clave: `InterpreteMedida`, `Medida.nombreDelTipo()` y `LogicaFake.aJSON()`, usando tramas iBeacon conocidas (incluida una trama real del Arduino). Se ejecutan con `./gradlew test`.
- Cada fichero llevará una cabecera con: nombre, descripción, copyright, fecha, autor y aportación.
