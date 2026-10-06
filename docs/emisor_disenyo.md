# Diseño del Componente

## Arquitectura del emisor
SENSOR (valor simulado)
        |
        v
    Medidor --> Publicador --> EmisoraBLE --> anuncio iBeacon (BLE)

## Tipos
N        Números naturales (enteros sin signo)
Z        Enteros con signo
B        Booleanos { true, false }
Text     Texto
[T]      Lista de longitud variable de elementos de tipo T
[T]_n    Array de longitud fija n

MedicionID = { CO2, TEMPERATURA, RUIDO }
codigo(CO2)         = 11
codigo(TEMPERATURA) = 12
codigo(RUIDO)       = 13

## Programa principal

### Estado global
el_led:        LED
el_puerto:     PuertoSerie
el_publicador: Publicador
el_medidor:    Medidor
contador:      N          (entre 0 y 255)

### Funciones globales
tiempo: Z --> esperar() -->
              inicializarPlaquita() -->
              setup() -->
              lucecitas() -->
              loop() -->

### Algoritmo de setup()
1. el_puerto.esperarDisponible()
2. inicializarPlaquita()
3. el_publicador.encenderEmisora()
4. el_medidor.iniciarMedidor()
5. esperar(1000)
6. el_puerto.escribir("---- setup(): fin ----")

### Algoritmo de loop()
1. contador = (contador + 1) mod 256
2. el_puerto.escribir("---- loop(): empieza " + contador)
3. lucecitas()
4. valor_co2 = el_medidor.medirCO2()
5. el_publicador.publicarCO2(valor_co2, contador, 1000)
6. el_puerto.escribir("---- loop(): acaba " + contador)
7. Repetir indefinidamente.

### Algoritmo de lucecitas()
el_led.brillar(100)  -> esperar(400)
el_led.brillar(100)  -> esperar(400)
el_led.brillar(100)  -> esperar(400)
el_led.brillar(1000) -> esperar(1000)

## Clase LED
------------------------------- LED --------------------------------
|
|  numero_led: Z
|  encendido:  B
|
numero: Z --> LED() -->
|
              encender() -->
|
              apagar() -->
|
              alternar() -->
|
tiempo: Z --> brillar() -->
|
--------------------------------------------------------------------
Algoritmo de brillar(tiempo):
1. encender()
2. esperar(tiempo)
3. apagar()

## Clase PuertoSerie
---------------------------- PuertoSerie ----------------------------
|
baudios: Z --> PuertoSerie() -->
|
               esperarDisponible() -->
|
mensaje: Text --> escribir() -->
|
--------------------------------------------------------------------

## Clase Medidor
----------------------------- Medidor -------------------------------
|
|  (sin estado interno)
|
               Medidor() -->
|
               iniciarMedidor() -->
|
          Z <-- medirCO2() <--
|
          Z <-- medirTemperatura() <--
|
--------------------------------------------------------------------
Valores del prototipo (medidas ficticias):
medirCO2()         --> 123
medirTemperatura() --> -12

## Clase Publicador
---------------------------- Publicador -----------------------------
|
|  beacon_uuid: [N]_16     ("EPSG-GTI-PROY-3A")
|  la_emisora:  EmisoraBLE
|  rssi:        Z          (-53)
|
                  Publicador() -->
|
                  encenderEmisora() -->
|
codigo: N, contador: N --> calcularMajor() --> N
|
valor_co2: Z, contador: N,
tiempo_espera: Z --> publicarCO2() -->
|
valor_temperatura: Z, contador: N,
tiempo_espera: Z --> publicarTemperatura() -->
|
--------------------------------------------------------------------
Algoritmo de calcularMajor(codigo, contador):
1. Devolver codigo * 256 + contador

Algoritmo de publicarCO2(valor_co2, contador, tiempo_espera):
1. major = calcularMajor(codigo(CO2), contador)
2. minor = valor_co2
3. la_emisora.emitirAnuncioIBeacon(beacon_uuid, major, minor, rssi)
4. esperar(tiempo_espera)
5. la_emisora.detenerAnuncio()

Algoritmo de publicarTemperatura(valor_temperatura, contador, tiempo_espera):
1. major = calcularMajor(codigo(TEMPERATURA), contador)
2. minor = valor_temperatura
3. la_emisora.emitirAnuncioIBeacon(beacon_uuid, major, minor, rssi)
4. esperar(tiempo_espera)
5. la_emisora.detenerAnuncio()

## Clase EmisoraBLE
---------------------------- EmisoraBLE -----------------------------
|
|  nombre_emisora: Text    ("GTI3A-2025")
|  fabricante_id:  N       (0x004c)
|  tx_power:       Z       (4)
|
nombre_emisora: Text, fabricante_id: N,
tx_power: Z --> EmisoraBLE() -->
|
                encenderEmisora() -->
|
                detenerAnuncio() -->
|
           B <-- estaAnunciando() <--
|
beacon_uuid: [N]_16, major: N,
minor: Z, rssi: Z --> emitirAnuncioIBeacon() -->
|
--------------------------------------------------------------------
Algoritmo de emitirAnuncioIBeacon(beacon_uuid, major, minor, rssi):
1. Si estaAnunciando(): detenerAnuncio()
2. Crear el beacon con beacon_uuid, major, minor y rssi.
3. Asignar fabricante_id al beacon.
4. Configurar tx_power y nombre_emisora.
5. Configurar el intervalo de anuncio BLE.
6. Iniciar el anuncio sin límite de tiempo.

## Formato del anuncio emitido
| Campo iBeacon | Contenido |
|---|---|
| UUID | `EPSG-GTI-PROY-3A` (16 bytes ASCII) |
| Major | Byte alto: código de la medida. Byte bajo: contador |
| Minor | Valor de la medida (16 bits) |
| TxPower | -53 |
| Nombre | `GTI3A-2025` |

# Aclaraciones del Diseño
- (decisiones concretas; si no hay, pon "Ninguna")

# Reglas Generales
- Lenguaje de programación: C++
- El encabezado de cada función/método incluirá su diseño lógico dentro de un bloque de comentarios delimitado por líneas --------------------
- El código será lo más claro y autoexplicativo posible, de modo que prácticamente no requiera comentarios adicionales.
- Se generarán tests unitarios o de integración para todas las funciones y métodos clave.
- Cada fichero llevará una cabecera con: nombre, descripción, copyright, fecha, autor y aportación.