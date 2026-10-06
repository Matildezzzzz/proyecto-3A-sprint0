# Diseño del Componente

## Arquitectura de la web
 index.html (pantalla)
        |
        v
   Pagina  --> LogicaFake --HTTP GET /medida/ultima--> servidor

- `Pagina`: controla la pantalla. No sabe nada de HTTP.
- `LogicaFake`: ofrece `obtenerUltimaMedida()` como si fuera la lógica del negocio y oculta la comunicación HTTP.

## Tipos
N, Z, B, Text   (igual que en el resto de componentes)

Medida = (
    id:       N,
    tipo:     N,
    valor:    Z,
    contador: N,
    fecha:    Text
)

## Pantalla (`index.html`)
Elementos:
- Título: "Última medida".
- Zona de resultado con: tipo de medida, valor, contador y fecha.
- Zona de mensajes (para errores o "Todavía no hay medidas").
- Botón "Actualizar".

## Clase LogicaFake
---------------------------- LogicaFake -----------------------------
|
|  url_servidor: Text
|
url_servidor: Text --> LogicaFake() -->
|
         Medida | vacío <-- obtenerUltimaMedida() <--
|
--------------------------------------------------------------------

Algoritmo de obtenerUltimaMedida() (asíncrono):
1. Enviar `GET url_servidor + "/medida/ultima"`.
2. Si la respuesta es 200: devolver el JSON recibido como Medida.
3. Si la respuesta es 404: devolver vacío.
4. En otro caso: error "no se pudo contactar con el servidor".

## Clase Pagina
------------------------------ Pagina -------------------------------
|
|  logica: LogicaFake
|
logica: LogicaFake --> Pagina() -->
|
              iniciar() -->
|
              mostrarUltimaMedida() -->
|
codigo: N --> nombreDeTipo() --> Text
|
--------------------------------------------------------------------

Algoritmo de iniciar():
1. Asociar el botón "Actualizar" a mostrarUltimaMedida().
2. mostrarUltimaMedida()
3. Repetir mostrarUltimaMedida() cada 5 segundos.

Algoritmo de mostrarUltimaMedida():
1. medida = logica.obtenerUltimaMedida()
2. Si es vacío: mostrar "Todavía no hay medidas".
3. Si hay error: mostrar el mensaje de error.
4. Si hay medida: mostrar nombreDeTipo(medida.tipo), medida.valor, medida.contador y medida.fecha.

Algoritmo de nombreDeTipo(codigo):
11 --> "CO2"
12 --> "Temperatura"
13 --> "Ruido"
otro --> "Desconocido"

# Aclaraciones del Diseño
- `LogicaFake` tiene el mismo nombre de operación que la lógica real del servidor (`obtenerUltimaMedida`). `Pagina` no sabe que por debajo hay HTTP.
- `nombreDeTipo()` es una función pura para poder probarla sin navegador.
- La actualización cada 5 segundos permite que en la demo la medida nueva aparezca sin recargar la página.
- La URL del servidor se define en un único sitio (al crear `LogicaFake`) para cambiarla fácilmente.

# Reglas Generales
- Lenguaje de programación: HTML, CSS y JavaScript sin frameworks. Ficheros: `index.html`, `logicaFake.js`, `pagina.js`.
- El encabezado de cada función/método incluirá su diseño lógico dentro de un bloque de comentarios delimitado por líneas `--------------------`.
- El código será lo más claro y autoexplicativo posible, de modo que prácticamente no requiera comentarios adicionales.
- Se generarán tests automáticos para todas las funciones y métodos clave: `nombreDeTipo()` y `LogicaFake.obtenerUltimaMedida()` simulando las respuestas 200, 404 y error del servidor. Se ejecutan con `npm test`.
- Cada fichero llevará una cabecera con: nombre, descripción, copyright, fecha, autor y aportación.