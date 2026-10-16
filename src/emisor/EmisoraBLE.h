// ---------------------------------------------------------------------
// Fichero:     EmisoraBLE.h
// Descripción: Emite anuncios iBeacon con la biblioteca Bluefruit.
// Copyright:   (c) 2026 Matilde Calleja García
// Fecha:       14/10/2026
// Autor:       Matilde Calleja García (a partir del código de
//              Jordi Bataller i Mascarell)
// Aportación:  Diseño por ingeniería inversa; código revisado. Se ha
//              quitado el nombre y el TxPower del paquete principal
//              para que quepa el iBeacon (máximo 31 bytes).
// ---------------------------------------------------------------------
#ifndef EMISORA_BLE_H
#define EMISORA_BLE_H

#include <bluefruit.h>

class EmisoraBLE {

private:
  const char* nombreEmisora;
  const uint16_t fabricanteID;
  const int8_t txPower;

public:

  // --------------------------------------------------------------
  // nombre_emisora: Text, fabricante_id: N, tx_power: Z
  //     --> EmisoraBLE() -->
  // --------------------------------------------------------------
  EmisoraBLE(const char* nombreEmisora, uint16_t fabricanteID, int8_t txPower)
    : nombreEmisora(nombreEmisora), fabricanteID(fabricanteID), txPower(txPower) {
  }

  // --------------------------------------------------------------
  // encenderEmisora() -->
  // 1. Arrancar el módulo Bluetooth.
  // 2. Configurar la potencia de emisión y el nombre.
  // --------------------------------------------------------------
  void encenderEmisora() {
    Bluefruit.begin();
    Bluefruit.setTxPower(txPower);
    Bluefruit.setName(nombreEmisora);
  }

  // --------------------------------------------------------------
  // B <-- estaAnunciando() <--
  // --------------------------------------------------------------
  bool estaAnunciando() {
    return Bluefruit.Advertising.isRunning();
  }

  // --------------------------------------------------------------
  // detenerAnuncio() -->
  // --------------------------------------------------------------
  void detenerAnuncio() {
    if (estaAnunciando()) {
      Bluefruit.Advertising.stop();
    }
  }

  // --------------------------------------------------------------
  // beacon_uuid: [N]_16, major: N, minor: N, rssi: Z
  //     --> emitirAnuncioIBeacon() --> B
  //
  // 1. detenerAnuncio()
  // 2. Vaciar el paquete de anuncio y la respuesta al escaneo
  //    (si no, cada llamada añadiría datos y dejaría de caber).
  // 3. Paquete de anuncio: flags + iBeacon         (30 de 31 bytes)
  //    Respuesta al escaneo: nombre de la emisora
  // 4. Empezar a anunciar sin límite de tiempo.
  // 5. Devolver true si el iBeacon ha cabido en el anuncio.
  // --------------------------------------------------------------
  bool emitirAnuncioIBeacon(uint8_t* beaconUUID, uint16_t major, uint16_t minor, int8_t rssi) {
    detenerAnuncio();

    Bluefruit.Advertising.clearData();
    Bluefruit.ScanResponse.clearData();

    Bluefruit.Advertising.addFlags(BLE_GAP_ADV_FLAGS_LE_ONLY_GENERAL_DISC_MODE);

    BLEBeacon elBeacon(beaconUUID, major, minor, rssi);
    elBeacon.setManufacturer(fabricanteID);
    bool cabe = Bluefruit.Advertising.setBeacon(elBeacon);

    Bluefruit.ScanResponse.addName();

    Bluefruit.Advertising.restartOnDisconnect(true);
    Bluefruit.Advertising.setInterval(32, 244);   // unidades de 0.625 ms
    Bluefruit.Advertising.setFastTimeout(30);     // segundos en modo rápido
    Bluefruit.Advertising.start(0);               // 0 = no parar solo

    return cabe;
  }
};

#endif
