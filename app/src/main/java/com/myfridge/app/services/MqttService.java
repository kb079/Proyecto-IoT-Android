package com.myfridge.app.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import static com.myfridge.app.comunication.Mqtt.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.myfridge.app.manager.fridge.Fridge;
import com.myfridge.app.manager.fridge.Item;
import com.myfridge.app.utils.Notifications;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MqttService extends Service implements MqttCallback  {

    private static MqttClient client;
    private Notifications notifications;

    //---------------------------------------------------------------//
    //----------------------- Conectarse a Mqtt ---------------------//
    public static void connectMqtt() {
        try {
            Log.i(TAG, "Conectando al broker " + broker);
            client = new MqttClient(broker, clientId, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setKeepAliveInterval(60);
            connOpts.setWill(topicRoot+"WillTopic","App desconectada".getBytes(),
                    qos, false);
            client.connect(connOpts);
        } catch (MqttException e) {
            Log.e(TAG, "Error al conectar.", e);
        }
    }

    //---------------------------------------------------------------//
    //---------------------- Suscribirse a Mqtt --------------------//

    public static void suscribeMqtt(String topic, MqttCallback listener) {
        try {
            Log.i(TAG, "Suscrito a " + topicRoot + topic);
            client.subscribe(topicRoot + topic, qos);
            client.setCallback(listener);
        } catch (MqttException e) {
            Log.e(TAG, "Error al suscribir.", e);
        }
    }

    //---------------------------------------------------------------//
    //-------------------- Desconectarse de Mqtt -------------------//

    public void disconectMqtt() {
        try {
            client.disconnect();
            Log.i(TAG, "Desconectado");
        } catch (MqttException e) {
            Log.e(TAG, "Error al desconectar.", e);
        }
    }

    //---------------------------------------------------------------//
    //------------------ Conexión Perdida de Mqtt ------------------//

    @Override
    public void connectionLost(Throwable cause) {
        Log.d(TAG, "Conexión perdida");
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                connectMqtt();
            }
        }, 60000); // Al minuto vuelve a establecer conexion con MQTT.
    }

    //---------------------------------------------------------------//
    //------------------ Mensaje recibido de Mqtt ------------------//

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());
        Log.d(TAG, "Recibiendo: " + topic + " -> " + payload);

        if( topic.equals(topicRoot + topicTemperature)) {
            notifications.getInstance().notificacionTemperatura(Double.parseDouble(payload));
        }
        else if(topic.equals(topicRoot + topicDoorState)){
            notifications.getInstance().notificationPuertaAbierta(Integer.parseInt(payload));

        }else if(topic.equals(topicRoot + topicRFID)){
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uidUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();

            db.collection("data").document(uidUsuario).collection("fridges").document("fridge0").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task){
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        String[] data = payload.split(";");

                        if (document.exists()) {
                            ArrayList<Item> items = task.getResult().toObject(Fridge.class).getItems();
                            boolean found = false;

                            Item rfidItem = null;
                            for (Item item: items) {
                                if(item.getRfidUUID() != null){
                                    rfidItem = item;
                                    break;
                                }
                            }

                            if(rfidItem != null){
                                items.remove(rfidItem);
                            }else{
                                items.add(new Item(data[2], 1, Long.parseLong(data[1]), data[0]));
                            }
                            /*
                            for (Item item: items) {
                                if(item.getBarCode().equals(data[2])){
                                    item.setQty(item.getQty()+1);
                                    found = true;
                                    break;
                                }
                            }
                            */
/*
                            if(!found){
                                items.add(new Item(data[2], 1, Long.parseLong(data[1]), data[0]));
                            }
*/
                            db.collection("data").document(uidUsuario).collection("fridges").document("fridge0").update("items", items);

                        } else {
                            Log.d("Firestore", "No such document");
                        }
                    } else {
                        Log.d("Firestore", "get failed with ", task.getException());
                    }

                }
            });
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(TAG, "Entrega completa");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        connectMqtt();

        suscribeMqtt(topicTemperature, this);
        suscribeMqtt(topicDoorState, this);
        suscribeMqtt(topicRFID, this);

        notifications = new Notifications(getApplicationContext());
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        if(client != null) disconectMqtt();
        super.onDestroy();
    }
}
