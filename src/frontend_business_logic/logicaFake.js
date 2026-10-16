// ---------------------------------------------------------------------
// Fichero:     logicaFake.js
// Descripción: Representa en la web a la lógica del negocio del
//              servidor. La página llama a obtenerUltimaMedida() sin
//              saber que por debajo hay una petición HTTP.
// Copyright:   (c) 2026 Matilde Calleja
// Fecha:       14/10/2026
// Autor:       Matilde Calleja
// Aportación:  Diseño propio; código generado con IA a partir del diseño
//              y revisado por la autora.
// ---------------------------------------------------------------------

class LogicaFake {

  // --------------------------------------------------------------
  // urlServidor: Text, hacerPeticion: fetch --> constructor() -->
  // hacerPeticion se puede cambiar en los tests para no usar la red.
  // --------------------------------------------------------------
  constructor(urlServidor, hacerPeticion) {
    this.urlServidor = urlServidor;
    this.hacerPeticion = hacerPeticion || ((...args) => fetch(...args));
  }

  // --------------------------------------------------------------
  // Medida | null <-- obtenerUltimaMedida() <--     (asíncrono)
  //
  // 1. GET urlServidor
  // 2. 200 --> devolver la medida {id, tipo, valor, contador, fecha}
  //    404 --> devolver null (todavía no hay medidas)
  //    otro --> error "HTTP <código>"
  // --------------------------------------------------------------
  async obtenerUltimaMedida() {
    const respuesta = await this.hacerPeticion(this.urlServidor, {
      method: "GET",
      cache: "no-store"
    });

    if (respuesta.status === 404) {
      return null;
    }

    if (!respuesta.ok) {
      throw new Error("HTTP " + respuesta.status);
    }

    return await respuesta.json();
  }
}

if (typeof module !== "undefined") {
  module.exports = { LogicaFake };
}
