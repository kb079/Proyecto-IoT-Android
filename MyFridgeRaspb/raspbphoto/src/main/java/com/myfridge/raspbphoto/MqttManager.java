package com.myfridge.raspbphoto;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttManager {

    public static final String topicRoot = "myfridge/sensor/";
    public static final String topic = "foto";
    public static final int qos = 2;
    public static final String broker = "tcp://broker.hivemq.com:1883";
    public static final String clientId = "MyFridgeRaspb";

    public static MqttClient client;

    public static void suscribirMqtt(MqttCallback listener) {
        try {
            System.out.println("Suscrito a " + topicRoot + topic);
            client.setCallback(listener);
            System.out.println("Anadi el callback");
            client.subscribe(topicRoot + topic +"/#", qos);
            System.out.println("Me suscribí");

        } catch (MqttException e) {
            System.out.print("Error al suscribir.");
        }
    }

    public static void conectarMqtt() {
        try {
            System.out.print("Conectando al broker " + broker);
            client = new MqttClient(broker, clientId, new MemoryPersistence());
            client.connect();
        } catch (MqttException e) {
            System.out.print("Error al conectar.");
        }
    }

    public static void desconectarMqtt() {
        try {
            client.disconnect();
            System.out.print("Desconectado");
        } catch (MqttException e) {
            System.out.print("Error al desconectar.");
        }
    }

}
