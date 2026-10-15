// ---------------------------------------------------------------------
// Fichero:     Codificacion.h
// Descripción: Funciones puras para meter una medida en el major y el
//              minor de un iBeacon. No dependen de Arduino ni de BLE,
//              así que se pueden probar en el ordenador.
// Copyright:   (c) 2026 Matilde Calleja García
// Fecha:       14/10/2026
// Autor:       Matilde Calleja García
// Aportación:  Diseño propio; código generado con IA a partir del diseño
//              y revisado por la autora.
// ---------------------------------------------------------------------
#ifndef CODIFICACION_H
#define CODIFICACION_H

#include <stdint.h>

namespace Codificacion {

  // --------------------------------------------------------------
  // codigo: N, contador: N --> calcularMajor() --> N
  //
  // Byte alto = código de la medida, byte bajo = contador.
  // major = (codigo mod 256) * 256 + (contador mod 256)
  // --------------------------------------------------------------
  inline uint16_t calcularMajor(int codigo, int contador) {
    return (uint16_t) (((codigo & 0xFF) << 8) | (contador & 0xFF));
  }

  // --------------------------------------------------------------
  // valor: Z --> valorAMinor() --> N
  //
  // El minor son 16 bits sin signo. Un valor negativo se guarda en
  // complemento a 2 (ej: -12 --> 0xFFF4) y el receptor lo vuelve a
  // leer con signo. Rango válido: -32768 .. 32767.
  // --------------------------------------------------------------
  inline uint16_t valorAMinor(int valor) {
    return (uint16_t) (int16_t) valor;
  }

} // namespace

#endif
