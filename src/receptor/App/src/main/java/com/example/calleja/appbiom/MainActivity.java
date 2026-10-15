// ---------------------------------------------------------------------
// Fichero:     MainActivity.java
// Descripción: Pantalla principal (activity_main.xml). Pide permisos al
//              abrirse, escanea BLE y muestra lo que pasa. La
//              interpretación de la medida la hace InterpreteMedida y el
//              envío al servidor LogicaFake.
// Copyright:   (c) 2026 Matilde Calleja
// Fecha:       14/10/2026
// Autor:       Matilde Calleja
// Aportación:  Diseño propio; código generado con IA a partir del diseño
//              y revisado por la autora.
// ---------------------------------------------------------------------
package com.example.calleja.appbiom;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {

    private static final String TAG = "AppBiom";

    private static final int CODIGO_PERMISOS = 1001;
    private static final int ID_FABRICANTE_APPLE = 0x004C;
    private static final String UUID_PROYECTO = "EPSG-GTI-PROY-3A";
    private static final String NOMBRE_NUESTRO_DISPOSITIVO = "GTI3A-Matilde";
    private static final String URL_SERVIDOR = "https://mcalgar2.upv.edu.es/api/mediciones.php";

    private static final int COLOR_OK = Color.parseColor("#0F766E");
    private static final int COLOR_ERROR = Color.parseColor("#B91C1C");
    private static final int COLOR_PENDIENTE = Color.parseColor("#92400E");

    private BluetoothAdapter adaptador;
    private BluetoothLeScanner escaner;
    private ScanCallback callback;

    private final InterpreteMedida interprete = new InterpreteMedida(UUID_PROYECTO);
    private final LogicaFake logica = new LogicaFake(URL_SERVIDOR);

    private Button botonBuscarNuestro;
    private Button botonBuscarTodos;
    private Button botonDetener;
    private TextView textoEstado;
    private TextView textoUltimaMedida;
    private TextView textoResultadoEnvio;

    private final Set<String> dispositivosEncontrados = new HashSet<>();

    private boolean escaneando = false;
    private boolean enviarAlServidor = false;
    private int ultimoContadorEnviado = -1;

    // --------------------------------------------------------------
    // onCreate() -->
    //
    // 1. Cargar activity_main.xml y buscar sus vistas.
    //    (Los botones llaman a sus métodos con android:onClick.)
    // 2. Obtener el adaptador Bluetooth.
    // 3. Si faltan permisos: pedirPermisos() (al abrir la app).
    // --------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        botonBuscarNuestro = findViewById(R.id.botonBuscarNuestroDispositivoBTLE);
        botonBuscarTodos = findViewById(R.id.botonBuscarDispositivosBTLE);
        botonDetener = findViewById(R.id.botonDetenerBusquedaDispositivosBTLE);
        textoEstado = findViewById(R.id.textoEstado);
        textoUltimaMedida = findViewById(R.id.textoUltimaMedida);
        textoResultadoEnvio = findViewById(R.id.textoResultadoEnvio);

        actualizarBotones();

        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (manager != null) {
            adaptador = manager.getAdapter();
        }

        if (adaptador == null) {
            mostrarEstado("Este móvil no tiene Bluetooth");
            botonBuscarNuestro.setEnabled(false);
            botonBuscarTodos.setEnabled(false);
            return;
        }

        if (!tienePermisos()) {
            pedirPermisos();
        }
    }

    // --------------------------------------------------------------
    // vista: View --> botonBuscarNuestroDispositivoBTLEPulsado() -->
    // Escanea solo GTI3A-2026 y activa el envío al servidor.
    // --------------------------------------------------------------
    public void botonBuscarNuestroDispositivoBTLEPulsado(View vista) {
        enviarAlServidor = true;
        iniciarEscaneo(NOMBRE_NUESTRO_DISPOSITIVO);
    }

    // --------------------------------------------------------------
    // vista: View --> botonBuscarDispositivosBTLEPulsado() -->
    // Escanea todo lo de alrededor. NO envía nada al servidor.
    // --------------------------------------------------------------
    public void botonBuscarDispositivosBTLEPulsado(View vista) {
        enviarAlServidor = false;
        iniciarEscaneo(null);
    }

    // --------------------------------------------------------------
    // vista: View --> botonDetenerBusquedaDispositivosBTLEPulsado() -->
    // --------------------------------------------------------------
    public void botonDetenerBusquedaDispositivosBTLEPulsado(View vista) {
        detenerEscaneo();
        mostrarEstado("Detenido");
    }

    // --------------------------------------------------------------
    // [Text] <-- permisosNecesarios() <--
    // Android 12+: BLUETOOTH_SCAN y BLUETOOTH_CONNECT.
    // Siempre: ACCESS_FINE_LOCATION.
    // --------------------------------------------------------------
    private String[] permisosNecesarios() {
        List<String> permisos = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permisos.add(Manifest.permission.BLUETOOTH_SCAN);
            permisos.add(Manifest.permission.BLUETOOTH_CONNECT);
        }

        permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);
        return permisos.toArray(new String[0]);
    }

    // --------------------------------------------------------------
    // B <-- tienePermisos() <--
    // --------------------------------------------------------------
    private boolean tienePermisos() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        for (String permiso : permisosNecesarios()) {
            if (checkSelfPermission(permiso) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    // --------------------------------------------------------------
    // pedirPermisos() -->
    // --------------------------------------------------------------
    private void pedirPermisos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permisosNecesarios(), CODIGO_PERMISOS);
        }
    }

    // --------------------------------------------------------------
    // codigo: N, permisos: [Text], resultados: [Z]
    //     --> onRequestPermissionsResult() -->
    // --------------------------------------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != CODIGO_PERMISOS) {
            return;
        }

        mostrarEstado(tienePermisos()
                ? "Detenido"
                : "Faltan permisos. Actívalos en Ajustes > Aplicaciones > AppBiom");
    }

    // --------------------------------------------------------------
    // B <-- ubicacionActivada() <--
    // --------------------------------------------------------------
    private boolean ubicacionActivada() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (lm == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return lm.isLocationEnabled();
        }

        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // --------------------------------------------------------------
    // nombre: Text | nulo --> iniciarEscaneo() -->
    //
    // 1. Si faltan permisos: pedirlos y terminar.
    // 2. Si Bluetooth o Ubicación están apagados: avisar y terminar.
    // 3. Escanear sin filtro (nombre nulo) o filtrando por nombre,
    //    con baja latencia y sin límite de tiempo.
    // 4. Por cada resultado: procesarResultado(resultado)
    // --------------------------------------------------------------
    private void iniciarEscaneo(String nombre) {
        if (!tienePermisos()) {
            pedirPermisos();
            return;
        }

        if (adaptador == null || !adaptador.isEnabled()) {
            mostrarEstado("Activa el Bluetooth del móvil");
            return;
        }

        if (!ubicacionActivada()) {
            mostrarEstado("Activa la Ubicación del móvil");
            return;
        }

        escaner = adaptador.getBluetoothLeScanner();
        if (escaner == null) {
            mostrarEstado("No se ha podido obtener el escáner BLE");
            return;
        }

        detenerEscaneo();
        dispositivosEncontrados.clear();

        List<ScanFilter> filtros = null;
        if (nombre != null) {
            filtros = Collections.singletonList(
                    new ScanFilter.Builder().setDeviceName(nombre).build());
        }

        ScanSettings ajustes = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

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
                Log.e(TAG, "Fallo de escaneo: " + errorCode);
                escaneando = false;
                runOnUiThread(() -> actualizarBotones());
                mostrarEstado("Error al escanear (código " + errorCode + ")");
            }
        };

        try {
            escaner.startScan(filtros, ajustes, callback);
            escaneando = true;
            actualizarBotones();
            mostrarEstado(nombre == null
                    ? "Buscando todos los dispositivos..."
                    : "Buscando " + nombre + "...");

        } catch (SecurityException e) {
            Log.e(TAG, "Sin permiso para escanear", e);
            mostrarEstado("Android ha denegado el permiso Bluetooth");
        }
    }

    // --------------------------------------------------------------
    // resultado: ResultadoEscaneo --> procesarResultado() -->
    //
    // 1. Si el dispositivo es nuevo: anotarlo, escribirlo en el log
    //    y, si se buscan todos, mostrar cuántos hay.
    // 2. Si no enviar_al_servidor: terminar.
    // 3. datos = datos de fabricante 0x004C
    // 4. Si interprete.esDeNuestroSensor(datos): tratarMedidaRecibida(datos)
    // --------------------------------------------------------------
    private void procesarResultado(ScanResult resultado) {
        ScanRecord registro = resultado.getScanRecord();
        if (registro == null) {
            return;
        }

        String direccion = resultado.getDevice().getAddress();

        if (dispositivosEncontrados.add(direccion)) {
            Log.i(TAG, "Dispositivo: " + registro.getDeviceName() + " | " + direccion
                    + " | " + resultado.getRssi() + " dBm");

            if (!enviarAlServidor) {
                mostrarEstado("Buscando todos... " + dispositivosEncontrados.size()
                        + " dispositivos (lista en Logcat)");
            }
        }

        if (!enviarAlServidor) {
            return;
        }

        SparseArray<byte[]> fabricantes = registro.getManufacturerSpecificData();
        byte[] datos = fabricantes == null ? null : fabricantes.get(ID_FABRICANTE_APPLE);

        if (interprete.esDeNuestroSensor(datos)) {
            tratarMedidaRecibida(datos);
        }
    }

    // --------------------------------------------------------------
    // datos: [Z] --> tratarMedidaRecibida() -->
    //
    // 1. medida = interprete.interpretar(datos)
    // 2. Si medida.contador == ultimo_contador_enviado: terminar.
    // 3. ultimo_contador_enviado = medida.contador
    // 4. Mostrar la medida y "Enviando..." en pantalla.
    // 5. logica.guardarMedida(medida) y mostrar el resultado del envío.
    // --------------------------------------------------------------
    private void tratarMedidaRecibida(byte[] datos) {
        Medida medida = interprete.interpretar(datos);

        if (medida.getContador() == ultimoContadorEnviado) {
            return;
        }
        ultimoContadorEnviado = medida.getContador();

        String texto = String.format(Locale.getDefault(), "%s = %d  (contador %d)",
                medida.nombreDelTipo(), medida.getValor(), medida.getContador());
        Log.i(TAG, "Medida recibida: " + texto);

        runOnUiThread(() -> {
            textoEstado.setText("Sensor encontrado ✓");
            textoUltimaMedida.setText(texto);
            mostrarEnvio("Enviando...", COLOR_PENDIENTE);

            logica.guardarMedida(medida, (ok, mensaje) -> {
                Log.i(TAG, "Envío: ok=" + ok + " " + mensaje);
                if (ok) {
                    mostrarEnvio("Enviado al servidor ✓", COLOR_OK);
                } else {
                    mostrarEnvio("Error al enviar ✗\n" + mensaje, COLOR_ERROR);
                }
            });
        });
    }

    // --------------------------------------------------------------
    // texto: Text --> mostrarEstado() -->
    // --------------------------------------------------------------
    private void mostrarEstado(String texto) {
        runOnUiThread(() -> textoEstado.setText(texto));
    }

    // --------------------------------------------------------------
    // texto: Text, color: Z --> mostrarEnvio() -->
    // --------------------------------------------------------------
    private void mostrarEnvio(String texto, int color) {
        textoResultadoEnvio.setText(texto);
        textoResultadoEnvio.setTextColor(color);
    }

    // --------------------------------------------------------------
    // actualizarBotones() -->
    // Buscando: solo "Detener" activo. Detenido: lo contrario.
    // --------------------------------------------------------------
    private void actualizarBotones() {
        botonBuscarNuestro.setEnabled(!escaneando);
        botonBuscarTodos.setEnabled(!escaneando);
        botonDetener.setEnabled(escaneando);
    }

    // --------------------------------------------------------------
    // detenerEscaneo() -->
    // 1. Si se está escaneando: parar el escaneo.
    // 2. escaneando = false
    // 3. actualizarBotones()
    // --------------------------------------------------------------
    private void detenerEscaneo() {
        if (escaner != null && callback != null && escaneando) {
            try {
                escaner.stopScan(callback);
            } catch (SecurityException e) {
                Log.e(TAG, "No se pudo detener el escaneo", e);
            }
        }

        escaneando = false;
        actualizarBotones();
    }

    // --------------------------------------------------------------
    // onDestroy() -->
    // --------------------------------------------------------------
    @Override
    protected void onDestroy() {
        detenerEscaneo();
        super.onDestroy();
    }
}
