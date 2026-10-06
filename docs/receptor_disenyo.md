# Diseño del Componente

## Arquitectura del receptor
anuncio iBeacon (BLE)
        |
        v
  MainActivity (escaneo BLE + interfaz)
        |
        v
  TramaIBeacon --> InterpreteMedida --> Medida --> LogicaFake --HTTP--> servidor
        ^
        |
    Utilidades (conversiones de bytes)

Cada clase tiene una sola responsabilidad:
- `MainActivity`: permisos, escaneo BLE e interfaz. No interpreta ni envía medidas.
- `TramaIBeacon`: divide los bytes del anuncio en campos.
- `Utilidades`: convierte bytes a texto, hexadecimal o entero.
- `InterpreteMedida`: convierte una trama en una `Medida` (lógica de dominio).
- `LogicaFake`: ofrece `guardarMedida()` como si fuera la lógica del negocio y oculta la comunicación HTTP.

## Tipos
N, Z, B, Text, [T], [T]_n   (igual que en el emisor)
ResultadoEscaneo = (
    nombre:    Text,
    direccion: Text,
    rssi:      Z,
    bytes:     [Z]
)

## Clase Medida
------------------------------ Medida -------------------------------
|
|  tipo:     N
|  valor:    Z
|  contador: N
|
tipo: N, valor: Z, contador: N --> Medida() -->
|
            N <-- getTipo() <--
|
            Z <-- getValor() <--
|
            N <-- getContador() <--
|
--------------------------------------------------------------------

## Clase MainActivity
---------------------------- MainActivity ---------------------------
|
|  etiqueta_log:              Text
|  codigo_peticion_permisos:  N
|  escaner_ble:               EscanerBLE
|  escaneo_activo:            B
|  interprete:                InterpreteMedida
|  logica:                    LogicaFake
|  ultimo_contador_enviado:   Z      (-1 al empezar)
|
                 inicializarBlueTooth() -->
|
                 buscarTodosLosDispositivosBTLE() -->
|
nombre_dispositivo: Text --> buscarEsteDispositivoBTLE() -->
|
                 detenerBusquedaDispositivosBTLE() -->
|
resultado: ResultadoEscaneo --> mostrarInformacionDispositivoBTLE() -->
|
resultado: ResultadoEscaneo --> tratarMedidaRecibida() -->
|
                 onCreate() -->
|
                 botonBuscarDispositivosBTLEPulsado() -->
|
                 botonBuscarNuestroDispositivoBTLEPulsado() -->
|
                 botonDetenerBusquedaDispositivosBTLEPulsado() -->
|
codigo: N, permisos: [Text], resultados: [B]
                 --> onRequestPermissionsResult() -->
|
--------------------------------------------------------------------

Algoritmo de onCreate():
1. interprete = InterpreteMedida("EPSG-GTI-PROY-3A")
2. logica = LogicaFake(URL del servidor)
3. inicializarBlueTooth()

Algoritmo de inicializarBlueTooth():
1. Obtener el adaptador Bluetooth del teléfono.
2. Comprobar el permiso BLUETOOTH_CONNECT.
3. Habilitar el adaptador.
4. Obtener el escáner BLE.
5. Comprobar el estado de los permisos requeridos.
6. Solicitar los permisos de Bluetooth y localización que falten.

Algoritmo de buscarTodosLosDispositivosBTLE():
1. Comprobar el permiso BLUETOOTH_SCAN.
2. Iniciar el escaneo BLE sin filtro.
3. escaneo_activo = true
4. Por cada resultado recibido: mostrarInformacionDispositivoBTLE(resultado)

Algoritmo de buscarEsteDispositivoBTLE(nombre_dispositivo):
1. Crear un filtro por nombre_dispositivo.
2. Comprobar el permiso BLUETOOTH_SCAN.
3. Iniciar el escaneo BLE con el filtro y baja latencia.
4. escaneo_activo = true
5. Por cada resultado recibido:
   - mostrarInformacionDispositivoBTLE(resultado)
   - tratarMedidaRecibida(resultado)

Algoritmo de tratarMedidaRecibida(resultado):
1. trama = TramaIBeacon(resultado.bytes)
2. Si no interprete.esDeNuestroSensor(trama): terminar.
3. medida = interprete.interpretar(trama)
4. Si medida.getContador() == ultimo_contador_enviado: terminar.
5. ultimo_contador_enviado = medida.getContador()
6. logica.guardarMedida(medida)

Algoritmo de detenerBusquedaDispositivosBTLE():
1. Si no escaneo_activo: terminar.
2. Comprobar el permiso BLUETOOTH_SCAN.
3. Detener el escaneo.
4. escaneo_activo = false

Algoritmo de mostrarInformacionDispositivoBTLE(resultado):
1. Obtener nombre, dirección, RSSI y bytes.
2. trama = TramaIBeacon(resultado.bytes)
3. Escribir en el log todos los campos de la trama, convertidos con Utilidades.

Botones:
- botonBuscarDispositivosBTLEPulsado() --> buscarTodosLosDispositivosBTLE()
- botonBuscarNuestroDispositivoBTLEPulsado() --> buscarEsteDispositivoBTLE("GTI3A-2025")
- botonDetenerBusquedaDispositivosBTLEPulsado() --> detenerBusquedaDispositivosBTLE()

