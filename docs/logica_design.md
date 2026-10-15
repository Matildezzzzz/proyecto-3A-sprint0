# Diseño del Componente

## Arquitectura del servidor

```
 App Android (LogicaFake) --POST--+
                                  v
                    mediciones.php          capa API REST (solo HTTP)
                                  |
                                  v
                       Logica.php           lógica del negocio (no sabe de HTTP)
                                  |
                                  v
              AlmacenMediciones.php         acceso a datos (PDO)
                                  |
                                  v
                    Base de datos MySQL     tabla medidas (ver basedatos_design.md)
                                  ^
 Web (LogicaFake) -------GET------+  (también entra por mediciones.php)
```

Patrón de capas: cada capa solo habla con la de debajo. Se despliega en `https://mcalgar2.upv.edu.es/api/`.

Ficheros: `mediciones.php`, `Logica.php`, `AlmacenMediciones.php`, `config.ejemplo.php` (plantilla de `config.php`, que no se sube al repositorio).

## Tipos

```
N, Z, B, Text   (igual que en el resto de componentes)

Medida = {
    id:       N,
    tipo:     N,      { 11 CO2, 12 temperatura, 13 ruido }
    valor:    Z,      [-32768, 32767]
    contador: N,      [0, 255]
    fecha:    Text    (hora del servidor, ISO 8601)
}
```

## Clase AlmacenMediciones (acceso a datos)

```
------------------------- AlmacenMediciones -------------------------
|
|  bd: PDO   (conexión a la base de datos)
|
bd: PDO --> AlmacenMediciones() -->
|
medida: {tipo, valor, contador, fecha} --> insertar() --> N
|
             Medida | null <-- ultima() <--
|
--------------------------------------------------------------------
```

Algoritmo de AlmacenMediciones(bd):

1. Guardar bd y hacer que sus errores se lancen como excepciones.

Algoritmo de insertar(medida):

1. `INSERT INTO medidas (tipo, valor, contador, fecha) VALUES (?, ?, ?, ?)` con la fecha convertida a DATETIME.
2. Devolver el id que ha asignado la base de datos.

Algoritmo de ultima():

1. `SELECT id, tipo, valor, contador, fecha FROM medidas ORDER BY id DESC LIMIT 1`
2. Si no hay fila: devolver null.
3. Devolver la fila con los números como enteros y la fecha en ISO 8601.

## Clase Logica (lógica del negocio)

```
------------------------------ Logica -------------------------------
|
|  almacen: AlmacenMediciones
|  TIPOS_VALIDOS: [N]   ([11, 12, 13])
|
almacen: AlmacenMediciones --> Logica() -->
|
tipo: N, valor: Z, contador: N --> guardarMedida() --> Medida
|
             Medida | null <-- obtenerUltimaMedida() <--
|
dato: ?, nombre: Text --> entero() --> Z                (privado)
|
--------------------------------------------------------------------
```

Algoritmo de guardarMedida(tipo, valor, contador):

1. tipo, valor y contador tienen que ser enteros (entero()).
2. tipo tiene que estar en TIPOS_VALIDOS.
3. valor tiene que estar entre -32768 y 32767.
4. contador tiene que estar entre 0 y 255.
   Si algo falla: error InvalidArgumentException con el motivo.
5. fecha = fecha y hora actual.
6. id = almacen.insertar({tipo, valor, contador, fecha})
7. Devolver {id, tipo, valor, contador, fecha}.

Algoritmo de obtenerUltimaMedida():

1. Devolver almacen.ultima()

Algoritmo de entero(dato, nombre):

1. Si dato es un entero (o un texto con un entero): devolverlo como entero.
2. Si no: error InvalidArgumentException("falta un entero válido en <nombre>").

## API REST (`mediciones.php`)

| Método | URL | Cuerpo | Llama a | Respuesta |
|---|---|---|---|---|
| POST | `/api/mediciones.php` | `{"tipo":11,"valor":123,"contador":17}` | `guardarMedida()` | 201 `{"ok":true,"mensaje":"Medición recibida","medicion":{...}}` |
| GET | `/api/mediciones.php` | — | `obtenerUltimaMedida()` | 200 `{"id":1,"tipo":11,"valor":123,"contador":17,"fecha":"..."}` |

Errores:

- JSON inválido o datos no válidos → 400 `{"ok":false,"mensaje":"<motivo>"}`
- GET sin medidas → 404 `{"ok":false,"mensaje":"Todavía no hay mediciones"}`
- Otro método → 405
- Error de la base de datos o del servidor → 500 `{"ok":false,"mensaje":"Error interno del servidor"}` (el detalle va al log del servidor, no a la respuesta)

Algoritmo de cada petición:

1. Conectar con la base de datos con los datos de `config.php` y crear `Logica(AlmacenMediciones(bd))`.
2. Leer el método y, si es POST, el JSON del cuerpo.
3. Llamar al método de `Logica` que corresponde.
4. Convertir el resultado, o el error, en respuesta HTTP con el código de la tabla (`responder(codigo, datos)`).

# Aclaraciones del Diseño

- `Logica` no conoce HTTP: no lee peticiones ni pone códigos de estado. Se prueba sin servidor.
- `mediciones.php` no valida datos ni toca la base de datos: solo traduce HTTP ↔ `Logica`.
- Solo `AlmacenMediciones` sabe que hay una base de datos. Al pasar del fichero JSON a MySQL solo cambió esta clase; `Logica` y la API quedaron igual.
- Las consultas usan parámetros (`?`) para evitar inyección SQL.
- La contraseña está en `config.php`, que está en `.gitignore`. En el repositorio solo está `config.ejemplo.php`.
- La fecha la pone el servidor para que todas las medidas usen el mismo reloj.
- Una medida = un tipo + un valor, porque cada anuncio del Arduino lleva una sola medida.

# Reglas Generales

- Lenguaje de programación: PHP (7.1 o superior) con PDO, sin frameworks.
- El encabezado de cada función/método incluirá su diseño lógico dentro de un bloque de comentarios delimitado por líneas `--------------------`.
- El código será lo más claro y autoexplicativo posible, de modo que prácticamente no requiera comentarios adicionales.
- Se generarán tests automáticos para todas las funciones y métodos clave de `Logica` y `AlmacenMediciones` (guardar y recuperar, la más reciente, valores límite y negativos, datos no válidos, fallo de la base de datos), con una base de datos SQLite en memoria con la misma tabla. Se ejecutan con `php test/test_logica.php`.
- Cada fichero llevará una cabecera con: nombre, descripción, copyright, fecha, autor y aportación.
