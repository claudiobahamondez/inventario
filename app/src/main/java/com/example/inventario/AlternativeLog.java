package com.example.inventario;

import android.app.Activity;
import android.content.Context;
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
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class AlternativeLog extends Activity {

    TextView escanearUsuario;
    EditText txtUsuario;
    public TextView statusTextView = null;
    ProgressBar pb_loading;
    private Button botonLoggear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_alt);
        escanearUsuario = findViewById(R.id.textViewUsuario_Alt);
        txtUsuario = (EditText) findViewById(R.id.textUsuario_Alt);
        txtUsuario.requestFocus();
        ocultarTeclado();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        statusTextView = findViewById(R.id.textViewStatusScanner_Alt);
        pb_loading = (ProgressBar) findViewById(R.id.progressBarLog_Alt);
        botonLoggear = (Button) findViewById(R.id.buttonLogin_Alt);
        procesandoTarea(false);

        escanearUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtUsuario.setText("");
            }
        });

        txtUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ocultarTeclado();
            }
        });

        botonLoggear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String usuario = txtUsuario.getText().toString();
                procesandoTarea(true);
                validarUsuario("http://10.107.226.241/apis/inv/validar_usuario", usuario);
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

    public void abrirActivityMain(String elUsuario) {
        Intent i = new Intent(this, AlternativeMainActivity.class);
        i.putExtra("loggedUser", elUsuario);
        startActivity(i);
        finish();
    }

    public void alertaDeError(String mensajeError) {
        MediaPlayer mp = MediaPlayer.create(this, R.raw.error);
        mp.start();
        Toast.makeText(getApplicationContext(), mensajeError, Toast.LENGTH_LONG).show();
    }

    private void validarUsuario(String URL, String usuario) {
        StringRequest sr = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.isEmpty()) {
                    procesandoTarea(false);
                    abrirActivityMain(usuario);
                } else {
                    procesandoTarea(false);
                    alertaDeError("Usuario '"+usuario+"' no existe en Manhattan Active");
                    txtUsuario.setText("");
                }
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                procesandoTarea(false);
                alertaDeError(error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("usuario", usuario);
                return parametros;
            }
        };
        RequestQueue rq = Volley.newRequestQueue(this);
        rq.add(sr);
    }

    public void ocultarTeclado(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtUsuario.getWindowToken(), 0);
    }

}
