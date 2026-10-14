// ---------------------------------------------------------------------
// Fichero:     Medidor.h
// Descripción: Simula el sensor. Devuelve medidas ficticias.
// Copyright:   (c) 2026 Julia Valén de Oliveira
// Fecha:       14/10/2026
// Autor:       Julia Valén de Oliveira (a partir del código de
//              Jordi Bataller i Mascarell)
// Aportación:  Diseño por ingeniería inversa; código revisado.
// ---------------------------------------------------------------------
#ifndef MEDIDOR_H
#define MEDIDOR_H

class Medidor {

public:

  // --------------------------------------------------------------
  // Medidor() -->
  // --------------------------------------------------------------
  Medidor() {
  }

  // --------------------------------------------------------------
  // iniciarMedidor() -->
  // Sin sensor real en este sprint: no hay nada que preparar.
  // --------------------------------------------------------------
  void iniciarMedidor() {
  }

  // --------------------------------------------------------------
  // Z <-- medirCO2() <--
  //
  // MEDIDA FICTICIA. En la demo se cambia este número, se sube el
  // programa a la placa y el nuevo valor tiene que salir en la web.
  // --------------------------------------------------------------
  int medirCO2() {
    return 123;
  }

  // --------------------------------------------------------------
  // Z <-- medirTemperatura() <--
  // MEDIDA FICTICIA.
  // --------------------------------------------------------------
  int medirTemperatura() {
    return -12;
  }
};

#endif
