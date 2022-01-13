package com.myfridge.raspbphoto;

import static com.myfridge.raspbphoto.MqttManager.suscribirMqtt;

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
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import io.grpc.LoadBalancerRegistry;
import io.grpc.internal.PickFirstLoadBalancerProvider;

public class RaspbPhoto {

    public static final String PATH_TO_CREDENTIALS = "gcloud-key.json";
    public static final String BUCKET = "proyectoapp-iot-1-3.appspot.com";

    public static Storage storage;

    public static void main(String[] args){

        System.out.println("Starting Raspberry Photo app...");

        LoadBalancerRegistry.getDefaultRegistry().register(new PickFirstLoadBalancerProvider());

        final InputStream gFile = RaspbPhoto.class.getClassLoader().getResourceAsStream(PATH_TO_CREDENTIALS);

        //Start Firebase
            try {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(RaspbPhoto.class.getClassLoader().getResourceAsStream(PATH_TO_CREDENTIALS)))
                        .build();

                FirebaseApp.initializeApp(options);
            } catch (IOException e) {
                e.printStackTrace();
            }

        guardarFirestore(null, "test");


        //Connect to Google Cloud storage
        try {
            storage = StorageOptions.newBuilder()
                    .setCredentials(ServiceAccountCredentials.fromStream(gFile))
                    .build()
                    .getService();
        } catch (IOException e) {
            e.printStackTrace();
        }

        long tiempo = System.currentTimeMillis();

        MqttManager.conectarMqtt();

        suscribirMqtt("foto", new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) { }

            @Override
            public void messageArrived(String arg0, MqttMessage arg1)  {

                System.out.println(
                        "  Topic:\t" + arg0 +
                                "  Message:\t" + new String(arg1.getPayload()) +
                                "  QoS:\t" + arg1.getQos());

                String nombreFichero = UUID.randomUUID().toString();
                //tomarFoto("captura.jpeg");

                subirFichero("captura.jpeg", "fotos/" +nombreFichero +".jpeg");

                String url = "https://storage.googleapis.com/" + BUCKET + "/"
                        +"fotos/" + nombreFichero+".jpeg";
                guardarFirestore(url, "subido desde Raspberry Pi");
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken arg0) { }

        });
    }

    //Methods

    private static int tomarFoto(String fichero) {
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

    private static void subirFichero(String fichero, String referencia) {
        BlobId blobId = BlobId.of(BUCKET, referencia);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, fichero.getBytes());
        //Da acceso al fichero a través de https. la URL es
        //https:storage.googleapis.com/BUCKET/nombre_recurso
        storage.createAcl(blobId, Acl.of(Acl.User.ofAllUsers(),
                Acl.Role.READER));
        System.out.println("Fichero subido: " + referencia);
    }

   private static void guardarFirestore(String url, String titulo){

       Firestore db = FirestoreClient.getFirestore();

       DocumentReference docRef = db.collection("fotos").document();

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

}
