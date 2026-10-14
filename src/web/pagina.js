// ---------------------------------------------------------------------
// Fichero:     pagina.js
// Descripción: Controla la pantalla: pide la última medida a la
//              LogicaFake cada 2 segundos y la pinta. No sabe nada de HTTP.
// Copyright:   (c) 2026 Matilde Calleja
// Fecha:       14/10/2026
// Autor:       Matilde Calleja
// Aportación:  Diseño propio; código generado con IA a partir del diseño
//              y revisado por la autora.
// ---------------------------------------------------------------------

const TIPOS = {
  11: { nombre: "CO₂", unidad: "ppm", tarjeta: "co2" },
  12: { nombre: "Temperatura", unidad: "°C", tarjeta: "temperatura" },
  13: { nombre: "Ruido", unidad: "dB", tarjeta: "ruido" }
};

// --------------------------------------------------------------
// codigo: N --> nombreDeTipo() --> Text
// 11 -> CO₂, 12 -> Temperatura, 13 -> Ruido, otro -> Desconocido
// --------------------------------------------------------------
function nombreDeTipo(codigo) {
  return TIPOS[codigo] ? TIPOS[codigo].nombre : "Desconocido";
}

// --------------------------------------------------------------
// codigo: N --> unidadDeTipo() --> Text
// --------------------------------------------------------------
function unidadDeTipo(codigo) {
  return TIPOS[codigo] ? TIPOS[codigo].unidad : "";
}

// --------------------------------------------------------------
// fechaISO: Text --> horaDe() --> Text
// "--" si la fecha no es válida.
// --------------------------------------------------------------
function horaDe(fechaISO) {
  const fecha = new Date(fechaISO);
  return Number.isNaN(fecha.getTime()) ? "--" : fecha.toLocaleTimeString("es-ES");
}

class Pagina {

  // --------------------------------------------------------------
  // logica: LogicaFake, intervaloMs: N --> constructor() -->
  // --------------------------------------------------------------
  constructor(logica, intervaloMs) {
    this.logica = logica;
    this.intervaloMs = intervaloMs;
  }

  // --------------------------------------------------------------
  // iniciar() -->
  // mostrarUltimaMedida() ahora y luego cada intervaloMs.
  // --------------------------------------------------------------
  iniciar() {
    this.mostrarUltimaMedida();
    setInterval(() => this.mostrarUltimaMedida(), this.intervaloMs);
  }

  // --------------------------------------------------------------
  // mostrarUltimaMedida() -->     (asíncrono)
  //
  // 1. medida = logica.obtenerUltimaMedida()
  // 2. null  --> estado "Esperando la primera medición"
  //    error --> estado "No se puede consultar la API"
  //    medida --> pintarMedida(medida) y estado "Servidor conectado"
  // --------------------------------------------------------------
  async mostrarUltimaMedida() {
    try {
      const medida = await this.logica.obtenerUltimaMedida();

      if (medida === null) {
        this.cambiarEstado("", "Esperando la primera medición",
          "El servidor funciona, pero AppBiom todavía no ha enviado datos.",
          "...", "Esperando mediciones");
        return;
      }

      this.pintarMedida(medida);
      this.cambiarEstado("connected", "Servidor conectado",
        "La web está recibiendo los datos de la API.", "✓", "Datos actualizados");

    } catch (error) {
      console.error("Error al consultar AppBiom:", error);
      this.cambiarEstado("error", "No se puede consultar la API",
        "Comprueba que el servidor esté activo y que la dirección de la API sea correcta.",
        "!", "Error de conexión");
    }
  }

  // --------------------------------------------------------------
  // medida: Medida --> pintarMedida() -->
  //
  // 1. Tarjeta de su tipo y tarjeta de contador.
  // 2. Tabla: tipo, valor, contador y hora.
  // 3. Fecha completa debajo del estado.
  // --------------------------------------------------------------
  pintarMedida(medida) {
    const valor = Number(medida.valor).toLocaleString("es-ES");
    const valorConUnidad = valor + " " + unidadDeTipo(medida.tipo);

    if (TIPOS[medida.tipo]) {
      this.escribir(TIPOS[medida.tipo].tarjeta, valor);
    }
    this.escribir("contador", medida.contador);

    this.escribir("tablaTipo", nombreDeTipo(medida.tipo));
    this.escribir("tablaValor", valorConUnidad);
    this.escribir("tablaContador", medida.contador);
    this.escribir("tablaHora", horaDe(medida.fecha));

    const fecha = new Date(medida.fecha);
    this.escribir("fechaMedicion", Number.isNaN(fecha.getTime())
      ? "Medición recibida."
      : "Última medición recibida: " + fecha.toLocaleString("es-ES"));
  }

  // --------------------------------------------------------------
  // tipo, titulo, detalle, icono, etiqueta: Text --> cambiarEstado() -->
  // --------------------------------------------------------------
  cambiarEstado(tipo, titulo, detalle, icono, etiqueta) {
    this.escribir("estadoPrincipal", titulo);
    this.escribir("detalleEstado", detalle);
    this.escribir("iconoEstado", icono);
    this.escribir("textoPildora", etiqueta);
    document.getElementById("estadoPildora").className = "demo-pill " + tipo;
  }

  // --------------------------------------------------------------
  // id: Text, texto: Text --> escribir() -->
  // --------------------------------------------------------------
  escribir(id, texto) {
    document.getElementById(id).textContent = texto;
  }
}

if (typeof module !== "undefined") {
  module.exports = { nombreDeTipo, unidadDeTipo, horaDe, Pagina };
}
