# Diseño del Componente

## Arquitectura del emisor

```
 Medidor --valor--> Publicador --major, minor--> EmisoraBLE --> anuncio iBeacon (BLE)
                        |
                        v
                  Codificacion  (calcularMajor, valorAMinor)
```

Ficheros: `emisor.ino`, `LED.h`, `PuertoSerie.h`, `Medidor.h`, `Codificacion.h`, `Publicador.h`, `EmisoraBLE.h`.

## Tipos

```
N        Números naturales (enteros sin signo)
Z        Enteros con signo
B        Booleanos { true, false }
Text     Texto
[T]_n    Array de longitud fija n

MedicionID = { CO2 = 11, TEMPERATURA = 12, RUIDO = 13 }
```

## Programa principal (`emisor.ino`)

### Estado global

```
el_led:        LED            (LED_BUILTIN)
el_puerto:     PuertoSerie    (115200 baudios)
el_publicador: Publicador
el_medidor:    Medidor
contador:      N              (entre 0 y 255, empieza en 0)
```

### Funciones globales

```
tiempo: Z --> esperar() -->
              inicializarPlaquita() -->
              setup() -->
              lucecitas() -->
              loop() -->
```

### Algoritmo de setup()

1. el_puerto.esperarDisponible()
2. inicializarPlaquita()
3. el_publicador.encenderEmisora()
4. el_medidor.iniciarMedidor()
5. esperar(1000)
6. el_puerto.escribir("---- setup(): fin ----")

### Algoritmo de lucecitas()

```
el_led.brillar(100)  -> esperar(400)
el_led.brillar(100)  -> esperar(400)
el_led.brillar(100)  -> esperar(400)
el_led.brillar(1000) -> esperar(1000)
```

### Algoritmo de loop()

1. contador = (contador + 1) mod 256
2. el_puerto.escribir("---- loop(): empieza " + contador)
3. lucecitas()
4. valor_co2 = el_medidor.medirCO2()
5. cabe = el_publicador.publicarCO2(valor_co2, contador, 1000)
6. Si cabe: escribir "Publicado CO2=<valor_co2> contador=<contador>".
   Si no: escribir "ERROR: el iBeacon no cabe en el anuncio".
7. Repetir indefinidamente.

## Clase LED

```
------------------------------- LED --------------------------------
|
|  numero_led: Z
|  encendido:  B
|
numero: Z --> LED() -->
              encender() -->
              apagar() -->
              alternar() -->
tiempo: Z --> brillar() -->
|
--------------------------------------------------------------------
```

- LED(numero): configura el pin como salida y apaga el LED.
- brillar(tiempo): encender(), esperar(tiempo), apagar().

## Clase PuertoSerie

```
---------------------------- PuertoSerie ----------------------------
|
|  baudios: Z
|
baudios: Z --> PuertoSerie() -->
               esperarDisponible() -->
mensaje: Text --> escribir() -->
|
--------------------------------------------------------------------
```

Algoritmo de esperarDisponible():

1. Abrir el puerto serie a `baudios`.
2. Esperar a que el ordenador lo abra, como máximo 2 segundos.

escribir(mensaje): escribe el mensaje y salta de línea.

## Clase Medidor

```
----------------------------- Medidor -------------------------------
|
               Medidor() -->
               iniciarMedidor() -->
          Z <-- medirCO2() <--
          Z <-- medirTemperatura() <--
|
--------------------------------------------------------------------
```

Medidas ficticias del prototipo:

```
medirCO2()         --> 123
medirTemperatura() --> -12
```

## Codificacion (funciones puras)

```
codigo: N, contador: N --> calcularMajor() --> N
valor: Z               --> valorAMinor()   --> N
```

Algoritmo de calcularMajor(codigo, contador):

1. Devolver (codigo mod 256) * 256 + (contador mod 256)

Algoritmo de valorAMinor(valor):

1. Devolver valor como entero de 16 bits sin signo (complemento a 2 si es negativo).

## Clase Publicador

```
---------------------------- Publicador -----------------------------
|
|  beacon_uuid: [N]_16      ("EPSG-GTI-PROY-3A" en ASCII)
|  la_emisora:  EmisoraBLE  ("GTI3A-2026", 0x004c, 4)
|  RSSI:        Z           (-53)
|
                  Publicador() -->
                  encenderEmisora() -->
|
tipo: MedicionID, valor: Z, contador: N,
tiempo_espera: Z --> publicarMedida() --> B          (privado)
|
valor_co2: Z, contador: N,
tiempo_espera: Z --> publicarCO2() --> B
|
valor_temperatura: Z, contador: N,
tiempo_espera: Z --> publicarTemperatura() --> B
|
--------------------------------------------------------------------
```

