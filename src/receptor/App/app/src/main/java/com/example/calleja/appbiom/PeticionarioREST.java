// ---------------------------------------------------------------------
// Fichero:     PeticionarioREST.java
// Descripción: Hace una petición HTTP en segundo plano y devuelve el
//              código y el cuerpo de la respuesta en el hilo de la
//              interfaz.
// Copyright:   (c) 2026 Matilde Calleja
// Fecha:       14/10/2026
// Autor:       Matilde Calleja (a partir del código de
//              Jordi Bataller i Mascarell)
// Aportación:  Código de la asignatura revisado; se lee también la
//              respuesta de error del servidor.
// ---------------------------------------------------------------------
package com.example.calleja.appbiom;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class PeticionarioREST extends AsyncTask<Void, Void, Boolean> {

    public interface RespuestaREST {
        void callback(int codigo, String cuerpo);
    }

    private static final int TIEMPO_MAXIMO_MS = 10000;

    private String elMetodo;
    private String urlDestino;
    private String elCuerpo;
    private RespuestaREST laRespuesta;

    private int codigoRespuesta = -1;
    private String cuerpoRespuesta = "";

    // --------------------------------------------------------------
    // metodo: Text, url: Text, cuerpo: Text, respuesta: RespuestaREST
    //     --> hacerPeticionREST() -->
    // Guarda los datos y lanza la petición en segundo plano.
    // --------------------------------------------------------------
    public void hacerPeticionREST(String metodo, String urlDestino, String cuerpo, RespuestaREST respuesta) {
        this.elMetodo = metodo;
        this.urlDestino = urlDestino;
        this.elCuerpo = cuerpo;
        this.laRespuesta = respuesta;

        execute();
    }

    // --------------------------------------------------------------
    // B <-- doInBackground() <--      (hilo secundario)
    //
    // 1. Abrir la conexión con el método y Content-Type JSON.
    // 2. Si no es GET: escribir el cuerpo.
    // 3. Leer el código y el cuerpo de la respuesta (o del error).
    // 4. Devolver true si el código está entre 200 y 399.
    // --------------------------------------------------------------
    @Override
    protected Boolean doInBackground(Void... params) {
        HttpURLConnection conexion = null;

        try {
            conexion = (HttpURLConnection) new URL(urlDestino).openConnection();
            conexion.setConnectTimeout(TIEMPO_MAXIMO_MS);
            conexion.setReadTimeout(TIEMPO_MAXIMO_MS);
            conexion.setRequestMethod(elMetodo);
            conexion.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conexion.setDoInput(true);

            if (!"GET".equals(elMetodo) && elCuerpo != null) {
                conexion.setDoOutput(true);
                try (DataOutputStream salida = new DataOutputStream(conexion.getOutputStream())) {
                    salida.write(elCuerpo.getBytes(StandardCharsets.UTF_8));
                    salida.flush();
                }
            }

            codigoRespuesta = conexion.getResponseCode();

            InputStream flujo = (codigoRespuesta >= 200 && codigoRespuesta < 400)
                    ? conexion.getInputStream()
                    : conexion.getErrorStream();

            if (flujo != null) {
                try (BufferedReader lector = new BufferedReader(
                        new InputStreamReader(flujo, StandardCharsets.UTF_8))) {
                    StringBuilder respuesta = new StringBuilder();
                    String linea;
                    while ((linea = lector.readLine()) != null) {
                        respuesta.append(linea);
                    }
                    cuerpoRespuesta = respuesta.toString();
                }
            }

            return codigoRespuesta >= 200 && codigoRespuesta < 400;

        } catch (Exception ex) {
            Log.e("AppBiom", "Error en petición REST", ex);
            cuerpoRespuesta = ex.getMessage() != null ? ex.getMessage() : "Error de conexión";
            return false;

        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }

    // --------------------------------------------------------------
    // correcto: B --> onPostExecute() -->      (hilo de la interfaz)
    // Llama a la respuesta con el código y el cuerpo.
    // --------------------------------------------------------------
    @Override
    protected void onPostExecute(Boolean correcto) {
        if (laRespuesta != null) {
            laRespuesta.callback(codigoRespuesta, cuerpoRespuesta);
        }
    }
}
