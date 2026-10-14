// ---------------------------------------------------------------------
// Fichero:     test_codificacion.cpp
// Descripción: Tests de Codificacion.h. Se ejecutan en el ordenador,
//              sin placa:
//                  g++ -std=c++11 test_codificacion.cpp -o test && ./test
// Copyright:   (c) 2026 Julia Valén de Oliveira
// Fecha:       14/10/2026
// Autor:       Julia Valén de Oliveira
// Aportación:  Tests generados con IA a partir del diseño y revisados.
// ---------------------------------------------------------------------
#include <cstdio>
#include "../Codificacion.h"

using namespace Codificacion;

static int fallos = 0;

// --------------------------------------------------------------
// condicion: B, nombre: Text --> comprobar() -->
// --------------------------------------------------------------
static void comprobar(bool condicion, const char* nombre) {
  printf("%s  %s\n", condicion ? "OK   " : "FALLO", nombre);
  if (!condicion) {
    fallos++;
  }
}

int main() {
  // major
  comprobar(calcularMajor(11, 5) == 11 * 256 + 5, "major de CO2 con contador 5");
  comprobar(calcularMajor(11, 0) == 0x0B00, "contador 0");
  comprobar(calcularMajor(11, 255) == 0x0BFF, "contador 255");
  comprobar(calcularMajor(11, 256) == 0x0B00, "el contador ocupa solo un byte");
  comprobar((calcularMajor(12, 7) >> 8) == 12, "byte alto = tipo");
  comprobar((calcularMajor(12, 7) & 0xFF) == 7, "byte bajo = contador");

  // minor
  comprobar(valorAMinor(123) == 123, "valor positivo");
  comprobar(valorAMinor(0) == 0, "valor cero");
  comprobar(valorAMinor(-12) == 0xFFF4, "negativo en complemento a 2");
  comprobar((int16_t) valorAMinor(-12) == -12, "el receptor recupera el negativo");
  comprobar(valorAMinor(32767) == 0x7FFF, "maximo positivo");

  printf("\n%s (%d fallos)\n", fallos == 0 ? "TODO OK" : "HAY FALLOS", fallos);
  return fallos == 0 ? 0 : 1;
}
