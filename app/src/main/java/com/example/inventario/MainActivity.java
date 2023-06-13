package com.example.inventario;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.Scanner.DataListener;
import com.symbol.emdk.barcode.Scanner.StatusListener;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerResults;
import com.symbol.emdk.barcode.StatusData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity implements EMDKListener, StatusListener, DataListener {

    AlertDialog alerta;
    private BarcodeManager barcodeManager = null;
    private Scanner scanner = null;
    private EMDKManager emdkManager = null;
    public TextView statusTextView = null;
    public TextView txtUbicacion = null;
    Bundle losExtras;
    String ubicacionActual, usuario, errorDeAntes;
    ProgressBar pb_loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        losExtras = getIntent().getExtras();
        try {
            usuario = losExtras.getString("loggedUser");
        } catch (NullPointerException ex) {
            usuario = "";
        }
        try {
            errorDeAntes = losExtras.getString("detailError");
            errorDeAntes.length();
        } catch (NullPointerException ex) {
            errorDeAntes="";
        }
        if(errorDeAntes.length()>0){
            alertaDeErrorModal("ERROR", errorDeAntes);
        }
        statusTextView = findViewById(R.id.textViewStatus);
        txtUbicacion = findViewById(R.id.textUbicacion);
        EMDKResults results = EMDKManager.getEMDKManager(getApplicationContext(), this);
        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            statusTextView.setText("Falló el EMDKManager. Asi es la vida");
        }
        pb_loading = (ProgressBar) findViewById(R.id.progressBarUbicacion);
        pb_loading.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onOpened(EMDKManager emdkManager) {
        this.emdkManager = emdkManager;
        try {
            initializeScanner();
        } catch (ScannerException e) {
            e.printStackTrace();
        }
        Toast.makeText(MainActivity.this, "Escanear ubicacion", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClosed() {
        if (this.emdkManager != null) {
            this.emdkManager.release();
            this.emdkManager = null;
        }
    }

    @Override
    public void onData(ScanDataCollection scanDataCollection) {
        new AsyncDataUpdate().execute(scanDataCollection);
    }

    @Override
    public void onStatus(StatusData statusData) {
        try {
            scanner.read();
        } catch (ScannerException e) {
            e.printStackTrace();
        }
        new AsyncStatusUpdate().execute(statusData);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (emdkManager != null) {
            emdkManager.release();
            emdkManager = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (scanner != null) {
                scanner.removeDataListener(this);
                scanner.removeStatusListener(this);
                scanner.disable();
                scanner = null;
            }
        } catch (ScannerException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        try {
            initializeScanner();
        } catch (ScannerException e) {
            e.printStackTrace();
        }
        Toast.makeText(MainActivity.this, "Escanear ubicacion", Toast.LENGTH_SHORT).show();
    }

    private void initializeScanner() throws ScannerException {;
        if (scanner == null) {
            barcodeManager = (BarcodeManager) this.emdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE);
            scanner = barcodeManager.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT);
            scanner.addDataListener(this);
            scanner.addStatusListener(this);
            scanner.triggerType = Scanner.TriggerType.HARD;
            scanner.enable();
            scanner.read();
        }
    }

    public class AsyncDataUpdate extends AsyncTask<ScanDataCollection, Void, String> {

        @Override
        protected String doInBackground(ScanDataCollection... params) {
            String statusStr = "";
            ScanDataCollection scanDataCollection = params[0];
            if ((scanDataCollection != null) && (scanDataCollection.getResult() == ScannerResults.SUCCESS)) {
                ArrayList<ScanDataCollection.ScanData> scanData = scanDataCollection.getScanData();
                statusStr = scanData.get(0).getData();
            }
            return statusStr;
        }

        @Override
        protected void onPostExecute(final String result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        alerta.dismiss();
                    } catch (NullPointerException ex) {
                        System.out.println(ex.toString());
                    }
                    ubicacionActual = result;
                    txtUbicacion.setText(ubicacionActual);
                    procesandoTarea(true);
                    existeUbicacion("http://10.107.226.241/apis/inv/validar_ubicacion", ubicacionActual);
                }
            });
        }
    }

    private class AsyncStatusUpdate extends AsyncTask<StatusData, Void, String> {

        @Override
        protected String doInBackground(StatusData... params) {
            String statusStr = "";
            StatusData statusData = params[0];
            StatusData.ScannerStates state = statusData.getState();
            switch (state) {
                case IDLE:
                    statusStr = "Escaner habilitado";
                    break;
                case SCANNING:
                    statusStr = "Escaneando...";
                    break;
                case WAITING:
                    statusStr = "Esperando acción del gatillo...";
                    break;
                case DISABLED:
                    statusStr = "Escaner offside";
                    break;
                default:
                    break;
            }
            return statusStr;
        }

        @Override
        protected void onPostExecute(String result) {
            statusTextView.setText(result);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

    }

    public void detenerScanner() {
        if (this.emdkManager != null) {
            this.emdkManager.release();
            this.emdkManager = null;
        }
        try {
            if (scanner != null) {
                scanner.removeDataListener(this);
                scanner.removeStatusListener(this);
                scanner.disable();
                scanner = null;
            }
        } catch (ScannerException e) {
            e.printStackTrace();
        }
    }

    public void procesandoTarea(boolean status) {
        if (status) {
            try {
                if (scanner != null) {
                    scanner.removeDataListener(this);
                    scanner.removeStatusListener(this);
                    scanner.disable();
                    scanner = null;
                }
            } catch (ScannerException e) {
                e.printStackTrace();
            }
            pb_loading.setVisibility(View.VISIBLE);
        } else {
            try {
                initializeScanner();
            } catch (ScannerException e) {
                e.printStackTrace();
            }
            pb_loading.setVisibility(View.INVISIBLE);
        }
    }

    public void abrirActivityVer(String laUbicacion, String elUsuario) {
        detenerScanner();
        Intent i = new Intent(this, VerUbicacion.class);
        i.putExtra("existingLocation", laUbicacion);
        i.putExtra("loggedUser", elUsuario);
        startActivity(i);
        finish();
    }

    public void JOptionPaneShowMessageDialog(String titulo, String mensaje) {
        try {
            alerta.dismiss();
        } catch (NullPointerException ex) {
            System.out.println(ex.toString());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titulo);
        builder.setMessage(mensaje);
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        alerta = builder.create();
        alerta.show();
    }

    private void existeUbicacion(String URL, String location) {
        StringRequest sr = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.isEmpty()) {
                    if (elInterpretador(response).equals("Existe")) {
                        procesandoTarea(false);
                        abrirActivityVer(location, usuario);
                    } else {
                        procesandoTarea(false);
                        alertaDeErrorModal("ERROR", elInterpretador(response));
                    }
                } else {
                    procesandoTarea(false);
                    alertaDeErrorModal("ERROR", "Code 317. Couldn't read no binary information");
                }
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                procesandoTarea(false);
                alertaDeErrorModal("ERROR", "Verificar conexion (" + error.toString() + ")");
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("ubicacion", location);
                return parametros;
            }
        };
        RequestQueue rq = Volley.newRequestQueue(this);
        rq.add(sr);
    }

    public void alertaDeErrorModal(String tituloError, String mensajeError) {
        MediaPlayer mp = MediaPlayer.create(this, R.raw.error);
        mp.start();
        JOptionPaneShowMessageDialog(tituloError, mensajeError);
    }

    public String elInterpretador(String responseString) {
        String resp = "Error 505";
        try {
            String[] arrSplit = responseString.split("\"");
            resp = arrSplit[3];
        } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
        }
        return resp;
    }

}
