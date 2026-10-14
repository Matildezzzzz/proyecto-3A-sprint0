// ---------------------------------------------------------------------
// Fichero:     emisor.ino
// Descripción: Programa principal del nodo sensor. Cada vuelta del loop toma una medida (ficticia) y la publica por BLE como iBeacon con un contador que cambia en cada vuelta.
// Copyright:   (c) 2026 Matilde Calleja García
// Fecha:       14/10/2026
// Autor:       Matilde Calleja García (a partir del código de Jordi Bataller i Mascarell)
// Aportación:  Diseño por ingeniería inversa; código generado con IA a partir del diseño (doc/emisor_design.md) y revisado.
// ---------------------------------------------------------------------
#include <bluefruit.h>

// --------------------------------------------------------------
// tiempo: Z --> esperar() -->
// Se define antes de los includes porque LED y Publicador la usan.
// --------------------------------------------------------------
void esperar(long tiempo) {
  delay(tiempo);
}

#include "LED.h"
#include "PuertoSerie.h"
#include "Medidor.h"
#include "Publicador.h"

// --------------------------------------------------------------
// Estado global
// --------------------------------------------------------------
namespace Globales {
  LED elLED(LED_BUILTIN);
  PuertoSerie elPuerto(115200);
  Publicador elPublicador;
  Medidor elMedidor;
}

namespace Loop {
  uint8_t contador = 0;   // 0..255, vuelve a 0 después de 255
}

// --------------------------------------------------------------
// inicializarPlaquita() -->
// Nada que hacer en esta placa.
// --------------------------------------------------------------
void inicializarPlaquita() {
}

// --------------------------------------------------------------
// setup() -->
//
// 1. elPuerto.esperarDisponible()
// 2. inicializarPlaquita()
// 3. elPublicador.encenderEmisora()
// 4. elMedidor.iniciarMedidor()
// 5. esperar(1000)
// 6. escribir "---- setup(): fin ----"
// --------------------------------------------------------------
void setup() {
  using namespace Globales;

  elPuerto.esperarDisponible();
  inicializarPlaquita();
  elPublicador.encenderEmisora();
  elMedidor.iniciarMedidor();

  esperar(1000);
  elPuerto.escribir("---- setup(): fin ----");
}

// --------------------------------------------------------------
// lucecitas() -->
// 3 destellos cortos y 1 largo: indica que empieza una vuelta.
// --------------------------------------------------------------
void lucecitas() {
  using namespace Globales;

  elLED.brillar(100);
  esperar(400);
  elLED.brillar(100);
  esperar(400);
  elLED.brillar(100);
  esperar(400);
  elLED.brillar(1000);
  esperar(1000);
}

// --------------------------------------------------------------
// loop() -->
//
// 1. contador = (contador + 1) mod 256
// 2. lucecitas()
// 3. valor_co2 = elMedidor.medirCO2()
// 4. cabe = elPublicador.publicarCO2(valor_co2, contador, 1000)
// 5. Escribir la medida publicada (o un error si no cabe).
// --------------------------------------------------------------
void loop() {
  using namespace Loop;
  using namespace Globales;

  contador++;

  elPuerto.escribir(String("---- loop(): empieza ") + contador);

  lucecitas();

  int valorCO2 = elMedidor.medirCO2();
  bool cabe = elPublicador.publicarCO2(valorCO2, contador, 1000);

  if (cabe) {
    elPuerto.escribir(String("Publicado CO2=") + valorCO2 + " contador=" + contador);
  } else {
    elPuerto.escribir("ERROR: el iBeacon no cabe en el anuncio");
  }
}
