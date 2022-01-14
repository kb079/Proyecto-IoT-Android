package com.myfridge.app.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.myfridge.app.MainActivity;
import com.myfridge.app.R;

import java.util.Timer;
import java.util.TimerTask;

public class Notifications {

    private NotificationManager notificationManager;
    private Context context;

    private int auxTemp = 0;
    private int auxDoor = 0;

    private static Notifications instance;

    public Notifications(Context context){
        instance = this;
        this.context = context;

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static Notifications getInstance(){
        return instance;
    }

    public void notificacionTemperatura( double temperatura){

        int NOTIFICATION_ID = 0;
        String CANAL_ID = "CanalTemperatura";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CANAL_ID, "Mis Notificaciones",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Descripcion del canal");
            notificationManager.createNotificationChannel(notificationChannel);

            if (temperatura >= 27 && auxTemp == 0) {
                NotificationCompat.Builder notificacion =
                        new NotificationCompat.Builder(context, CANAL_ID)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("Alerta de temperatura")
                                .setContentText("La temperatura de la nevera ha excedido a " + temperatura + " ºC");

                PendingIntent intencionPendiente = PendingIntent.getActivity(
                        context, 0, new Intent(context, MainActivity.class), 0);
                notificacion.setContentIntent(intencionPendiente);

                notificationManager.notify(NOTIFICATION_ID, notificacion.build());
                auxTemp = 1;

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        auxTemp = 0;
                    }
                }, 900000); // Cada 15 minutos, lanza la notificación.
            }

        }
    }

    public void notificationPuertaAbierta( int estado ){

        int NOTIFICATION_ID = 1;
        String CANAL_ID = "CanalPuerta";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CANAL_ID, "Mis Notificaciones",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Descripcion del canal");
            notificationManager.createNotificationChannel(notificationChannel);

            if (estado == 1 && auxDoor == 0) {
                NotificationCompat.Builder notificacion =
                        new NotificationCompat.Builder(context, CANAL_ID)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("Puerta abierta")
                                .setContentText("Parece que te has dejado la puerta de tu nevera abierta, cierrala!");

                PendingIntent intencionPendiente = PendingIntent.getActivity(
                        context, 0, new Intent(context, MainActivity.class), 0);
                notificacion.setContentIntent(intencionPendiente);

                notificationManager.notify(NOTIFICATION_ID, notificacion.build());
                auxDoor = 1;

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        auxDoor = 0;
                    }
                }, 900000); // Cada 15 minutos, lanza la notificación.
            }
        }
    }
}
