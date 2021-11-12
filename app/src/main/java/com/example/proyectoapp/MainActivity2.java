package com.example.proyectoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity2 extends AppCompatActivity {

    Button notifyBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_home);
    }

    public void lanzarMain(View view){
        Intent i = new Intent(this, MainActivity2.class);
        startActivity(i);
    }

    public void lanzarPruebaNotificaciones(View view){
        Intent i = new Intent(this, PruebaNotificaciones.class);
        startActivity(i);
    }

    public void viewFridges(View v){
        Intent i = new Intent(this, FridgesListFragment.class);
        startActivity(i);
    }
}