package com.example.inventario;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class VerUbicacion extends AppCompatActivity {

    Button botonNext;
    ProgressDialog pd;
    ArrayList<Bitmap> lasimagenes = new ArrayList<Bitmap>();
    ListView listViewandowski;
    String usuario, ubicacion;
    TextView textUbicacion;
    boolean elOK;
    Bundle losExtras;
    AlertDialog alerta;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verubicacion);
        getSupportActionBar().hide();
        losExtras = getIntent().getExtras();
        try {
            usuario = losExtras.getString("loggedUser");
            ubicacion = losExtras.getString("existingLocation");
        } catch (NullPointerException ex) {
            ubicacion = " ";
        }
        listViewandowski = (ListView) findViewById(R.id.listaInfo);
        textUbicacion = (TextView) findViewById(R.id.txtUbicacionInfo);
        textUbicacion.setText(ubicacion);
        elOK = true;
        botonNext = (Button) findViewById(R.id.buttonSiguiente);

        mensajeDeProcesando();

        mostrarItemsUbicacion("http://10.107.226.241/apis/inv/ver_ubicacion", ubicacion);

        botonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mensajeDeProcesando();
                ingresarValidacion("http://10.107.226.241/apis/inv/insertar_inventario_ubicacion", usuario, ubicacion);
            }
        });
        botonNext.setEnabled(false);

    }

    public void ocultarTeclado() {
        try {
            View view = this.getCurrentFocus();
            view.clearFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (NullPointerException ex) {
            System.out.println(ex);
        }
    }


    public void mensajeDeProcesando() {
        pd = ProgressDialog.show(this, "Procesando", "Espere unos segundos...", true, false);
    }

    public void adaptadorInfoImagen(ArrayList[] matriz, ArrayList imagenes) {
        VisualAdapter visualAdapter = new VisualAdapter(this, R.layout.fila_lista, matriz, imagenes);
        listViewandowski.setAdapter(visualAdapter);
        listViewandowski.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount == 0) {
                    botonNext.setEnabled(true);
                } else {
                    System.out.println(firstVisibleItem + "---" + visibleItemCount + "---" + totalItemCount);
                    if (firstVisibleItem + 1 == totalItemCount) {
                        botonNext.setEnabled(true);
                    }
                }
            }
        });
    }

    public void mostrarItemsUbicacion(String url, String ubicacion) {
        StringRequest sr = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arregloJSON = new JSONArray(response);
                    int cantidadFilasRespuesta = arregloJSON.length();
                    ArrayList<String>[] matrizDeArreglos = new ArrayList[cantidadFilasRespuesta];

                    for (int i = 0; i < cantidadFilasRespuesta; i++) {
                        JSONObject objetoJSON = arregloJSON.getJSONObject(i);
                        Iterator keys = objetoJSON.keys();
                        ArrayList<String> valores = new ArrayList<>();
                        while (keys.hasNext()) {
                            String currentColumnaDinamica = (String) keys.next();
                            String currentValorDinamico = objetoJSON.getString(currentColumnaDinamica);
                            if (currentValorDinamico.equals("null")) {
                                currentValorDinamico = "";
                            }
                            valores.add(currentValorDinamico);
                            matrizDeArreglos[i] = valores;
                        }
                    }
                    cargarImagenes(matrizDeArreglos);
                } catch (Exception e) {
                    if (VerUbicacion.this.pd != null) {
                        VerUbicacion.this.pd.dismiss();
                    }
                    e.printStackTrace();
                    alertaDeErrorModal("ERROR ", "Image Load Epic Fail ("+e.toString()+")");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (VerUbicacion.this.pd != null) {
                    VerUbicacion.this.pd.dismiss();
                }
                alertaDeErrorModal("ERROR", "Volley fail ("+error.toString()+")");
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("ubicacion", ubicacion);
                return parametros;
            }
        };
        RequestQueue rq = Volley.newRequestQueue(this);
        rq.add(sr);
    }

    public void cargarImagenes(ArrayList<String>[] matriz) {
        new VerUbicacion.GetItemImages().execute(matriz);
    }

    class GetItemImages extends AsyncTask<ArrayList<String>[], Void, Bitmap> {

        private Exception exception = null;
        ArrayList<String>[] matrizea;

        protected Bitmap doInBackground(ArrayList<String>[]... matrix) {
            HttpURLConnection connection = null;
            matrizea = matrix[0];
            for (int i = 0; i < matrizea.length; i++) {
                try {
                    ArrayList resultado = matrizea[i];
                    String uerrele = resultado.get(5).toString() + "?sw=200&amp";
                    System.out.println(uerrele);
                    URL url = new URL(uerrele);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    Bitmap myBitmap = BitmapFactory.decodeStream(connection.getInputStream());
                    lasimagenes.add(myBitmap);
                    connection.disconnect();
                } catch (Exception e) {
                    this.exception = e;
                    lasimagenes.add(null);
                    try {
                        connection.disconnect();
                    } catch (NullPointerException err) {
                    }
                } finally {
                    try {
                        connection.getInputStream().close();
                    } catch (Exception e) {
                        try {
                            connection.getErrorStream().close();
                        } catch (Exception ex) {
                        }
                    }
                }
            }
            return null;
        }

        protected void onPostExecute(Bitmap feed) {
            if (VerUbicacion.this.pd != null) {
                VerUbicacion.this.pd.dismiss();
            }
            if(exception==null){
                adaptadorInfoImagen(matrizea, lasimagenes);
            }else{
                Toast.makeText(VerUbicacion.this, "Some pictures weren't found", Toast.LENGTH_LONG).show();
                adaptadorInfoImagen(matrizea, lasimagenes);
            }
        }
    }

    private void ingresarValidacion(String URL, String user, String location) {
        StringRequest sr = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.isEmpty()) {
                    if (elInterpretador(response).equals("OK")) {
                        if (VerUbicacion.this.pd != null) {
                            VerUbicacion.this.pd.dismiss();
                        }
                        volverAlPincharUbicacion();
                    } else {
                        if (VerUbicacion.this.pd != null) {
                            VerUbicacion.this.pd.dismiss();
                        }
                        alertaDeErrorModal("ERROR", elInterpretador(response));
                    }
                } else {
                    if (VerUbicacion.this.pd != null) {
                        VerUbicacion.this.pd.dismiss();
                    }
                    alertaDeErrorModal("ERROR", "Couldn't read no binary information");
                }
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                if (VerUbicacion.this.pd != null) {
                    VerUbicacion.this.pd.dismiss();
                }
                alertaDeErrorModal("ERROR DE CONEXION", "Verifica la conexion del dispositivo. Code 267. "+error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("ubicacion", location);
                parametros.put("usuario", user);
                return parametros;
            }
        };
        RequestQueue rq = Volley.newRequestQueue(this);
        rq.add(sr);
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

    public void alertaDeErrorModal(String tituloError, String mensajeError) {
        MediaPlayer mp = MediaPlayer.create(this, R.raw.error);
        mp.start();
        JOptionPaneShowMessageDialog(tituloError, mensajeError);
    }


    public void JOptionPaneShowMessageDialog(String titulo, String mensaje) {
        try {
            alerta.dismiss();
        } catch (NullPointerException ex) {

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

    public void volverAlPincharUbicacion() {
        deleteCache(this);
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("loggedUser", usuario);
        startActivity(i);
        finish();
    }

    public void volverAlPincharUbicacionConError(String error) {
        deleteCache(this);
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("loggedUser", usuario);
        i.putExtra("detailError", error);
        startActivity(i);
        finish();
    }

    public void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

}
