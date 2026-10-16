# Diseño del Componente

## Arquitectura de la web

```
 index.html (pantalla)
        |
        v
   Pagina (pagina.js) --obtenerUltimaMedida()--> LogicaFake   (frontend_business_logic_design.md)
```

- `Pagina`: decide qué se muestra. No hace peticiones HTTP: solo llama a la lógica del negocio del cliente.
- La lógica del negocio del cliente (`LogicaFake`) se describe en `frontend_business_logic_design.md`.

## Tipos

```
N, Z, B, Text   (igual que en el resto de componentes)

Medida = { id: N, tipo: N, valor: Z, contador: N, fecha: Text }   (ver business_logic_design.md)
```

## Pantalla (`index.html`)

- Cabecera con la píldora de estado (`estadoPildora`, `textoPildora`).
- 4 tarjetas: CO₂ (`co2`, ppm), Temperatura (`temperatura`, °C), Ruido (`ruido`, dB) y Contador (`contador`).
- Panel "Estado del sistema": `iconoEstado`, `estadoPrincipal`, `detalleEstado`, `fechaMedicion`.
- Panel "Última medición recibida": tabla con `tablaTipo`, `tablaValor`, `tablaContador`, `tablaHora`.
- Carga `../frontend_business_logic/logicaFake.js` y `pagina.js`, y arranca `Pagina(LogicaFake("/api/mediciones.php"), 2000)`.

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
2. Si es nulo: estado "Esperando la primera medición".
3. Si hay error: estado "No se puede consultar la API".
4. Si hay medida: pintarMedida(medida) y estado "Servidor conectado".

Algoritmo de pintarMedida(medida):

1. Escribir el valor en la tarjeta de su tipo y el contador en la tarjeta Contador.
2. Rellenar la tabla: nombreDeTipo(tipo), valor + unidadDeTipo(tipo), contador, horaDe(fecha).
3. Escribir la fecha completa en `fechaMedicion`.

# Aclaraciones del Diseño

- La interfaz no contiene comunicación con el servidor: no usa `fetch` ni conoce la URL de la API más que para pasársela a `LogicaFake` al arrancar.
- El servidor solo devuelve la última medida. Las tarjetas de los otros tipos conservan el último valor visto mientras la página está abierta (o "--").
- La tarjeta Contador sirve en la demo para ver que llega una medida nueva aunque el valor sea el mismo.

# Reglas Generales

- Lenguaje de programación: HTML, CSS y JavaScript sin frameworks. Ficheros: `index.html`, `pagina.js`.
- El encabezado de cada función/método incluirá su diseño lógico dentro de un bloque de comentarios delimitado por líneas `--------------------`.
- El código será lo más claro y autoexplicativo posible, de modo que prácticamente no requiera comentarios adicionales.
- Se generarán tests automáticos para todas las funciones y métodos clave: `nombreDeTipo()`, `unidadDeTipo()` y `horaDe()`. Están en `src/web/test/web.test.js` y se ejecutan desde `src/web` con `node --test`.
- Cada fichero llevará una cabecera con: nombre, descripción, copyright, fecha, autor y aportación.