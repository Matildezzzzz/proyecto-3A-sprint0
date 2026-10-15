# Diseño del Componente

## Arquitectura de la web

```
 index.html (pantalla)
        |
        v
   Pagina (pagina.js) --> LogicaFake (logicaFake.js) --GET /api/mediciones.php--> servidor
```

- `Pagina`: decide qué se muestra. No sabe nada de HTTP.
- `LogicaFake`: ofrece `obtenerUltimaMedida()` como si fuera la lógica del negocio y oculta la comunicación HTTP.

## Tipos

```
N, Z, B, Text   (igual que en el resto de componentes)

Medida = { id: N, tipo: N, valor: Z, contador: N, fecha: Text }
```

## Pantalla (`index.html`)

- Cabecera con la píldora de estado (`estadoPildora`, `textoPildora`).
- 4 tarjetas: CO₂ (`co2`, ppm), Temperatura (`temperatura`, °C), Ruido (`ruido`, dB) y Contador (`contador`).
- Panel "Estado del sistema": `iconoEstado`, `estadoPrincipal`, `detalleEstado`, `fechaMedicion`.
- Panel "Última medición recibida": tabla con `tablaTipo`, `tablaValor`, `tablaContador`, `tablaHora`.

## Clase LogicaFake

```
---------------------------- LogicaFake -----------------------------
|
|  urlServidor:   Text      ("/api/mediciones.php")
|  hacerPeticion: fetch     (se sustituye en los tests)
|
urlServidor: Text, hacerPeticion: fetch --> constructor() -->
|
         Medida | null <-- obtenerUltimaMedida() <--      (asíncrono)
|
--------------------------------------------------------------------
```

Algoritmo de obtenerUltimaMedida():

1. GET urlServidor (sin caché).
2. 404 → devolver null.
3. Otro código que no sea 2xx → error "HTTP <código>".
4. Devolver el JSON recibido como Medida.

## Funciones de pagina.js

```
codigo: N    --> nombreDeTipo() --> Text      11 CO₂, 12 Temperatura, 13 Ruido, otro Desconocido
codigo: N    --> unidadDeTipo() --> Text      11 ppm, 12 °C, 13 dB, otro ""
fechaISO: Text --> horaDe()     --> Text      hora local, o "--" si no es válida
```

## Clase Pagina

```
------------------------------ Pagina -------------------------------
|
|  logica:      LogicaFake
|  intervaloMs: N
|
logica: LogicaFake, intervaloMs: N --> constructor() -->
|
                  iniciar() -->
                  mostrarUltimaMedida() -->                 (asíncrono)
medida: Medida --> pintarMedida() -->
tipo, titulo, detalle, icono, etiqueta: Text --> cambiarEstado() -->
id: Text, texto: Text --> escribir() -->
|
--------------------------------------------------------------------
```

Algoritmo de iniciar():

1. mostrarUltimaMedida()
2. Repetir mostrarUltimaMedida() cada intervaloMs (2000 ms).

Algoritmo de mostrarUltimaMedida():

1. medida = logica.obtenerUltimaMedida()
2. Si es null: estado "Esperando la primera medición".
3. Si hay error: estado "No se puede consultar la API".
4. Si hay medida: pintarMedida(medida) y estado "Servidor conectado".

Algoritmo de pintarMedida(medida):

1. Escribir el valor en la tarjeta de su tipo y el contador en la tarjeta Contador.
2. Rellenar la tabla: nombreDeTipo(tipo), valor + unidadDeTipo(tipo), contador, horaDe(fecha).
3. Escribir la fecha completa en `fechaMedicion`.

# Aclaraciones del Diseño

- El servidor solo devuelve la última medida. Las tarjetas de los otros tipos conservan el último valor visto mientras la página está abierta (o "--").
- La tarjeta Contador sirve en la demo para ver que llega una medida nueva aunque el valor sea el mismo.
- La URL de la API es relativa (`/api/mediciones.php`) porque la web y la API están en el mismo servidor.
- `LogicaFake` recibe la función de petición en el constructor para poder probarla sin red.

# Reglas Generales

- Lenguaje de programación: HTML, CSS y JavaScript sin frameworks. Ficheros: `index.html`, `logicaFake.js`, `pagina.js`.
- El encabezado de cada función/método incluirá su diseño lógico dentro de un bloque de comentarios delimitado por líneas `--------------------`.
- El código será lo más claro y autoexplicativo posible, de modo que prácticamente no requiera comentarios adicionales.
- Se generarán tests automáticos para todas las funciones y métodos clave: `LogicaFake.obtenerUltimaMedida()` con respuestas 200, 404 y 500 simuladas, `nombreDeTipo()`, `unidadDeTipo()` y `horaDe()`. Se ejecutan con `node --test` desde la carpeta `web`.
- Cada fichero llevará una cabecera con: nombre, descripción, copyright, fecha, autor y aportación.
