package com.example.calleja.appbiom;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AppBiom";
    private static final int CODIGO_PERMISOS = 11223344;
    private static final int CODIGO_ACTIVAR_BLUETOOTH = 11223345;

    private static final String NOMBRE_ARDUINO = "Matilde";
    private static final String UUID_PROYECTO = "EPSG-GTI-PROY-3A";
    private static final int ID_FABRICANTE_APPLE = 0x004C;

    private BluetoothAdapter adaptadorBluetooth;
    private BluetoothLeScanner escaner;
    private ScanCallback callbackEscaneo;

    private TextView textoEstado;
    private TextView textoUltimaMedida;
    private TextView textoResultadoEnvio;

    private boolean buscarSoloArduino = false;
    private boolean escaneoActivo = false;

    // Se mantiene un contador por tipo de sensor para no repetir lecturas.
    private final int[] ultimoContadorPorTipo = new int[256];
    private final boolean[] hayContadorPorTipo = new boolean[256];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textoEstado = findViewById(R.id.textoEstado);
        textoUltimaMedida = findViewById(R.id.textoUltimaMedida);
        textoResultadoEnvio = findViewById(R.id.textoResultadoEnvio);

        textoEstado.setText("Detenido");
        textoUltimaMedida.setText("Todavía no se han recibido medidas");
        textoResultadoEnvio.setText("Envío al servidor pendiente");

        inicializarBluetooth();
    }

    private boolean tienePermisosNecesarios() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(
                    this, Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                    this, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED;
        }

        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void pedirPermisosNecesarios() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT
                    },
                    CODIGO_PERMISOS
            );
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    CODIGO_PERMISOS
            );
        }
    }

    private void inicializarBluetooth() {
        BluetoothManager manager =
                (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);

        if (manager == null) {
            textoEstado.setText("Error: Bluetooth no disponible");
            return;
        }

        adaptadorBluetooth = manager.getAdapter();

        if (adaptadorBluetooth == null) {
            textoEstado.setText("Este móvil no tiene Bluetooth");
            return;
        }

        if (!tienePermisosNecesarios()) {
            textoEstado.setText("Esperando permisos de Bluetooth...");
            pedirPermisosNecesarios();
            return;
        }

        prepararEscaner();
    }

    private void prepararEscaner() {
        if (adaptadorBluetooth == null) {
            return;
        }

        try {
            if (!adaptadorBluetooth.isEnabled()) {
                textoEstado.setText("Activa el Bluetooth para continuar");
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, CODIGO_ACTIVAR_BLUETOOTH);
                return;
            }

            escaner = adaptadorBluetooth.getBluetoothLeScanner();

            if (escaner == null) {
                textoEstado.setText("No se ha podido iniciar el escáner BLE");
            } else {
                textoEstado.setText("Bluetooth preparado. Pulsa un botón para buscar.");
            }
        } catch (SecurityException e) {
            textoEstado.setText("Faltan permisos de Bluetooth");
            Log.e(TAG, "No se pudo preparar Bluetooth", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CODIGO_PERMISOS) {
            if (tienePermisosNecesarios()) {
                prepararEscaner();
            } else {
                textoEstado.setText("Permisos denegados. No se puede buscar.");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODIGO_ACTIVAR_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                prepararEscaner();
            } else {
                textoEstado.setText("Bluetooth desactivado");
            }
        }
    }

    public void botonBuscarDispositivosBTLEPulsado(View view) {
        buscarSoloArduino = false;
        iniciarEscaneo(false);
    }

    public void botonBuscarNuestroDispositivoBTLEPulsado(View view) {
        buscarSoloArduino = true;
        iniciarEscaneo(true);
    }

    public void botonDetenerBusquedaDispositivosBTLEPulsado(View view) {
        detenerEscaneo();
        textoEstado.setText("Detenido");
    }

    private void iniciarEscaneo(boolean soloArduino) {
        if (!tienePermisosNecesarios()) {
            pedirPermisosNecesarios();
            textoEstado.setText("Concede los permisos y vuelve a pulsar el botón");
            return;
        }

        if (escaner == null) {
            prepararEscaner();
            if (escaner == null) {
                return;
            }
        }

        detenerEscaneo();
        buscarSoloArduino = soloArduino;

        callbackEscaneo = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                procesarDispositivoEncontrado(resultado);
            }

            @Override
            public void onScanFailed(int errorCode) {
                runOnUiThread(() ->
                        textoEstado.setText("Error de escaneo Bluetooth: " + errorCode));
                Log.e(TAG, "Error de escaneo: " + errorCode);
            }


        };

        try {
            // Escaneo sin filtro del sistema: algunos dispositivos no anuncian
            // su nombre en todos los paquetes. Filtramos por nombre en Java.
            escaner.startScan(
                    Collections.<ScanFilter>emptyList(),
                    new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .build(),
                    callbackEscaneo
            );

            escaneoActivo = true;
            textoEstado.setText(soloArduino
                    ? "Buscando " + NOMBRE_ARDUINO + "..."
                    : "Buscando todos los dispositivos BLE...");
        } catch (SecurityException e) {
            escaneoActivo = false;
            textoEstado.setText("Sin permisos para escanear Bluetooth");
            Log.e(TAG, "No se pudo iniciar el escaneo", e);
        }
    }
    private void procesarDispositivoEncontrado(ScanResult resultado) {
        BluetoothDevice dispositivo = resultado.getDevice();
        ScanRecord registro = resultado.getScanRecord();

        Log.d(TAG, "ANUNCIO COMPLETO: " + registro);
        Log.d(TAG, "NOMBRE DEL ANUNCIO: " + registro.getDeviceName());

        byte[] fabricante =
                registro.getManufacturerSpecificData(0x004C);

        if (fabricante != null) {
            StringBuilder hex = new StringBuilder();

            for (byte b : fabricante) {
                hex.append(String.format("%02X ", b & 0xFF));
            }

            Log.d(TAG, "DATOS APPLE: " + hex);
        } else {
            Log.d(TAG, "SIN DATOS DE FABRICANTE APPLE");
        }

        if (registro == null) {
            return;
        }

        String nombre = registro.getDeviceName();

        if (nombre == null || nombre.isEmpty()) {
            try {
                nombre = dispositivo.getName();
            } catch (SecurityException e) {
                Log.e(TAG, "No se puede leer el nombre", e);
            }
        }

        final String nombreFinal =
                (nombre == null) ? "(sin nombre)" : nombre;

        int rssi = resultado.getRssi();

        Log.d(TAG, "BLE: " + nombreFinal + " RSSI=" + rssi);

        // Buscar los datos iBeacon de Apple.
        byte[] datosFabricante =
                registro.getManufacturerSpecificData(ID_FABRICANTE_APPLE);

        boolean esNuestroBeacon = false;

        if (datosFabricante != null && datosFabricante.length >= 23) {
            boolean prefijoCorrecto =
                    (datosFabricante[0] & 0xFF) == 0x02
                            && (datosFabricante[1] & 0xFF) == 0x15;

            if (prefijoCorrecto) {
                byte[] uuidBytes = new byte[16];

                System.arraycopy(
                        datosFabricante, 2, uuidBytes, 0, 16);

                String uuid = new String(
                        uuidBytes, StandardCharsets.US_ASCII);

                esNuestroBeacon = UUID_PROYECTO.equals(uuid);

                Log.d(TAG, "Beacon detectado: " + nombreFinal
                        + ", UUID=" + uuid);
            }
        }

        if (buscarSoloArduino) {
            // Identificar por UUID, no por el nombre.
            if (!esNuestroBeacon) {
                return;
            }

            runOnUiThread(() ->
                    textoEstado.setText(
                            "Beacon encontrado: " + nombreFinal));

            procesarTramaIBeacon(datosFabricante);
        } else {
            runOnUiThread(() ->
                    textoEstado.setText(
                            "Buscando todos... Último: "
                                    + nombreFinal + " (RSSI "
                                    + rssi + " dBm)"));
        }
    }

    private void procesarTramaIBeacon(byte[] datos) {
        // Los datos de fabricante iBeacon normalmente tienen esta estructura:
        // 0x02, 0x15, UUID (16 bytes), major (2), minor (2), txPower (1).
        if (datos == null || datos.length < 23) {
            Log.d(TAG, "Datos iBeacon demasiado cortos");
            return;
        }

        if ((datos[0] & 0xFF) != 0x02 || (datos[1] & 0xFF) != 0x15) {
            Log.d(TAG, "Los datos no tienen el prefijo iBeacon esperado");
            return;
        }

        byte[] uuidBytes = new byte[16];
        System.arraycopy(datos, 2, uuidBytes, 0, 16);
        String uuid = new String(uuidBytes, StandardCharsets.US_ASCII);

        if (!UUID_PROYECTO.equals(uuid)) {
            Log.d(TAG, "UUID ignorado: " + uuid);
            return;
        }

        int major = ((datos[18] & 0xFF) << 8) | (datos[19] & 0xFF);
        int minor = ((datos[20] & 0xFF) << 8) | (datos[21] & 0xFF);

        /*
         * IMPORTANTE: esta interpretación supone que el Arduino codifica:
         * byte alto de major = tipo de sensor (11 CO2, 12 temperatura,
         * 13 ruido, 14 NO2); byte bajo de major = contador (0..255);
         * minor = valor numérico de la medida.
         *
         * Si el emisor usa otra distribución, hay que cambiar estas líneas
         * para que coincidan con el código Arduino.
         */
        int tipo = (major >> 8) & 0xFF;
        int contador = major & 0xFF;
        int valor = minor;

        if (hayContadorPorTipo[tipo]
                && ultimoContadorPorTipo[tipo] == contador) {
            Log.d(TAG, "Medida repetida ignorada. Tipo=" + tipo
                    + ", contador=" + contador);
            return;
        }

        ultimoContadorPorTipo[tipo] = contador;
        hayContadorPorTipo[tipo] = true;

        String nombreTipo = nombreTipoSensor(tipo);
        String medida = String.format(
                Locale.getDefault(),
                "%s = %d (contador %d)",
                nombreTipo, valor, contador
        );

        runOnUiThread(() -> {
            textoEstado.setText("Recibiendo medidas de " + NOMBRE_ARDUINO);
            textoUltimaMedida.setText(medida);
            textoResultadoEnvio.setText("Envío desactivado (pendiente)");
        });

        Log.d(TAG, "UUID correcto: " + uuid);
        Log.d(TAG, "major=" + major + ", minor=" + minor);
        Log.d(TAG, "Medida nueva: " + medida);
    }

    private String nombreTipoSensor(int tipo) {
        switch (tipo) {
            case 11:
                return "CO2";
            case 12:
                return "Temperatura";
            case 13:
                return "Ruido";
            case 14:
                return "NO2";
            default:
                return "Sensor " + tipo;
        }
    }

    private void detenerEscaneo() {
        if (escaner != null && callbackEscaneo != null && escaneoActivo) {
            try {
                escaner.stopScan(callbackEscaneo);
            } catch (SecurityException e) {
                Log.e(TAG, "No se pudo detener el escaneo", e);
            }
        }

        callbackEscaneo = null;
        escaneoActivo = false;
    }

    @Override
    protected void onDestroy() {
        detenerEscaneo();
        super.onDestroy();
    }
}
