# Diseño del Componente

## Arquitectura del servidor
 Android (LogicaFake) --HTTP--+
                              |
                              v
                        ServidorREST        (capa de acceso remoto)
                              |
                              v
                           Logica           (lógica del negocio)
                              |
                              v
                        Base de datos       (ver basedatos_design.md)
                              ^
                              |
 Web (LogicaFake) ----HTTP----+  (también entra por ServidorREST)

Patrón de capas: cada capa solo habla con la de debajo.

## Tipos
N, Z, B, Text, [T]   (igual que en el resto de componentes)

Medida = (
    id:       N,
    tipo:     N,      { 11, 12, 13 }
    valor:    Z,
    contador: N,      [0, 255]
    fecha:    Text    (fecha y hora de llegada al servidor, formato ISO 8601)
)

NuevaMedida = (
    tipo:     N,
    valor:    Z,
    contador: N
)

## Clase Logica
------------------------------ Logica -------------------------------
|
|  conexion: ConexionBD
|
nombre_bd: Text --> Logica() -->
|
nueva: NuevaMedida --> guardarMedida() --> N
|
            Medida | vacío <-- obtenerUltimaMedida() <--
|
                   cerrar() -->
|
--------------------------------------------------------------------

Algoritmo de Logica(nombre_bd):
1. conexion = abrir la base de datos nombre_bd.

Algoritmo de guardarMedida(nueva):
1. Si nueva.tipo no está en { 11, 12, 13 }: error "tipo no válido".
2. Si nueva.contador no está entre 0 y 255: error "contador no válido".
3. Si nueva.valor no es un entero: error "valor no válido".
4. fecha = fecha y hora actual.
5. Insertar (tipo, valor, contador, fecha) en la tabla `medidas`.
6. Devolver el id asignado.

Algoritmo de obtenerUltimaMedida():
1. Buscar en `medidas` la fila con el id más alto.
2. Si no hay ninguna: devolver vacío.
3. Devolver esa fila como Medida.

Algoritmo de cerrar():
1. Cerrar conexion.

## Clase ServidorREST
--------------------------- ServidorREST ----------------------------
|
|  puerto: N
|  logica: Logica
|
puerto: N, logica: Logica --> ServidorREST() -->
|
             arrancar() -->
|
             detener() -->
|
--------------------------------------------------------------------

## API REST
| Método | Ruta | Cuerpo de la petición | Llama a | Respuesta |
|---|---|---|---|---|
| POST | `/medida` | `{ "tipo": 11, "valor": 123, "contador": 5 }` | `logica.guardarMedida()` | 201 `{ "id": 7 }` |
| GET | `/medida/ultima` | (ninguno) | `logica.obtenerUltimaMedida()` | 200 `{ "id": 7, "tipo": 11, "valor": 123, "contador": 5, "fecha": "2026-10-06T10:00:00Z" }` |

Errores:
- POST con datos no válidos --> 400 `{ "error": "<mensaje>" }`
- GET sin ninguna medida guardada --> 404 `{ "error": "no hay medidas" }`
- Error interno --> 500 `{ "error": "<mensaje>" }`

Algoritmo de cada petición:
1. Leer los datos de la petición HTTP.
2. Llamar al método correspondiente de `logica`.
3. Convertir el resultado (o el error) en respuesta HTTP con el código de la tabla.

# Aclaraciones del Diseño
- `Logica` no conoce HTTP, rutas, peticiones ni códigos de estado. Se puede usar y probar sin servidor.
- `ServidorREST` no contiene lógica del negocio: no valida datos ni accede a la base de datos, solo traduce HTTP a llamadas a `Logica`.
- La fecha la pone el servidor, no el móvil, para que todas las medidas usen el mismo reloj.
- El servidor activa CORS para que la página web pueda llamar a la API desde otro origen.
- El servidor escucha en todas las interfaces de red (`0.0.0.0`) para que el móvil pueda conectarse desde la red local.

# Reglas Generales
- Lenguaje de programación: JavaScript (Node.js), con Express para el servidor REST y SQLite como base de datos.
- El encabezado de cada función/método incluirá su diseño lógico dentro de un bloque de comentarios delimitado por líneas `--------------------`.
- El código será lo más claro y autoexplicativo posible, de modo que prácticamente no requiera comentarios adicionales.
- Se generarán tests automáticos para todas las funciones y métodos clave:
  - Tests unitarios de `Logica` con una base de datos temporal (guardar una medida y recuperarla, rechazar datos no válidos, base de datos vacía).
  - Tests de integración de la API REST (POST correcto, POST con error, GET con y sin medidas).
  - Los tests se ejecutan con `npm test`.
- Cada fichero llevará una cabecera con: nombre, descripción, copyright, fecha, autor y aportación.