## Clase TramaIBeacon
--------------------------- TramaIBeacon ----------------------------
|
|  bytes:          [Z]
|  prefijo:        [Z]_9
|  uuid:           [Z]_16
|  major:          [Z]_2
|  minor:          [Z]_2
|  tx_power:       Z
|  adv_flags:      [Z]_3
|  adv_header:     [Z]_2
|  company_id:     [Z]_2
|  ibeacon_type:   Z
|  ibeacon_length: Z
|
bytes: [Z] --> TramaIBeacon() -->
|
     [Z]_9  <-- getPrefijo() <--
     [Z]_16 <-- getUUID() <--
     [Z]_2  <-- getMajor() <--
     [Z]_2  <-- getMinor() <--
     Z      <-- getTxPower() <--
     [Z]    <-- getLosBytes() <--
     [Z]_3  <-- getAdvFlags() <--
     [Z]_2  <-- getAdvHeader() <--
     [Z]_2  <-- getCompanyID() <--
     Z      <-- getiBeaconType() <--
     Z      <-- getiBeaconLength() <--
|
--------------------------------------------------------------------
Descomposición de la trama:
prefijo        = bytes[0..8]
adv_flags      = prefijo[0..2]
adv_header     = prefijo[3..4]
company_id     = prefijo[5..6]
ibeacon_type   = prefijo[7]
ibeacon_length = prefijo[8]
uuid           = bytes[9..24]
major          = bytes[25..26]
minor          = bytes[27..28]
tx_power       = bytes[29]

## Clase Utilidades
Todos los métodos son estáticos.
----------------------------- Utilidades ----------------------------
|
texto: Text                 --> stringToBytes()     --> [Z]
uuid: Text                  --> stringToUUID()      --> UUID
uuid: UUID                  --> uuidToString()      --> Text
uuid: UUID                  --> uuidToHexString()   --> Text
bytes: [Z]                  --> bytesToString()     --> Text
parte_alta: Z, parte_baja: Z --> dosLongToBytes()   --> [Z]_16
bytes: [Z]                  --> bytesToInt()        --> Z
bytes: [Z]                  --> bytesToLong()       --> Z
bytes: [Z]                  --> bytesToIntOK()      --> Z
bytes: [Z]                  --> bytesToHexString()  --> Text
|
--------------------------------------------------------------------

## Clase InterpreteMedida
------------------------- InterpreteMedida --------------------------
|
|  uuid_esperado: Text
|
uuid_esperado: Text --> InterpreteMedida() -->
|
trama: TramaIBeacon --> esDeNuestroSensor() --> B
|
trama: TramaIBeacon --> interpretar() --> Medida
|
--------------------------------------------------------------------
Algoritmo de esDeNuestroSensor(trama):
1. Devolver Utilidades.bytesToString(trama.getUUID()) == uuid_esperado

Algoritmo de interpretar(trama):
1. major = trama.getMajor()
2. tipo = major[0]          (byte alto, sin signo)
3. contador = major[1]      (byte bajo, sin signo)
4. valor = trama.getMinor() interpretado como entero de 16 bits con signo
5. Devolver Medida(tipo, valor, contador)

## Clase LogicaFake
---------------------------- LogicaFake -----------------------------
|
|  url_servidor: Text
|
url_servidor: Text --> LogicaFake() -->
|
medida: Medida --> guardarMedida() -->
|
--------------------------------------------------------------------

Algoritmo de guardarMedida(medida):
1. En un hilo distinto del de la interfaz:
2. Construir el JSON `{ "tipo": medida.tipo, "valor": medida.valor, "contador": medida.contador }`
3. Enviar `POST url_servidor + "/medida"` con ese JSON.
4. Escribir en el log el código de respuesta (201 = guardada) o el error.

# Aclaraciones del Diseño
- `LogicaFake` tiene el mismo nombre de operación que la lógica real del servidor (`guardarMedida`). Quien la usa no sabe que por debajo hay HTTP.
- `InterpreteMedida` no depende de Android, para poder probarla con tests en el ordenador.
- El mismo anuncio llega varias veces. Solo se envía una medida al servidor cuando su contador es distinto del último enviado.
- El minor se interpreta con signo para recuperar valores negativos como la temperatura (-12).
- La URL del servidor es la IP del ordenador donde corre el servidor en la red local (por ejemplo `http://192.168.1.50:8080`), nunca `localhost`, porque en el móvil `localhost` es el propio móvil.
- La app necesita los permisos BLUETOOTH_SCAN, BLUETOOTH_CONNECT, ACCESS_FINE_LOCATION e INTERNET. Si el servidor es http (no https) hay que permitir tráfico en claro en el manifiesto.

# Reglas Generales
- Lenguaje de programación: Java (Android).
- El encabezado de cada función/método incluirá su diseño lógico dentro de un bloque de comentarios delimitado por líneas `--------------------`.
- El código será lo más claro y autoexplicativo posible, de modo que prácticamente no requiera comentarios adicionales.
- Se generarán tests unitarios (JUnit) para todas las funciones y métodos clave, como mínimo para `TramaIBeacon`, `Utilidades` e `InterpreteMedida`, usando bytes de tramas conocidas.
- Cada fichero llevará una cabecera con: nombre, descripción, copyright, fecha, autor y aportación.