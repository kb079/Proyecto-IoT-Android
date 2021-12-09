package com.myfridge.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseUser;
import com.myfridge.app.databinding.ActivityMainBinding;
import com.myfridge.app.manager.fridge.Fridge;
import com.myfridge.app.manager.fridge.Item;
import com.myfridge.app.manager.fridge.Location;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import static com.myfridge.app.comunication.Mqtt.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements MqttCallback {

    private AppBarConfiguration mAppBarConfiguration;
    public static ActivityMainBinding binding;
    public static NavController navController;

    //---------------- MQTT -------------------//

    private static MqttClient client;

    //----------- Notifications ---------------//

    private int auxTemp = 0;
    private int auxDoor = 0;
    private NotificationManager notificationManager;
    private String CANAL_ID ;
    private int NOTIFICATION_ID ;

//-----------------------------------------------------------------------------------------------//
//----------------------------------- OnCreate -------------------------------------------//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //----------------------------------------------------------//
        //--------------- Conexión y suscripción MQTT --------------//

        connectMqtt();
        suscribeMqtt("temperatura", this);
        suscribeMqtt("magnetico", this);

        //----------------------------------------------------------//
        //------------------------ Binding -------------------------//

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //----------------------------------------------------------//
        //---------------------- Nav and toolBar -------------------//
        setSupportActionBar(binding.appBarMain2.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_profile, R.id.nav_about, R.id.nav_log_out)
                .setOpenableLayout(drawer)
                .build();

        // NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        navController = findNavController(getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment));
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //----------------------------------------------------------//
        //--------------------- User Info Menu ---------------------//

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        binding.userMenu.setText(usuario.getDisplayName());
        binding.userEmailMenu.setText(usuario.getEmail());

        //----------------------------------------------------------//
        //--------------------- Cerrar Sesion ----------------------//

        navigationView.getMenu().findItem(R.id.nav_log_out).setOnMenuItemClickListener(menuItem -> {
            cerrarSesion();
            return true;
        });

        //----------------------------------------------------------//
        //----------------------------------------------------------//

        // test("fridge0");
        //test("fridge1");

    }

//-----------------------------------------------------------------------------------------------//
//-----------------------------------------------------------------------------------------------//

    public void test(String id){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("data/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/fridges/").document(id);

        Fridge a = new Fridge(1, "test", 5, true, new Location(5.5, 5.6));
        ArrayList<Item> b = new ArrayList<Item>();
        /*
        b.add(new Item("555", 5, new Date().getTime()));
        b.add(new Item("test 2", 4, new Date().getTime()));
        b.add(new Item("test 3", 2, new Date().getTime()));
         */
        a.setItems(b);

        docRef.set(a);
    }

//-----------------------------------------------------------------------------------------------//
//----------------------------------- Navigation Menu -------------------------------------------//


    public NavController findNavController(@NonNull Fragment fragment) {
        Fragment findFragment = fragment;
        while (findFragment != null) {
            if (findFragment instanceof NavHostFragment) {
                return ((NavHostFragment) findFragment).getNavController();
            }
            Fragment primaryNavFragment = findFragment.requireFragmentManager()
                    .getPrimaryNavigationFragment();
            if (primaryNavFragment instanceof NavHostFragment) {
                return ((NavHostFragment) primaryNavFragment).getNavController();
            }
            findFragment = findFragment.getParentFragment();
        }

        // Try looking for one associated with the view instead, if applicable
        View view = fragment.getView();
        if (view != null) {
            return Navigation.findNavController(view);
        }
        throw new IllegalStateException("Fragment " + fragment
                + " does not have a NavController set");
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
//------------------------------------------------------------------------------------------------//
//------------------------------------------------------------------------------------------------//

    public void viewFridges(View v) {
        navController.navigate(R.id.nav_fridgesList);
    }

//-----------------------------------------------------------------------------------------------//
//--------------------------------------- Cerrar Sesión -----------------------------------------//

    public void cerrarSesion(){
        AuthUI.getInstance().signOut(MainActivity.this);
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        Toast toast =
                Toast.makeText(getApplicationContext(),
                        "Sesión cerrada", Toast.LENGTH_SHORT);
        toast.show();
        MainActivity.this.finish();
    }

//------------------------------------------------------------------------------------------------//
//------------------------------------------------------------------------------------------------//

//-----------------------------------------------------------------------------------------------//
//--------------------------------------------- MQTT --------------------------------------------//

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

    public static void disconectMqtt() {
        try {
            client.disconnect();
            Log.i(TAG, "Desconectado");
        } catch (MqttException e) {
            Log.e(TAG, "Error al desconectar.", e);
        }
    }

    @Override
    public void onDestroy() {
        disconectMqtt();
        super.onDestroy();
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

        if( topic == topicTemperature) {
            notificacionTemperatura(Double.parseDouble(payload));
        }
        else if(topic == topicDoorState){
            notificationPuertaAbierta(Integer.parseInt(payload));
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(TAG, "Entrega completa");
    }

//-----------------------------------------------------------------------------------------------//
//-----------------------------------------------------------------------------------------------//

//-----------------------------------------------------------------------------------------------//
//------------------------------------- Notificaciones ------------------------------------------//

    public void notificacionTemperatura( double temperatura){

        NOTIFICATION_ID = 0;
        CANAL_ID = "CanalTemperatura";

        notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CANAL_ID, "Mis Notificaciones",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Descripcion del canal");
            notificationManager.createNotificationChannel(notificationChannel);

            if (temperatura >= 27 && auxTemp == 0) {
                NotificationCompat.Builder notificacion =
                        new NotificationCompat.Builder(this, CANAL_ID)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("Alerta de temperatura")
                                .setContentText("La temperatura de la nevera ha excedido a " + temperatura + " ºC");

                PendingIntent intencionPendiente = PendingIntent.getActivity(
                        this, 0, new Intent(this, MainActivity.class), 0);
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

        NOTIFICATION_ID = 1;
        CANAL_ID = "CanalPuerta";

        notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CANAL_ID, "Mis Notificaciones",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Descripcion del canal");
            notificationManager.createNotificationChannel(notificationChannel);

            if (estado == 1 && auxDoor == 0) {
                NotificationCompat.Builder notificacion =
                        new NotificationCompat.Builder(this, CANAL_ID)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("Puerta abierta")
                                .setContentText("Parece que te has dejado la puerta de tu nevera abierta, cierrala!");

                PendingIntent intencionPendiente = PendingIntent.getActivity(
                        this, 0, new Intent(this, MainActivity.class), 0);
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

//------------------------------------------------------------------------------------------------//
//------------------------------------------------------------------------------------------------//

}