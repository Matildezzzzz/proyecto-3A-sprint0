// ---------------------------------------------------------------------
// Fichero:     web.test.js
// Descripción: Tests automáticos de las funciones de pagina.js.
//              Se ejecutan desde src/web con:
//                  node --test
// Copyright:   (c) 2026 Matilde Calleja
// Fecha:       15/10/2026
// Autor:       Matilde Calleja
// Aportación:  Tests generados con IA a partir del diseño
//              (web_design.md) y revisados por la autora.
// ---------------------------------------------------------------------

const test = require("node:test");
const assert = require("node:assert/strict");

const { nombreDeTipo, unidadDeTipo, horaDe } = require("../pagina.js");

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

test("horaDe: una fecha válida da una hora hh:mm:ss", () => {
  assert.match(horaDe("2026-10-15T10:00:00+00:00"), /^\d{1,2}:\d{2}:\d{2}$/);
});

test("horaDe: una fecha no válida da --", () => {
  assert.equal(horaDe("no es una fecha"), "--");
  assert.equal(horaDe(undefined), "--");
});