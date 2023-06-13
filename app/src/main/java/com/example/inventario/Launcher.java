package com.example.inventario;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Launcher extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isSymbolEMDKAvailable()) {
            abrirActivityLogEMDK();
        } else {
            abrirActivityLogOtro();
        }
    }

    public void abrirActivityLogEMDK() {
        Intent i = new Intent(this, Log.class);
        startActivity(i);
        finish();
    }

    public void abrirActivityLogOtro() {
        Intent i = new Intent(this, AlternativeLog.class);
        startActivity(i);
        finish();
    }

    public boolean isSymbolEMDKAvailable() {
        try {
            Class.forName("com.symbol.emdk.EMDKManager");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }


}
