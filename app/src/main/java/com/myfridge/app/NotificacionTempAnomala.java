package com.myfridge.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

public class NotificacionTempAnomala extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notificacion_temp_anomala, container, false);

        Button notifyBtn = view.findViewById(R.id.btnNotiTempAnomala);

        notifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity().getApplicationContext(), "ProyectoApp")
                        .setSmallIcon(R.drawable.app_icon_v1)
                        .setContentTitle("¡Comprueba tu nevera!")
                        .setContentText(getString(R.string.textContentTempAnomala))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);

                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getActivity().getApplicationContext());
                managerCompat.notify(1, builder.build());
            }
        });

        return view;
    }
}