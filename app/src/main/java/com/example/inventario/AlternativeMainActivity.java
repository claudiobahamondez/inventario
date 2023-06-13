package com.example.inventario;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class AlternativeMainActivity extends Activity {

    AlertDialog alerta;
    TextView statusTextView, escanearUbicacion;
    EditText txtUbicacion = null;
    Bundle losExtras;
    String usuario, errorDeAntes;
    ProgressBar pb_loading;
    Button botonNext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_alt);
        botonNext = (Button) findViewById(R.id.bt_next_Alt);
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
        escanearUbicacion = findViewById(R.id.textViewEscUbicacion_Alt);
        statusTextView = findViewById(R.id.textViewStatus_Alt);
        txtUbicacion = findViewById(R.id.textUbicacion_Alt);
        ocultarTeclado();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        pb_loading = (ProgressBar) findViewById(R.id.progressBarUbicacion_Alt);
        procesandoTarea(false);

        escanearUbicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtUbicacion.setText("");
            }
        });

        txtUbicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ocultarTeclado();
            }
        });

        botonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ubicacionActual = txtUbicacion.getText().toString().toUpperCase();
                procesandoTarea(true);
                existeUbicacion("http://10.107.226.241/apis/inv/validar_ubicacion", ubicacionActual);
            }
        });

    }

    public void procesandoTarea(boolean status) {
        if (status) {
            pb_loading.setVisibility(View.VISIBLE);
        } else {
            pb_loading.setVisibility(View.INVISIBLE);
        }
    }

    public void abrirActivityVer(String laUbicacion, String elUsuario) {
        Intent i = new Intent(this, AlternativeVerUbicacion.class);
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
                        txtUbicacion.setText("");
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
        } catch (ArrayIndexOutOfBoundsException ex) {
        }
        return resp;
    }

    public void ocultarTeclado(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtUbicacion.getWindowToken(), 0);
    }

}
