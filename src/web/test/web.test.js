// ---------------------------------------------------------------------
// Fichero:     web.test.js
// Descripción: Tests automáticos de la web: LogicaFake con respuestas
//              del servidor simuladas (sin red) y las funciones de
//              pagina.js. Se ejecutan desde src/web con:
//                  node --test
// Copyright:   (c) 2026 Matilde Calleja
// Fecha:       15/10/2026
// Autor:       Matilde Calleja
// Aportación:  Tests generados con IA a partir del diseño
//              (web_design.md) y revisados por la autora.
// ---------------------------------------------------------------------

const test = require("node:test");
const assert = require("node:assert/strict");

const { LogicaFake } = require("../logicaFake.js");
const { nombreDeTipo, unidadDeTipo, horaDe } = require("../pagina.js");

const MEDIDA = { id: 7, tipo: 11, valor: 123, contador: 5, fecha: "2026-10-15T10:00:00+00:00" };

// --------------------------------------------------------------
// status: N, cuerpo: ? --> servidorQueResponde() --> fetch simulado
// Guarda en .peticiones las llamadas que recibe.
// --------------------------------------------------------------
function servidorQueResponde(status, cuerpo) {
  const falsoFetch = async (url, opciones) => {
    falsoFetch.peticiones.push({ url, opciones });
    return {
      status,
      ok: status >= 200 && status < 300,
      json: async () => cuerpo
    };
  };
  falsoFetch.peticiones = [];
  return falsoFetch;
}

// --- LogicaFake.obtenerUltimaMedida() --------------------------------

test("200: devuelve la medida que manda el servidor", async () => {
  const logica = new LogicaFake("/api/mediciones.php", servidorQueResponde(200, MEDIDA));
  assert.deepEqual(await logica.obtenerUltimaMedida(), MEDIDA);
});

test("hace un GET sin caché a la URL indicada", async () => {
  const servidor = servidorQueResponde(200, MEDIDA);
  await new LogicaFake("/api/mediciones.php", servidor).obtenerUltimaMedida();

  assert.equal(servidor.peticiones.length, 1);
  assert.equal(servidor.peticiones[0].url, "/api/mediciones.php");
  assert.equal(servidor.peticiones[0].opciones.method, "GET");
  assert.equal(servidor.peticiones[0].opciones.cache, "no-store");
});

test("404: todavía no hay medidas, devuelve null", async () => {
  const logica = new LogicaFake("/api/mediciones.php",
    servidorQueResponde(404, { ok: false, mensaje: "Todavía no hay mediciones" }));
  assert.equal(await logica.obtenerUltimaMedida(), null);
});

test("500: lanza un error con el código HTTP", async () => {
  const logica = new LogicaFake("/api/mediciones.php",
    servidorQueResponde(500, { ok: false, mensaje: "Error interno del servidor" }));
  await assert.rejects(() => logica.obtenerUltimaMedida(), /HTTP 500/);
});

test("sin conexión: el error llega a quien llama", async () => {
  const sinRed = async () => { throw new TypeError("Failed to fetch"); };
  await assert.rejects(() => new LogicaFake("/api/mediciones.php", sinRed).obtenerUltimaMedida(), TypeError);
});

// --- nombreDeTipo() y unidadDeTipo() -----------------------------------

test("nombreDeTipo: 11, 12 y 13 tienen nombre; otro es Desconocido", () => {
  assert.equal(nombreDeTipo(11), "CO₂");
  assert.equal(nombreDeTipo(12), "Temperatura");
  assert.equal(nombreDeTipo(13), "Ruido");
  assert.equal(nombreDeTipo(99), "Desconocido");
});

test("unidadDeTipo: ppm, °C, dB y vacío si no se conoce", () => {
  assert.equal(unidadDeTipo(11), "ppm");
  assert.equal(unidadDeTipo(12), "°C");
  assert.equal(unidadDeTipo(13), "dB");
  assert.equal(unidadDeTipo(99), "");
});

// --- horaDe() ----------------------------------------------------------

test("horaDe: una fecha válida da una hora hh:mm:ss", () => {
  assert.match(horaDe(MEDIDA.fecha), /^\d{1,2}:\d{2}:\d{2}$/);
});

test("horaDe: una fecha no válida da --", () => {
  assert.equal(horaDe("no es una fecha"), "--");
  assert.equal(horaDe(undefined), "--");
});