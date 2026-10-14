// ---------------------------------------------------------------------
// Fichero:     PuertoSerie.h
// Descripción: Escribe mensajes por el puerto serie (Monitor Serie).
// Copyright:   (c) 2026 Julia Valén de Oliveira
// Fecha:       14/10/2026
// Autor:       Julia Valén de Oliveira (a partir del código de
//              Jordi Bataller i Mascarell)
// Aportación:  Diseño por ingeniería inversa; código revisado.
// ---------------------------------------------------------------------
#ifndef PUERTO_SERIE_H
#define PUERTO_SERIE_H

class PuertoSerie {

private:
  long baudios;
  static const unsigned long ESPERA_MAXIMA_MS = 2000;

public:

  // --------------------------------------------------------------
  // baudios: Z --> PuertoSerie() -->
  // --------------------------------------------------------------
  PuertoSerie(long baudios) : baudios(baudios) {
  }

  // --------------------------------------------------------------
  // esperarDisponible() -->
  //
  // 1. Abrir el puerto a "baudios".
  // 2. Esperar a que el ordenador lo abra, como MÁXIMO 2 segundos,
  //    para que la placa también funcione sin cable USB (batería).
  // --------------------------------------------------------------
  void esperarDisponible() {
    Serial.begin(baudios);

    unsigned long inicio = millis();
    while (!Serial && millis() - inicio < ESPERA_MAXIMA_MS) {
      delay(10);
    }
  }

  // --------------------------------------------------------------
  // mensaje: Text --> escribir() -->
  // Escribe el mensaje y salta de línea.
  // --------------------------------------------------------------
  template <typename T>
  void escribir(T mensaje) {
    Serial.println(mensaje);
  }
};

#endif
