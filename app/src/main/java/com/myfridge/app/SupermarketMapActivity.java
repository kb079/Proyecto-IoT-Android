package com.myfridge.app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myfridge.app.supermarkets.Result;
import com.myfridge.app.supermarkets.Root;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.location.LocationUpdateListener;
import com.tomtom.online.sdk.map.Icon;
import com.tomtom.online.sdk.map.MapFragment;
import com.tomtom.online.sdk.map.MapView;
import com.tomtom.online.sdk.map.MarkerAnchor;
import com.tomtom.online.sdk.map.MarkerBuilder;
import com.tomtom.online.sdk.map.OnMapReadyCallback;
import com.tomtom.online.sdk.map.SimpleMarkerBalloon;
import com.tomtom.online.sdk.map.TomtomMap;

import org.json.JSONObject;

public class SupermarketMapActivity extends FragmentActivity implements LocationUpdateListener, OnMapReadyCallback {

    protected LocationManager locationManager;
    private TomtomMap tomtomMap;
    private Location userLocation;
    private MapView mapView;
    private Context context;
    Root nearbySupermarkets;

    private final OnMapReadyCallback onMapReadyCallback =
            new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull TomtomMap map) {
                    //Map is ready here
                    tomtomMap = map;
                    setMap();
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

        context = this;
        super.onCreate(savedInstanceState);
        mapView = new MapView(getApplicationContext());
        setContentView(R.layout.supermarket_map);// Obtenemos el mapa de forma asíncrona (notificará cuando esté listo)

        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getAsyncMap(onMapReadyCallback);
    }

    public void getNearbySupermarkets(){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://api.tomtom.com/search/2/nearbySearch/.json?key=FiNxZjVXG3GrcF4EHYmO7XYu0cml0iCK&lat="+String.valueOf(userLocation.getLatitude())+"&lon="+String.valueOf(userLocation.getLongitude())+"&radius=1500&categorySet=7332005";
        JsonObjectRequest request = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (null != response) {
                            Log.i("HTTPRequest",response.toString());
                            ObjectMapper om = new ObjectMapper();
                            try {
                                nearbySupermarkets = om.readValue(response.toString(), Root.class);
                                Log.i("HTTPRequestPojo",nearbySupermarkets.toString());
                                for(Result supermarket:nearbySupermarkets.results){
                                    MarkerBuilder markerBuilder = new MarkerBuilder(new LatLng(supermarket.position.lat,supermarket.position.lon))
                                            .icon(Icon.Factory.fromResources(context, R.drawable.supermarket_icon_resized))
                                            .markerBalloon(new SimpleMarkerBalloon(supermarket.poi.name+"\na "+Math.round(supermarket.dist)+"m de ti"))
                                            .tag("more information in tag").iconAnchor(MarkerAnchor.Top)
                                            .decal(true); //By default is false
                                    tomtomMap.addMarker(markerBuilder);
                                }
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(request);

    }


    @Override
    public void onMapReady(@NonNull TomtomMap tomtomMap) {


    }

    public void setMap(){
        tomtomMap.setMyLocationEnabled(true);
        tomtomMap.zoomTo(30);
        this.tomtomMap.addLocationUpdateListener(this);
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


    @Override
    public void onLocationChanged(Location location) {
        tomtomMap.centerOnMyLocation();
        userLocation = this.tomtomMap.getUserLocation();
        getNearbySupermarkets();
        this.tomtomMap.removeLocationUpdateListener(this);
    }

}
