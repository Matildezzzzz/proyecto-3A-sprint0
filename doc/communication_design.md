# Diseño del Componente

## Propósito

API REST del servidor. Recibe las peticiones HTTP de la app Android y de la web, llama a la lógica del negocio (`business_logic_design.md`) y convierte su resultado en una respuesta HTTP. No valida datos ni accede a la base de datos.

Se despliega en `https://mcalgar2.upv.edu.es/api/mediciones.php`.

## Arquitectura

```
 App Android (LogicaFake) --POST--+
                                  v
                          mediciones.php      API REST (este componente)
                                  ^   |
 Web (LogicaFake) -------GET------+   v
                               Logica          lógica del negocio
```

Ficheros: `mediciones.php` y `config.ejemplo.php` (plantilla de `config.php`, que tiene la contraseña de la base de datos y no se sube al repositorio).

## Tipos

```
N, Z, B, Text   (igual que en el resto de componentes)

Medida = { id: N, tipo: N, valor: Z, contador: N, fecha: Text }   (ver business_logic_design.md)

Respuesta = { codigo: N, cuerpo: JSON }
```

## API REST

| Método | URL | Cuerpo de la petición | Llama a | Respuesta correcta |
|---|---|---|---|---|
| POST | `/api/mediciones.php` | `{"tipo":11,"valor":123,"contador":17}` | `logica.guardarMedida(tipo, valor, contador)` | 201 `{"ok":true,"mensaje":"Medición recibida","medicion":Medida}` |
| GET | `/api/mediciones.php` | — | `logica.obtenerUltimaMedida()` | 200 `Medida` |

Errores:

| Caso | Respuesta |
|---|---|
| El cuerpo no es JSON | 400 `{"ok":false,"mensaje":"JSON inválido"}` |
| La lógica lanza DatoNoValido | 400 `{"ok":false,"mensaje":"<motivo>"}` |
| GET y la lógica devuelve nulo (no hay medidas) | 404 `{"ok":false,"mensaje":"Todavía no hay mediciones"}` |
| Otro método | 405, con la cabecera `Allow: GET, POST` |
| Cualquier otro error | 500 `{"ok":false,"mensaje":"Error interno del servidor"}` |

## Funciones de mediciones.php

```
codigo: N, datos: JSON --> responder() -->
```

Algoritmo de responder(codigo, datos):

1. Poner el código de estado HTTP.
2. Escribir datos como JSON (UTF-8) y terminar.

Algoritmo de cada petición:

1. Cabecera `Content-Type: application/json; charset=utf-8`.
2. Leer `config.php`, abrir la conexión con la base de datos y crear `logica = Logica(AlmacenMediciones(bd))`.
3. Según el método:
   - GET: medida = logica.obtenerUltimaMedida(). Si es nulo: responder(404, ..). Si no: responder(200, medida).
   - POST: leer el JSON del cuerpo; si no es válido: responder(400, ..). Si lo es: medida = logica.guardarMedida(tipo, valor, contador) y responder(201, ..).
   - Otro: responder(405, ..).
4. Si la lógica lanza DatoNoValido: responder(400, motivo).
5. Si se lanza cualquier otro error: escribirlo en el log del servidor y responder(500, ..).

# Aclaraciones del Diseño

- Este componente no contiene lógica del negocio: no valida datos ni hace consultas. Solo traduce HTTP ↔ llamadas a `Logica`.
- Es el único componente que conoce HTTP: métodos, códigos de estado, cabeceras y JSON.
- Aquí se crean `Logica` y `AlmacenMediciones` con la conexión de `config.php`, porque es el punto de entrada del servidor.
- Un error 500 no devuelve el detalle del error, porque los errores de la base de datos pueden contener el usuario o el host. El detalle va al log del servidor.
- La contraseña está en `config.php`, que está en `.gitignore`. En el repositorio solo está `config.ejemplo.php`.
- La web está en el mismo servidor que la API, así que no hace falta CORS.

# Reglas Generales

- Lenguaje de programación: PHP (7.1 o superior), sin frameworks.
- El encabezado de cada función/método incluirá su diseño lógico dentro de un bloque de comentarios delimitado por líneas `--------------------`.
- El código será lo más claro y autoexplicativo posible, de modo que prácticamente no requiera comentarios adicionales.
- Se generarán tests automáticos para todas las funciones y métodos clave. Como este componente solo traduce HTTP, la lógica que hay detrás se prueba en `src/business_logic/test/test_logica.php`; la API se prueba a mano con el navegador (GET) y con la app (POST).
- Cada fichero llevará una cabecera con: nombre, descripción, copyright, fecha, autor y aportación.