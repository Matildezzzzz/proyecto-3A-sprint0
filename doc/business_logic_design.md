# Diseño del Componente

## Propósito

Lógica del negocio del servidor: valida las medidas que llegan, las guarda y devuelve la última. No conoce la capa de comunicación (ver `communication_design.md`): solo recibe y devuelve tipos del dominio.

Guarda los datos en la tabla `medidas` definida en `database_design.md`.

## Arquitectura

```
   (capa de comunicación)
            |
            v
       Logica              lógica del negocio: valida y decide
            |
            v
   AlmacenMediciones       acceso a datos: consultas a la tabla medidas
            |
            v
     tabla medidas         (database_design.md)
```

Ficheros: `Logica.php`, `AlmacenMediciones.php`.

## Tipos

```
N      Números naturales
Z      Enteros con signo
B      Booleanos { true, false }
Text   Texto

TipoMedida = N  { 11 CO2, 12 temperatura, 13 ruido }

Medida = {
    id:       N,           columna medidas.id
    tipo:     TipoMedida,  columna medidas.tipo
    valor:    Z,           columna medidas.valor     [-32768, 32767]
    contador: N,           columna medidas.contador  [0, 255]
    fecha:    Text         columna medidas.fecha     (ISO 8601)
}

MedidaNueva = { tipo: TipoMedida, valor: Z, contador: N, fecha: Text }
```

Cada campo de `Medida` corresponde a una columna de la tabla `medidas` con el mismo nombre.

## Clase AlmacenMediciones (acceso a datos)

```
------------------------- AlmacenMediciones -------------------------
|
|  bd: ConexionBD
|
bd: ConexionBD --> AlmacenMediciones() -->
|
medida: MedidaNueva --> insertar() --> N
|
             Medida | nulo <-- ultima() <--
|
--------------------------------------------------------------------
```

Algoritmo de AlmacenMediciones(bd):

1. Guardar bd y hacer que sus errores se lancen como excepciones.

Algoritmo de insertar(medida):

1. Insertar en `medidas` las columnas `tipo`, `valor`, `contador` y `fecha` (la fecha convertida a DATETIME):
   `INSERT INTO medidas (tipo, valor, contador, fecha) VALUES (?, ?, ?, ?)`
2. Devolver el `id` que ha asignado la base de datos.

Algoritmo de ultima():

1. Leer la fila con el `id` más alto:
   `SELECT id, tipo, valor, contador, fecha FROM medidas ORDER BY id DESC LIMIT 1`
2. Si no hay ninguna fila: devolver nulo.
3. Devolver la fila como `Medida`, con los números como enteros y la fecha en ISO 8601.

## Clase Logica (lógica del negocio)

```
------------------------------ Logica -------------------------------
|
|  almacen: AlmacenMediciones
|  TIPOS_VALIDOS: [TipoMedida]   ([11, 12, 13])
|
almacen: AlmacenMediciones --> Logica() -->
|
tipo: N, valor: Z, contador: N --> guardarMedida() --> Medida
|
             Medida | nulo <-- obtenerUltimaMedida() <--
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
   Si algo falla: error DatoNoValido con el motivo.
5. fecha = fecha y hora actual.
6. id = almacen.insertar({tipo, valor, contador, fecha})
7. Devolver {id, tipo, valor, contador, fecha}.

Algoritmo de obtenerUltimaMedida():

1. Devolver almacen.ultima()

Algoritmo de entero(dato, nombre):

1. Si dato es un entero, o un texto que contiene un entero: devolverlo como entero.
2. Si no (falta, tiene decimales, es un booleano o no es un número): error DatoNoValido("falta un entero válido en <nombre>").

# Aclaraciones del Diseño

- `Logica` no conoce la capa de comunicación: no lee peticiones ni devuelve respuestas. Se usa y se prueba sin servidor.
- Solo `AlmacenMediciones` sabe que hay una base de datos. Si cambia la base de datos, solo cambia esta clase.
- Los límites de `valor` y `contador` son los de la tabla `medidas` (SMALLINT y TINYINT UNSIGNED), que a su vez son los del iBeacon del Arduino.
- `TIPOS_VALIDOS` coincide con la restricción `chk_medidas_tipo` de la tabla `medidas`. La tabla repite la comprobación como segunda barrera.
- La fecha la pone la lógica del servidor, no el móvil, para que todas las medidas usen el mismo reloj.
- La "última medida" es la de `id` más alto, es decir, la última que ha llegado.
- Las consultas usan parámetros (`?`) para evitar inyección SQL.
- `DatoNoValido` se implementa en PHP como `InvalidArgumentException`. Un error de la base de datos se lanza como `PDOException`.
- `ConexionBD` se implementa en PHP como un objeto `PDO`.

# Reglas Generales

- Lenguaje de programación: PHP (7.1 o superior) con PDO, sin frameworks.
- El encabezado de cada función/método incluirá su diseño lógico dentro de un bloque de comentarios delimitado por líneas `--------------------`.
- El código será lo más claro y autoexplicativo posible, de modo que prácticamente no requiera comentarios adicionales.
- Se generarán tests automáticos para todas las funciones y métodos clave de `Logica` y `AlmacenMediciones`: guardar y recuperar, la más reciente, valores límite y negativos, datos no válidos y fallo de la base de datos. Usan una base de datos SQLite en memoria con la misma tabla `medidas`. Están en `src/business_logic/test/test_logica.php` y se ejecutan desde `src/business_logic` con `php test/test_logica.php`.
- Cada fichero llevará una cabecera con: nombre, descripción, copyright, fecha, autor y aportación.