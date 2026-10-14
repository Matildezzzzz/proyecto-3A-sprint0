// ---------------------------------------------------------------------
// Fichero:     InterpreteMedidaTest.java
// Descripción: Tests unitarios de InterpreteMedida, Medida y
//              LogicaFake.aJSON con tramas iBeacon conocidas.
// Copyright:   (c) 2026 Matilde Calleja
// Fecha:       11/10/2026
// Autor:       Matilde Calleja
// Aportación:  Tests generados con IA a partir del diseño y revisados.
// ---------------------------------------------------------------------
package com.example.calleja.appbiom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class InterpreteMedidaTest {

    private final InterpreteMedida interprete = new InterpreteMedida("EPSG-GTI-PROY-3A");

    // Trama real vista en nRF Connect (sin el id 4C 00):
    // major = 00 0C, minor = 00 22, tx = 49
    private static final byte[] TRAMA_NRF = bytes(
            0x02, 0x15,
            0x45, 0x50, 0x53, 0x47, 0x2D, 0x47, 0x54, 0x49,
            0x2D, 0x50, 0x52, 0x4F, 0x59, 0x2D, 0x33, 0x41,
            0x00, 0x0C, 0x00, 0x22, 0x49);

    // --------------------------------------------------------------
    // Construye una trama con el UUID del proyecto y el major/minor dados.
    // --------------------------------------------------------------
    private static byte[] tramaCon(int tipo, int contador, int valor) {
        byte[] trama = TRAMA_NRF.clone();
        trama[18] = (byte) tipo;
        trama[19] = (byte) contador;
        trama[20] = (byte) (valor >> 8);
        trama[21] = (byte) valor;
        return trama;
    }

    private static byte[] bytes(int... valores) {
        byte[] resultado = new byte[valores.length];
        for (int i = 0; i < valores.length; i++) {
            resultado[i] = (byte) valores[i];
        }
        return resultado;
    }

    @Test
    public void reconoceLaTramaDeNuestroSensor() {
        assertTrue(interprete.esDeNuestroSensor(TRAMA_NRF));
    }

    @Test
    public void leeElUUIDComoTextoASCII() {
        assertEquals("EPSG-GTI-PROY-3A", interprete.leerUUID(TRAMA_NRF));
    }

    @Test
    public void rechazaOtroUUID() {
        byte[] otra = TRAMA_NRF.clone();
        otra[2] = 'X';
        assertFalse(interprete.esDeNuestroSensor(otra));
    }

    @Test
    public void rechazaDatosQueNoSonIBeacon() {
        assertFalse(interprete.esDeNuestroSensor(bytes(0x10, 0x05, 0x01, 0x02)));
        assertFalse(interprete.esDeNuestroSensor(null));
    }

    @Test
    public void interpretaTipoContadorYValor() {
        Medida medida = interprete.interpretar(tramaCon(11, 5, 123));

        assertEquals(11, medida.getTipo());
        assertEquals(5, medida.getContador());
        assertEquals(123, medida.getValor());
        assertEquals("CO2", medida.nombreDelTipo());
    }

    @Test
    public void interpretaValoresNegativos() {
        Medida medida = interprete.interpretar(tramaCon(12, 7, -12));

        assertEquals(-12, medida.getValor());
        assertEquals("Temperatura", medida.nombreDelTipo());
    }

    @Test
    public void interpretaContadorMayorDe127SinSigno() {
        assertEquals(200, interprete.interpretar(tramaCon(11, 200, 1)).getContador());
    }

    @Test
    public void interpretaLaTramaDeNrfConnect() {
        Medida medida = interprete.interpretar(TRAMA_NRF);

        assertEquals(0, medida.getTipo());
        assertEquals(12, medida.getContador());
        assertEquals(34, medida.getValor());
    }

    // Trama real del Arduino nuevo (nRF Connect, 14/10/2026):
    // major = 0B 11 (2833), minor = 00 7B (123), potencia = CB (-53)
    @Test
    public void interpretaLaTramaRealDelArduino() {
        byte[] trama = TRAMA_NRF.clone();
        trama[18] = 0x0B;
        trama[19] = 0x11;
        trama[20] = 0x00;
        trama[21] = 0x7B;
        trama[22] = (byte) 0xCB;

        Medida medida = interprete.interpretar(trama);

        assertEquals(11, medida.getTipo());
        assertEquals(17, medida.getContador());
        assertEquals(123, medida.getValor());
        assertEquals("CO2", medida.nombreDelTipo());
    }

    @Test
    public void convierteLaMedidaAJSON() {
        assertEquals("{\"tipo\":11,\"valor\":-5,\"contador\":3}",
                LogicaFake.aJSON(new Medida(11, -5, 3)));
    }
}
