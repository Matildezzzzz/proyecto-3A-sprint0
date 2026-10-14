// ---------------------------------------------------------------------
// Fichero:     Medida.java
// Descripción: Medida recibida del sensor: tipo, valor y contador.
// Copyright:   (c) 2026 Matilde Calleja
// Fecha:       11/10/2026
// Autor:       Matilde Calleja
// Aportación:  Diseño propio; código generado con IA a partir del diseño
//              y revisado por la autora.
// ---------------------------------------------------------------------
package com.example.calleja.appbiom;

public class Medida {

    private final int tipo;
    private final int valor;
    private final int contador;

    // --------------------------------------------------------------
    // tipo: N, valor: Z, contador: N --> Medida() -->
    // --------------------------------------------------------------
    public Medida(int tipo, int valor, int contador) {
        this.tipo = tipo;
        this.valor = valor;
        this.contador = contador;
    }

    // --------------------------------------------------------------
    // N <-- getTipo() <--
    // --------------------------------------------------------------
    public int getTipo() {
        return tipo;
    }

    // --------------------------------------------------------------
    // Z <-- getValor() <--
    // --------------------------------------------------------------
    public int getValor() {
        return valor;
    }

    // --------------------------------------------------------------
    // N <-- getContador() <--
    // --------------------------------------------------------------
    public int getContador() {
        return contador;
    }

    // --------------------------------------------------------------
    // Text <-- nombreDelTipo() <--
    // 11 -> CO2, 12 -> Temperatura, 13 -> Ruido, otro -> Desconocido
    // --------------------------------------------------------------
    public String nombreDelTipo() {
        switch (tipo) {
            case 11: return "CO2";
            case 12: return "Temperatura";
            case 13: return "Ruido";
            default: return "Desconocido";
        }
    }
}
