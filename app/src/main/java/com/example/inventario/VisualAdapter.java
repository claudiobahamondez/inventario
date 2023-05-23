package com.example.inventario;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class VisualAdapter extends BaseAdapter {
    private Context context;
    private int layout;
    private ArrayList<String>[] matriz;
    private ArrayList<Bitmap> imagenes;


    public VisualAdapter(Context context, int layout, ArrayList [] matriz, ArrayList<Bitmap> imagenes){
        this.context = context;
        this.layout = layout;
        this.matriz = matriz;
        this.imagenes = imagenes;

    }

    @Override
    public int getCount() {
        return this.matriz.length;
    }

    @Override
    public Object getItem(int position) {
        return matriz[position];
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override

    public View getView(int position, View convertView, ViewGroup viewGroup) {
        // Copiamos la vista
        View v = convertView;

        //Inflamos la vista con nuestro propio layout
        LayoutInflater layoutInflater = LayoutInflater.from(this.context);

        v= layoutInflater.inflate(R.layout.fila_lista, null);
        // Valor actual según la posición

        ArrayList resultado = matriz[position];
        String el_item = resultado.get(1).toString();
        System.out.print(el_item+"\t");
        String el_descripcion = resultado.get(2).toString();
        System.out.print(el_item+"\t");
        String el_depto = resultado.get(4).toString();
        System.out.print(el_item+"\t");
        String el_qty = resultado.get(3).toString();

        // Referenciamos el elemento a modificar y lo rellenamos
        TextView textView = (TextView) v.findViewById(R.id.txtItem);
        textView.setText(el_item);

        TextView textView2 = (TextView) v.findViewById(R.id.txtDescripcion);
        textView2.setText(el_descripcion);

        TextView textView3 = (TextView) v.findViewById(R.id.txtDepto);
        textView3.setText(el_depto);

        TextView textView4 = (TextView) v.findViewById(R.id.txtCant);
        textView4.setText(el_qty);

        ImageView photo = (ImageView) v.findViewById(R.id.imach);
        photo.setImageBitmap(imagenes.get(position));

        return v;
    }





 }