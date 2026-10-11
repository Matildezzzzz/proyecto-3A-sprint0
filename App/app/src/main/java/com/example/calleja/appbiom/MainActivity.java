package com.example.calleja.appbiom;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {

    private static final String TAG = "AppBiom";

    private static final int CODIGO_PERMISOS = 1001;
    private static final int TIEMPO_ESCANEO_MS = 15000;

    private static final int ID_FABRICANTE_APPLE = 0x004C;
    private static final String UUID_PROYECTO = "EPSG-GTI-PROY-3A";
    private static final String NOMBRE_ARDUINO = "Matilde";

    private BluetoothAdapter adaptador;
    private BluetoothLeScanner escaner;
    private ScanCallback callback;

    private TextView textoEstado;
    private TextView textoUltimaMedida;
    private TextView textoResultadoEnvio;

    private Button botonBuscar;
    private Button botonBuscarNuestro;
    private Button botonDetener;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Set<String> dispositivosMostrados = new HashSet<>();

    private boolean escaneando = false;
    private int anunciosRecibidos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cargar la interfaz diseñada en XML
        setContentView(R.layout.activity_main);

        // Enlazar los textos del XML
        textoEstado = findViewById(R.id.textoEstado);
        textoUltimaMedida = findViewById(R.id.textoUltimaMedida);
        textoResultadoEnvio = findViewById(R.id.textoResultadoEnvio);

        // Enlazar los tres botones
        botonBuscarNuestro =
                findViewById(R.id.botonBuscarNuestroDispositivoBTLE);

        botonBuscar =
                findViewById(R.id.botonBuscarDispositivosBTLE);

        botonDetener =
                findViewById(R.id.botonDetenerBusquedaDispositivosBTLE);

        // Obtener Bluetooth
        BluetoothManager manager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        if (manager != null) {
            adaptador = manager.getAdapter();
        }

        // Conectar los botones con sus acciones
        botonBuscar.setOnClickListener(v -> comprobarPermisosYBuscar());

        botonBuscarNuestro.setOnClickListener(v -> comprobarPermisosYBuscar());

        botonDetener.setOnClickListener(v -> detenerEscaneo());

        // Estado inicial
        if (adaptador == null) {
            mostrarEstado("Este móvil no dispone de Bluetooth.");
            botonBuscar.setEnabled(false);
            botonBuscarNuestro.setEnabled(false);
            return;
        }

        mostrarEstado("Listo. Enciende el Arduino y pulsa Buscar.");
    }

    // ------------------------------------------------------------------
    // CAMBIO: en Android 12+ también se pide ubicación, porque sin ella
    // Android no entrega los anuncios iBeacon a la app.
    // ------------------------------------------------------------------
    private String[] permisosNecesarios() {
        List<String> permisos = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permisos.add(Manifest.permission.BLUETOOTH_SCAN);
            permisos.add(Manifest.permission.BLUETOOTH_CONNECT);
        }

        permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);

        return permisos.toArray(new String[0]);
    }

    private boolean tienePermisos() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        for (String permiso : permisosNecesarios()) {
            if (checkSelfPermission(permiso) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "PASO 0 | Falta el permiso " + permiso);
                return false;
            }
        }

        return true;
    }

    // ------------------------------------------------------------------
    // CAMBIO: muchos móviles no entregan resultados de escaneo BLE si la
    // ubicación del sistema (el "GPS" del panel rápido) está apagada.
    // ------------------------------------------------------------------
    private boolean ubicacionActivada() {
        LocationManager lm =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (lm == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return lm.isLocationEnabled();
        }

        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void comprobarPermisosYBuscar() {
        if (!tienePermisos()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permisosNecesarios(), CODIGO_PERMISOS);
            }
            return;
        }

        iniciarEscaneo();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CODIGO_PERMISOS) {
            if (tienePermisos()) {
                iniciarEscaneo();
            } else {
                mostrarEstado(
                        "Faltan permisos (Bluetooth y Ubicación). "
                                + "Autorízalos en Ajustes > Aplicaciones > AppBiom."
                );
            }
        }
    }

    private void iniciarEscaneo() {
        if (!tienePermisos()) {
            comprobarPermisosYBuscar();
            return;
        }

        if (adaptador == null || !adaptador.isEnabled()) {
            mostrarEstado("Activa el Bluetooth del móvil.");
            return;
        }

        if (!ubicacionActivada()) {
            mostrarEstado("Activa la Ubicación del móvil (panel rápido) y vuelve a buscar.");
            Log.w(TAG, "PASO 0 | La ubicación del sistema está desactivada");
            return;
        }

        escaner = adaptador.getBluetoothLeScanner();

        if (escaner == null) {
            mostrarEstado("No se ha podido obtener el escáner BLE.");
            return;
        }

        detenerEscaneo();

        dispositivosMostrados.clear();
        anunciosRecibidos = 0;
        textoUltimaMedida.setText("");

        callback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                procesarResultado(resultado);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> resultados) {
                for (ScanResult resultado : resultados) {
                    procesarResultado(resultado);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                escaneando = false;

                runOnUiThread(() -> {
                    mostrarEstado("Error al escanear: " + errorCode);
                    botonBuscar.setEnabled(true);
                    botonDetener.setEnabled(false);
                });

                Log.e(TAG, "Fallo de escaneo: " + errorCode);
            }
        };

        ScanSettings ajustes = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        try {
            // Sin filtros: recibe anuncios de todos los dispositivos BLE.
            escaner.startScan(null, ajustes, callback);

            escaneando = true;
            botonBuscar.setEnabled(false);
            botonDetener.setEnabled(true);
            mostrarEstado("Buscando dispositivos BLE durante 15 segundos...");
            Log.i(TAG, "PASO 0 | Escaneo iniciado (permisos OK, BT OK, ubicación OK)");

            handler.postDelayed(() -> {
                if (escaneando) {
                    detenerEscaneo();
                    mostrarEstado("Búsqueda finalizada. Revisa los resultados.");
                    Log.i(TAG, "Escaneo terminado. Anuncios recibidos: " + anunciosRecibidos);
                }
            }, TIEMPO_ESCANEO_MS);

        } catch (SecurityException e) {
            mostrarEstado("Android ha denegado el permiso Bluetooth.");
            Log.e(TAG, "Sin permiso para escanear", e);
        }
    }

    private void procesarResultado(ScanResult resultado) {
        anunciosRecibidos++;

        ScanRecord registro = resultado.getScanRecord();

        if (registro == null) {
            Log.d(TAG, "Anuncio recibido sin ScanRecord");
            return;
        }

        String nombre = registro.getDeviceName();

        if (nombre == null || nombre.isEmpty()) {
            nombre = "(sin nombre en el anuncio)";
        }

        int rssi = resultado.getRssi();

        // ------------------------------------------------------------
        // PASO 1: ¿llega algún anuncio del Arduino? (por nombre, que va
        // en la respuesta al escaneo). Se muestran los bytes en bruto.
        // ------------------------------------------------------------
        if (NOMBRE_ARDUINO.equals(nombre)) {
            Log.i(TAG, "PASO 1 | Anuncio de " + NOMBRE_ARDUINO
                    + " RSSI=" + rssi
                    + " bytes=" + bytesHex(registro.getBytes()));
        }

        SparseArray<byte[]> fabricantes = registro.getManufacturerSpecificData();
        byte[] datosApple = fabricantes == null ? null : fabricantes.get(ID_FABRICANTE_APPLE);

        // ------------------------------------------------------------
        // PASO 2: datos de fabricante Apple (0x004C). Android ya quita
        // los 2 bytes del ID, así que una trama iBeacon empieza por 02 15
        // y mide 23 bytes.
        // ------------------------------------------------------------
        if (datosApple != null) {
            Log.d(TAG, "PASO 2 | Apple de " + nombre
                    + ", longitud=" + datosApple.length
                    + ", datos=" + bytesHex(datosApple));
        }

        boolean esNuestroBeacon = false;
        int major = -1;
        int minor = -1;
        int txPower = 0;
        String uuid = "";

        if (datosApple != null
                && datosApple.length >= 23
                && (datosApple[0] & 0xFF) == 0x02
                && (datosApple[1] & 0xFF) == 0x15) {

            byte[] uuidBytes = Arrays.copyOfRange(datosApple, 2, 18);
            uuid = new String(uuidBytes, StandardCharsets.US_ASCII);

            major = ((datosApple[18] & 0xFF) << 8) | (datosApple[19] & 0xFF);
            minor = ((datosApple[20] & 0xFF) << 8) | (datosApple[21] & 0xFF);
            txPower = datosApple[22];

            esNuestroBeacon = UUID_PROYECTO.equals(uuid);

            // PASO 3: ¿el UUID coincide?
            Log.i(TAG, "PASO 3 | iBeacon detectado. UUID=[" + uuid
                    + "] coincide=" + esNuestroBeacon
                    + " major=" + major + " minor=" + minor);
        }

        final String nombreFinal = nombre;
        final String uuidFinal = uuid;
        final int majorFinal = major;
        final int minorFinal = minor;
        final int txPowerFinal = txPower;
        final boolean encontrado = esNuestroBeacon;

        // Mostrar cada dispositivo una sola vez, excepto el beacon objetivo.
        String clave = nombreFinal + "|" + Arrays.toString(
                datosApple == null ? new byte[0] : datosApple
        );

        if (!encontrado && !dispositivosMostrados.add(clave)) {
            return;
        }

        runOnUiThread(() -> {
            if (encontrado) {
                mostrarEstado("¡BEACON ENCONTRADO: " + nombreFinal + "!");
                agregarResultado(
                        "***** NUESTRO BEACON *****\n"
                                + "Nombre: " + nombreFinal + "\n"
                                + "UUID: " + uuidFinal + "\n"
                                + "Major: " + majorFinal + "\n"
                                + "Minor: " + minorFinal + "\n"
                                + "Tx Power anunciado: " + txPowerFinal + "\n"
                                + "RSSI: " + rssi + " dBm\n\n"
                );
                enviarMedicionesPost();
            } else {
                String detalle = "Dispositivo: " + nombreFinal
                        + " | RSSI: " + rssi + " dBm";

                if (datosApple != null) {
                    detalle += "\nFabricante 0x004C: " + bytesHex(datosApple);
                }

                agregarResultado(detalle + "\n\n");
            }
        });
    }

    private String bytesHex(byte[] datos) {
        if (datos == null) {
            return "(null)";
        }

        StringBuilder resultado = new StringBuilder();

        for (byte b : datos) {
            resultado.append(String.format(Locale.US, "%02X ", b & 0xFF));
        }

        return resultado.toString().trim();
    }

    private void agregarResultado(String texto) {
        textoUltimaMedida.append(texto);
    }

    private void mostrarEstado(String texto) {
        runOnUiThread(() -> textoEstado.setText(texto));
    }

    private void detenerEscaneo() {
        handler.removeCallbacksAndMessages(null);

        if (escaner != null && callback != null && escaneando) {
            try {
                if (tienePermisos()) {
                    escaner.stopScan(callback);
                }
            } catch (SecurityException e) {
                Log.e(TAG, "No se pudo detener el escaneo", e);
            }
        }

        escaneando = false;

        if (botonBuscar != null) {
            botonBuscar.setEnabled(true);
        }

        if (botonDetener != null) {
            botonDetener.setEnabled(false);
        }
    }

    @Override
    protected void onDestroy() {
        detenerEscaneo();
        super.onDestroy();
    }

    private void enviarMedicionesPost() {
        new Thread(() -> {
            HttpURLConnection conexion = null;

            try {
                // Cambia esta URL por la dirección real de tu API.
                URL url = new URL("https://mcalgar2.upv.edu.es/api/mediciones.php");

                JSONObject datos = new JSONObject();
                datos.put("dispositivo", "Matilde");
                datos.put("co2", 420);
                datos.put("temperatura", 666);
                datos.put("ruido", 333);
                datos.put("no2", 02);

                conexion = (HttpURLConnection) url.openConnection();
                conexion.setRequestMethod("POST");
                conexion.setConnectTimeout(5000);
                conexion.setReadTimeout(5000);
                conexion.setDoOutput(true);
                conexion.setRequestProperty(
                        "Content-Type", "application/json; charset=UTF-8"
                );

                byte[] cuerpo = datos.toString().getBytes(
                        java.nio.charset.StandardCharsets.UTF_8
                );

                try (OutputStream salida = conexion.getOutputStream()) {
                    salida.write(cuerpo);
                }

                int codigo = conexion.getResponseCode();

                runOnUiThread(() ->
                        textoResultadoEnvio.setText(
                                "POST enviado. Respuesta HTTP: " + codigo
                        )
                );

            } catch (Exception e) {
                Log.e(TAG, "Error al enviar POST", e);

                runOnUiThread(() ->
                        textoResultadoEnvio.setText(
                                "Error al enviar POST: " + e.getMessage()
                        )
                );

            } finally {
                if (conexion != null) {
                    conexion.disconnect();
                }
            }
        }).start();
    }
}