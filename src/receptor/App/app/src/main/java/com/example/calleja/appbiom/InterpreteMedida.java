// ---------------------------------------------------------------------
// Fichero:     InterpreteMedida.java
// Descripción: Convierte los datos de fabricante de un iBeacon en una
//              Medida. No depende de Android (se prueba con JUnit).
// Copyright:   (c) 2026 Matilde Calleja
// Fecha:       11/10/2026
// Autor:       Matilde Calleja
// Aportación:  Diseño propio; código generado con IA a partir del diseño
//              y revisado por la autora.
// ---------------------------------------------------------------------
package com.example.calleja.appbiom;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class InterpreteMedida {

    private static final int LONGITUD_IBEACON = 23;

    private final String uuidEsperado;

    // --------------------------------------------------------------
    // uuid_esperado: Text --> InterpreteMedida() -->
    // --------------------------------------------------------------
    public InterpreteMedida(String uuidEsperado) {
        this.uuidEsperado = uuidEsperado;
    }

    // --------------------------------------------------------------
    // datos: [Z] --> esDeNuestroSensor() --> B
    //
    // datos = datos de fabricante Apple SIN el id 4C 00:
    //   [0]=02 [1]=15 [2..17]=uuid [18..19]=major [20..21]=minor [22]=tx
    // Devuelve true si es un iBeacon y su UUID (ASCII) es el esperado.
    // --------------------------------------------------------------
    public boolean esDeNuestroSensor(byte[] datos) {
        if (datos == null || datos.length < LONGITUD_IBEACON) {
            return false;
        }

        boolean esIBeacon = (datos[0] & 0xFF) == 0x02 && (datos[1] & 0xFF) == 0x15;

        return esIBeacon && uuidEsperado.equals(leerUUID(datos));
    }

    // --------------------------------------------------------------
    // datos: [Z] --> leerUUID() --> Text
    // --------------------------------------------------------------
    public String leerUUID(byte[] datos) {
        byte[] uuid = Arrays.copyOfRange(datos, 2, 18);
        return new String(uuid, StandardCharsets.US_ASCII);
    }

    // --------------------------------------------------------------
    // datos: [Z] --> interpretar() --> Medida
    //
    // tipo     = datos[18]  (byte alto del major, sin signo)
    // contador = datos[19]  (byte bajo del major, sin signo)
    // valor    = datos[20..21] como entero de 16 bits CON signo
    // --------------------------------------------------------------
    public Medida interpretar(byte[] datos) {
        int tipo = datos[18] & 0xFF;
        int contador = datos[19] & 0xFF;
        int valor = (short) (((datos[20] & 0xFF) << 8) | (datos[21] & 0xFF));

        return new Medida(tipo, valor, contador);
    }
}
