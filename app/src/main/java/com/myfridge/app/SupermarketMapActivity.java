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

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SupermarketMapActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mapa;
    protected LocationManager locationManager;
    LatLng ubicacion = new LatLng(0,0);

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
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.supermarket_map);// Obtenemos el mapa de forma asíncrona (notificará cuando esté listo)
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapa = googleMap;
        mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mapa.getUiSettings().setZoomControlsEnabled(false);
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            mapa.setMyLocationEnabled(false);
            mapa.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
            mapa.getUiSettings().setCompassEnabled(true);
        }
        ubicacion = new LatLng(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude(), locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude());
        mapa.addMarker(new MarkerOptions()
                .position(ubicacion)
                .title("Mi Nevera")
                .snippet("La nevera de mi casa")
                .icon(BitmapDescriptorFactory.fromResource(android.R.drawable.ic_menu_compass))
                .anchor(0.5f, 0.5f));
        mapa.animateCamera(CameraUpdateFactory.newLatLng(ubicacion));
        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 16));
        //mapa.setOnMapClickListener(this);

        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+String.valueOf(ubicacion.latitude)+","+String.valueOf(ubicacion.longitude)+"&radius=1000&type=supermarket&key=AIzaSyCbf2JFqQU8m9wNGpUE4tRC7M8tNOqbar4";
        //String url ="https://maps.googleapis.com/maps/api/place/textsearch/json?location="+String.valueOf(ubicacion.latitude)+","+String.valueOf(ubicacion.longitude)+"&radius=1000&type=supermarket&key=AIzaSyCbf2JFqQU8m9wNGpUE4tRC7M8tNOqbar4";
        JsonObjectRequest request = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (null != response) {
                            //handle your response
                            Log.i("HTTPRequest",response.toString());
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
    public void onLocationChanged(@NonNull Location location) {

    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}
