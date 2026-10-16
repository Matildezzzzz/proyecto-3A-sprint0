# Diseño del Componente

## Propósito

Lógica del negocio del cliente web. La interfaz gráfica (`web_design.md`) la usa como si fuera la lógica del negocio del servidor (`business_logic_design.md`): le ofrece los mismos métodos, con la misma firma. Por dentro es un proxy ("fake") que hace la petición a la API REST del servidor (`communication_design.md`).

## Arquitectura

```
   Pagina (web)                       interfaz gráfica
        |
        |  obtenerUltimaMedida()      misma firma que en el servidor
        v
   LogicaFake (logicaFake.js)         este componente
        |
        |  GET /api/mediciones.php
        v
   API REST del servidor              (communication_design.md)
```

Fichero: `logicaFake.js`.

## Tipos

```
N      Números naturales
Z      Enteros con signo
Text   Texto

Medida = {
    id:       N,
    tipo:     N,      { 11 CO2, 12 temperatura, 13 ruido }
    valor:    Z,      [-32768, 32767]
    contador: N,      [0, 255]
    fecha:    Text    (ISO 8601)
}
```

`Medida` es el mismo tipo que en `business_logic_design.md`.

## Métodos que replica de la lógica del negocio del servidor

| Método del servidor (`business_logic_design.md`) | Método del cliente | ¿Se usa en la web? |
|---|---|---|
| `Medida \| nulo <-- obtenerUltimaMedida() <--` | `Medida \| nulo <-- obtenerUltimaMedida() <--` | Sí |
| `tipo: N, valor: Z, contador: N --> guardarMedida() --> Medida` | — | No (solo lo usa la app Android) |

## Clase LogicaFake

```
---------------------------- LogicaFake -----------------------------
|
|  urlServidor:   Text                ("/api/mediciones.php")
|  hacerPeticion: FuncionPeticion     (fetch del navegador; se sustituye en los tests)
|
urlServidor: Text, hacerPeticion: FuncionPeticion --> constructor() -->
|
             Medida | nulo <-- obtenerUltimaMedida() <--
|
--------------------------------------------------------------------
```

Algoritmo de constructor(urlServidor, hacerPeticion):

1. Guardar urlServidor.
2. Si no se da hacerPeticion: usar `fetch` del navegador.

Algoritmo de obtenerUltimaMedida():

1. Hacer un GET a urlServidor, sin caché.
2. Si la respuesta es 404 (todavía no hay medidas): devolver nulo.
3. Si es otro código que no sea 2xx: error "HTTP <código>".
4. Si no: devolver el JSON recibido como `Medida`.
5. Si no hay conexión, el error de la petición llega tal cual a quien llama.

# Aclaraciones del Diseño

- La interfaz gráfica nunca hace peticiones HTTP: solo llama a `obtenerUltimaMedida()`. Toda la comunicación con el servidor está dentro de este componente.
- La firma de `obtenerUltimaMedida()` es la misma que en el servidor. En JavaScript la llamada es asíncrona: el método devuelve una promesa que se resuelve con `Medida` o con nulo (`null`).
- Solo replica `obtenerUltimaMedida()` porque es el único método que usa la web. `guardarMedida()` lo usa la app Android, que tiene su propia `LogicaFake` (ver `receptor_design.md`).
- La URL es relativa (`/api/mediciones.php`) porque la web y la API están en el mismo servidor.
- `hacerPeticion` se recibe en el constructor para poder probar el componente sin red.

# Reglas Generales

- Lenguaje de programación: JavaScript (navegador), sin frameworks.
- El encabezado de cada función/método incluirá su diseño lógico dentro de un bloque de comentarios delimitado por líneas `--------------------`.
- El código será lo más claro y autoexplicativo posible, de modo que prácticamente no requiera comentarios adicionales.
- Se generarán tests automáticos para todas las funciones y métodos clave: `obtenerUltimaMedida()` con respuestas 200, 404 y 500 simuladas y sin conexión. Están en `src/frontend_business_logic/test/logicaFake.test.js` y se ejecutan desde `src/frontend_business_logic` con `node --test`.
- Cada fichero llevará una cabecera con: nombre, descripción, copyright, fecha, autor y aportación.