Algoritmo de publicarMedida(tipo, valor, contador, tiempo_espera):

1. major = calcularMajor(tipo, contador)
2. minor = valorAMinor(valor)
3. cabe = la_emisora.emitirAnuncioIBeacon(beacon_uuid, major, minor, RSSI)
4. esperar(tiempo_espera)
5. la_emisora.detenerAnuncio()
6. Devolver cabe

- publicarCO2(valor, contador, t) = publicarMedida(CO2, valor, contador, t)
- publicarTemperatura(valor, contador, t) = publicarMedida(TEMPERATURA, valor, contador, t)

## Clase EmisoraBLE

```
---------------------------- EmisoraBLE -----------------------------
|
|  nombre_emisora: Text
|  fabricante_id:  N
|  tx_power:       Z
|
nombre_emisora: Text, fabricante_id: N,
tx_power: Z --> EmisoraBLE() -->
                encenderEmisora() -->
           B <-- estaAnunciando() <--
                detenerAnuncio() -->
|
beacon_uuid: [N]_16, major: N,
minor: N, rssi: Z --> emitirAnuncioIBeacon() --> B
|
--------------------------------------------------------------------
```

Algoritmo de encenderEmisora():

1. Arrancar el módulo Bluetooth.
2. Configurar tx_power y nombre_emisora.

Algoritmo de detenerAnuncio():

1. Si estaAnunciando(): parar el anuncio.

Algoritmo de emitirAnuncioIBeacon(beacon_uuid, major, minor, rssi):

1. detenerAnuncio()
2. Vaciar el paquete de anuncio y la respuesta al escaneo.
3. Paquete de anuncio: flags + iBeacon (beacon_uuid, major, minor, rssi, fabricante_id).
4. Respuesta al escaneo: nombre_emisora.
5. Intervalo de anuncio 32–244 (unidades de 0,625 ms), modo rápido 30 s.
6. Empezar a anunciar sin límite de tiempo.
7. Devolver true si el iBeacon ha cabido en el paquete de anuncio.

## Formato del anuncio emitido

| Campo iBeacon | Contenido |
|---|---|
| UUID | `EPSG-GTI-PROY-3A` (16 bytes ASCII) |
| Major | Byte alto: código de la medida (11 = CO2). Byte bajo: contador |
| Minor | Valor de la medida (16 bits, con signo en complemento a 2) |
| Potencia a 1 m | -53 |
| Nombre (respuesta al escaneo) | `GTI3A-2026` |

Ejemplo: CO2 = 123 con contador 5 → major = `0B 05`, minor = `00 7B`.

# Aclaraciones del Diseño

- El valor de la medida es ficticio en este sprint. En la demo se cambia el número de `medirCO2()`, se sube el programa y el valor nuevo tiene que llegar a la web.
- El paquete de anuncio BLE admite como máximo 31 bytes: flags (3) + iBeacon (27) = 30. Por eso el nombre va en la respuesta al escaneo y no se añade TxPower al paquete principal. Si se añaden, el iBeacon deja de caber.
- Antes de cada anuncio se vacían los datos (`clearData`); si no, cada vuelta del loop añadiría otro iBeacon y dejaría de caber.
- El contador cambia en cada vuelta para que el receptor distinga una medida nueva de la repetición de la misma. Ocupa un byte, así que vuelve a 0 después de 255.
- Cada medida se anuncia durante 1000 ms; durante las lucecitas (≈2,6 s) no se emite.
- `esperarDisponible()` espera al ordenador como máximo 2 s para que la placa funcione también con batería, sin cable USB.
- La codificación está en `Codificacion.h`, sin dependencias de Arduino ni BLE, para poder probarla en el ordenador.
- Los componentes del código original que no participan en el flujo iBeacon (`ServicioEnEmisora`, `Caracteristica`) no forman parte de este componente.

# Reglas Generales

- Lenguaje de programación: C++ para Arduino (Adafruit nRF52840 con la biblioteca Bluefruit).
- El encabezado de cada función/método incluirá su diseño lógico dentro de un bloque de comentarios delimitado por líneas `--------------------`.
- El código será lo más claro y autoexplicativo posible, de modo que prácticamente no requiera comentarios adicionales.
- Se generarán tests para todas las funciones clave sin dependencias de hardware (`calcularMajor`, `valorAMinor`). Están en `src/emisor/test/test_codificacion.cpp` y se ejecutan en el ordenador con `g++ -std=c++11 test_codificacion.cpp -o test && ./test`.
- Cada fichero llevará una cabecera con: nombre, descripción, copyright, fecha, autor y aportación.