package com.myfridge.app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.tomtom.online.sdk.map.CameraFocusArea;
import com.tomtom.online.sdk.map.MapFragment;
import com.tomtom.online.sdk.map.MapView;
import com.tomtom.online.sdk.map.MarkerAnchor;
import com.tomtom.online.sdk.map.MarkerBuilder;
import com.tomtom.online.sdk.map.OnMapReadyCallback;
import com.tomtom.online.sdk.map.TomtomMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SupermarketMapActivity extends FragmentActivity implements OnMapReadyCallback {

    protected LocationManager locationManager;
    private TomtomMap tomtomMap;
    private MapView mapView;

    private final OnMapReadyCallback onMapReadyCallback =
            new OnMapReadyCallback() {
                @Override
                public void onMapReady(TomtomMap map) {
                    //Map is ready here
                    tomtomMap = map;
                    tomtomMap.setMyLocationEnabled(true);
                    //tomtomMap.collectLogsToFile(SampleApp.LOG_FILE_PATH);
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
       // locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        super.onCreate(savedInstanceState);
        mapView = new MapView(getApplicationContext());
        setContentView(R.layout.supermarket_map);// Obtenemos el mapa de forma asíncrona (notificará cuando esté listo)

        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getAsyncMap(onMapReadyCallback);
    }

/*
    public void loadNearbyMarkets(){
        MarkerBuilder markerBuilder = new MarkerBuilder(position)
                .icon(Icon.Factory.fromResources(context, R.drawable.ic_favourites))
                .markerBalloon(new SimpleMarkerBalloon(positionToText(position)))
                .tag("more information in tag").iconAnchor(MarkerAnchor.Bottom)
                .decal(true); //By default is false
        tomtomMap.addMarker(markerBuilder);
    }

 */
    @Override
    public void onMapReady(@NonNull TomtomMap tomtomMap) {
        tomtomMap.centerOnMyLocation();

    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }


}
