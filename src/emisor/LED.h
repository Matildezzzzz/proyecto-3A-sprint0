// ---------------------------------------------------------------------
// Fichero:     LED.h
// Descripción: Controla un LED de la placa.
// Copyright:   (c) 2026 Matilde Calleja García
// Fecha:       14/10/2026
// Autor:       Matilde Calleja García (a partir del código de
//              Jordi Bataller i Mascarell)
// Aportación:  Diseño por ingeniería inversa; código revisado.
// ---------------------------------------------------------------------
#ifndef LED_H
#define LED_H

class LED {

private:
  int numeroLED;
  bool encendido;

public:

  // --------------------------------------------------------------
  // numero: Z --> LED() -->
  // Configura el pin como salida y deja el LED apagado.
  // --------------------------------------------------------------
  LED(int numero) : numeroLED(numero), encendido(false) {
    pinMode(numeroLED, OUTPUT);
    apagar();
  }

  // --------------------------------------------------------------
  // encender() -->
  // --------------------------------------------------------------
  void encender() {
    digitalWrite(numeroLED, HIGH);
    encendido = true;
  }

  // --------------------------------------------------------------
  // apagar() -->
  // --------------------------------------------------------------
  void apagar() {
    digitalWrite(numeroLED, LOW);
    encendido = false;
  }

  // --------------------------------------------------------------
  // alternar() -->
  // --------------------------------------------------------------
  void alternar() {
    if (encendido) {
      apagar();
    } else {
      encender();
    }
  }

  // --------------------------------------------------------------
  // tiempo: Z --> brillar() -->
  // 1. encender()  2. esperar(tiempo)  3. apagar()
  // --------------------------------------------------------------
  void brillar(long tiempo) {
    encender();
    esperar(tiempo);
    apagar();
  }
};

#endif
