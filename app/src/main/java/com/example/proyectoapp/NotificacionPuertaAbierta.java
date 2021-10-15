package com.example.proyectoapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

public class NotificacionPuertaAbierta extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.notificacion_p_abierta, container, false);

        Button notifyBtn = view.findViewById(R.id.btnNotiPAbierta);

        notifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity().getApplicationContext(), "ProyectoApp")
                        .setSmallIcon(R.drawable.app_icon_v1)
                        .setContentTitle("¡Comprueba tu nevera!")
                        .setContentText(getString(R.string.textContentPAbierta))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);

                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getActivity().getApplicationContext());
                managerCompat.notify(1, builder.build());
            }
        });

        return view;
    }

}
