// ---------------------------------------------------------------------
// Fichero:     LogicaFake.java
// Descripción: Representa en el móvil a la lógica del negocio del
//              servidor. Quien la usa llama a guardarMedida() sin saber
//              que por debajo hay una petición HTTP (PeticionarioREST).
// Copyright:   (c) 2026 Matilde Calleja
// Fecha:       14/10/2026
// Autor:       Matilde Calleja
// Aportación:  Diseño propio; código generado con IA a partir del diseño
//              y revisado por la autora.
// ---------------------------------------------------------------------
package com.example.calleja.appbiom;

public class LogicaFake {

    // Se llama en el hilo de la interfaz cuando termina el envío.
    public interface AlTerminar {
        void resultado(boolean ok, String mensaje);
    }

    private final String urlServidor;

    // --------------------------------------------------------------
    // url_servidor: Text --> LogicaFake() -->
    // --------------------------------------------------------------
    public LogicaFake(String urlServidor) {
        this.urlServidor = urlServidor;
    }

    // --------------------------------------------------------------
    // medida: Medida --> aJSON() --> Text
    // { "tipo": .., "valor": .., "contador": .. }
    // --------------------------------------------------------------
    public static String aJSON(Medida medida) {
        return "{\"tipo\":" + medida.getTipo()
                + ",\"valor\":" + medida.getValor()
                + ",\"contador\":" + medida.getContador() + "}";
    }

    // --------------------------------------------------------------
    // medida: Medida, al_terminar: AlTerminar --> guardarMedida() -->
    //
    // 1. POST url_servidor con aJSON(medida)  (con PeticionarioREST)
    // 2. Si la respuesta es 201: al_terminar(true, ..)
    //    Si no: al_terminar(false, "HTTP <código>: <respuesta>")
    //
    // Hay que llamarlo desde el hilo de la interfaz.
    // --------------------------------------------------------------
    public void guardarMedida(Medida medida, AlTerminar alTerminar) {
        new PeticionarioREST().hacerPeticionREST(
                "POST",
                urlServidor,
                aJSON(medida),
                (codigo, cuerpo) -> {
                    if (codigo == 201) {
                        alTerminar.resultado(true, "Medida guardada");
                    } else {
                        alTerminar.resultado(false, "HTTP " + codigo + ": " + cuerpo);
                    }
                });
    }
}
