package com.example.proyectoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity2 extends AppCompatActivity {

    Button notifyBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_fridges);
    }

    public void lanzarMain(View view){
        Intent i = new Intent(this, MainActivity2.class);
        startActivity(i);
    }

    public void lanzarPruebaNotificaciones(View view){
        Intent i = new Intent(this, PruebaNotificaciones.class);
        startActivity(i);
    }

}