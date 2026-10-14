// ---------------------------------------------------------------------
// Fichero:     Publicador.h
// Descripción: Publica medidas como anuncios iBeacon: decide qué va en
//              el major y en el minor y se lo pasa a la EmisoraBLE.
// Copyright:   (c) 2026 Julia Valén de Oliveira
// Fecha:       14/10/2026
// Autor:       Julia Valén de Oliveira (a partir del código de
//              Jordi Bataller i Mascarell)
// Aportación:  Diseño por ingeniería inversa; código revisado. La
//              codificación se ha sacado a Codificacion.h para probarla.
// ---------------------------------------------------------------------
#ifndef PUBLICADOR_H
#define PUBLICADOR_H

#include "EmisoraBLE.h"
#include "Codificacion.h"

class Publicador {

private:

  // "EPSG-GTI-PROY-3A" en ASCII (16 bytes)
  uint8_t beaconUUID[16] = {
    'E', 'P', 'S', 'G', '-', 'G', 'T', 'I',
    '-', 'P', 'R', 'O', 'Y', '-', '3', 'A'
  };

  EmisoraBLE laEmisora{
    "GTI3A-2026",   // nombre de la emisora (lo busca la app)
    0x004c,         // fabricante: Apple (iBeacon)
    4               // potencia de emisión (dBm)
  };

  // --------------------------------------------------------------
  // tipo: MedicionID, valor: Z, contador: N, tiempo_espera: Z
  //     --> publicarMedida() --> B
  //
  // 1. major = Codificacion::calcularMajor(tipo, contador)
  // 2. minor = Codificacion::valorAMinor(valor)
  // 3. cabe = laEmisora.emitirAnuncioIBeacon(beacon_uuid, major, minor, RSSI)
  // 4. esperar(tiempo_espera)
  // 5. laEmisora.detenerAnuncio()
  // 6. Devolver cabe
  // --------------------------------------------------------------
  bool publicarMedida(int tipo, int valor, uint8_t contador, long tiempoEspera) {
    uint16_t major = Codificacion::calcularMajor(tipo, contador);
    uint16_t minor = Codificacion::valorAMinor(valor);

    bool cabe = laEmisora.emitirAnuncioIBeacon(beaconUUID, major, minor, RSSI);
    esperar(tiempoEspera);
    laEmisora.detenerAnuncio();

    return cabe;
  }

public:

  enum MedicionID {
    CO2 = 11,
    TEMPERATURA = 12,
    RUIDO = 13
  };

  static const int8_t RSSI = -53;   // potencia medida a 1 metro

  // --------------------------------------------------------------
  // Publicador() -->
  // --------------------------------------------------------------
  Publicador() {
  }

  // --------------------------------------------------------------
  // encenderEmisora() -->
  // --------------------------------------------------------------
  void encenderEmisora() {
    laEmisora.encenderEmisora();
  }

  // --------------------------------------------------------------
  // valor_co2: Z, contador: N, tiempo_espera: Z --> publicarCO2() --> B
  // --------------------------------------------------------------
  bool publicarCO2(int valorCO2, uint8_t contador, long tiempoEspera) {
    return publicarMedida(CO2, valorCO2, contador, tiempoEspera);
  }

  // --------------------------------------------------------------
  // valor_temperatura: Z, contador: N, tiempo_espera: Z
  //     --> publicarTemperatura() --> B
  // --------------------------------------------------------------
  bool publicarTemperatura(int valorTemperatura, uint8_t contador, long tiempoEspera) {
    return publicarMedida(TEMPERATURA, valorTemperatura, contador, tiempoEspera);
  }
};

#endif
