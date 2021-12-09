package com.example.raspberry;


import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import io.grpc.LoadBalancerRegistry;
import io.grpc.internal.PickFirstLoadBalancerProvider;

public class Portero {
    public static final String TAG = "MQTT";
    public static final String topicRoot="myfridge/sensor/";//Reemplaza jtomas
    public static final int qos = 2;
    public static final String broker = "tcp://broker.hivemq.com:1883";
    public static final String clientId = "Manolito"; //Reemplaza
    public static MqttClient client;

    public static String PATH_TO_CREDENTIALS =
            "./my-application3-fb8d1.json";
            //"C:\\Users\\alumno\\AndroidStudioProjects\\PorteroAutomatico\\raspberry\\my-application3-fb8d1.json";
    public static String BUCKET = "my-application3-fb8d1.appspot.com";
    public static Storage storage;
    public static void main(String[] args) {
        System.out.println("Hello Raspberry");
        LoadBalancerRegistry.getDefaultRegistry()
                .register(new PickFirstLoadBalancerProvider());
        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(
                            new FileInputStream(PATH_TO_CREDENTIALS)))
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            storage = StorageOptions.newBuilder()
                    .setCredentials(ServiceAccountCredentials.fromStream(
                            new FileInputStream(PATH_TO_CREDENTIALS)))
                    .build()
                    .getService();
        } catch (IOException e) {
            e.printStackTrace();
        }

        conectarMqtt();
        long tiempo = System.currentTimeMillis();
        //publicarMqtt("tiempo", "Nublado");
        suscribirMqtt("foto", new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String arg0, MqttMessage arg1)  {



                System.out.println(
                        "  Topic:\t" + arg0 +
                                "  Message:\t" + new String(arg1.getPayload()) +
                                "  QoS:\t" + arg1.getQos());
                String nombreFichero = UUID.randomUUID().toString();
                tomarFoto("captura.jpeg");
                subirFichero("captura.jpeg", "fotos/"+nombreFichero+".jpeg");
                String url = "https://storage.googleapis.com/"+BUCKET+"/"
                        +"fotos/"+nombreFichero+".jpeg";
                guardarFirestore(url, "subido desde Raspberry Pi");
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken arg0) {


                try {
                    System.out.println(arg0.getMessageId()+" "+arg0.getMessage());
                } catch (MqttException e) {

                    e.printStackTrace();
                }
            }


        });

        //subirFichero(PATH_TO_CREDENTIALS, "carpeta/raspberry.json");
        //subirFichero(PATH_TO_CREDENTIALS, "carpeta/raspberry.json");

        /*String nombreFichero = UUID.randomUUID().toString();
        tomarFoto("captura.jpeg");
        subirFichero("captura.jpeg", "fotos/"+nombreFichero+".jpeg");
        String url = "https://storage.googleapis.com/"+BUCKET+"/"
                +"fotos/"+nombreFichero+".jpeg";
        guardarFirestore(url, "subido desde Raspberry Pi");*/
    }
    static int tomarFoto(String fichero) {
        String s;
        Process p;
        try {
            p = Runtime.getRuntime().exec("raspistill -n -o " + fichero);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            while ((s = br.readLine()) != null)
                System.out.println("line: " + s);
            p.waitFor();
            p.destroy();
            return p.exitValue();
        } catch (Exception e) {
            System.out.println("Error al tomar foto: libcamera-jpeg -o ");
            return -1;
        }

    }
    static private void subirFichero(String fichero, String referencia) {
        try {
            BlobId blobId = BlobId.of(BUCKET, referencia);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            storage.create(blobInfo, Files.readAllBytes(Paths.get(fichero)));
            //Da acceso al fichero a través de https. la URL es
            //https:storage.googleapis.com/BUCKET/nombre_recurso
            storage.createAcl(blobId, Acl.of(Acl.User.ofAllUsers(),
                    Acl.Role.READER));
            System.out.println("Fichero subido: " + referencia);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void guardarFirestore(String url, String titulo){
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection("foto").document();
        Map<String, Object> data = new HashMap<>();
        data.put("titulo", titulo);
        data.put("url", url);
        data.put("tiempo", System.currentTimeMillis());
        ApiFuture<WriteResult> result = docRef.set(data); //escritura asíncrona
        try { //al añadir result.get() bloquemos hasta respuesta
            System.out.println("Tiempo subida: "+result.get().getUpdateTime());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void suscribirMqtt(String topic, MqttCallback listener) {
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

    public static void publicarMqtt(String topic, String mensageStr) {
        try {
            MqttMessage message = new MqttMessage(mensageStr.getBytes());
            message.setQos(qos);
            message.setRetained(false);
            client.publish(topicRoot + "/" + topic, message);
            System.out.print("Publicando mensaje: " + topic+ "->"+mensageStr);
        } catch (MqttException e) {
            System.out.print("Error al publicar.");
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
