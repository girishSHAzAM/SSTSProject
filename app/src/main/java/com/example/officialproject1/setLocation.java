package com.example.officialproject1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class setLocation extends AppCompatActivity implements OnMapReadyCallback {
    Geocoder geocoder;
    FirebaseFirestore db;
    FirebaseAuth auth;
    LatLng finalLatLng,finalMyLoc;
    String finalPlace;
    double radius;
    private Marker markerName;
    EditText searchLoc;
    Button searchBtn, trackBtn;
    public MapView mMapView;
    GoogleMap map;
    public static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_location);
        mMapView = findViewById(R.id.setMapLocation);
        searchLoc = findViewById(R.id.input_set_location);
        searchBtn = findViewById(R.id.btn_search_loc);
        trackBtn = findViewById(R.id.btn_start_track);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(setLocation.this,"Please enable location permissions for the application",Toast.LENGTH_LONG).show();
            finish();
        }
        initGMap(savedInstanceState);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(markerName != null) {
                    markerName.remove();
                }
                String place = searchLoc.getText().toString();
                geocoder = new Geocoder(setLocation.this);
                List<Address> address = new ArrayList<>();
                try{
                    address = geocoder.getFromLocationName(place,1);
                }
                catch(Exception e){
                    Log.d("MapError",e.toString());
                }
                if(address.size() > 0){
                    Address addressRes = address.get(0);
                    moveCamera(new LatLng(addressRes.getLatitude(),addressRes.getLongitude()),19,addressRes.getAddressLine(0));
                    trackBtn.setVisibility(View.VISIBLE);
                }
            }
        });
        trackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread dbStoreLocations = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        auth = FirebaseAuth.getInstance();
                        db = FirebaseFirestore.getInstance();
                        Map<String, Object> locations = new HashMap<>();
                        locations.put("StartLocation",new GeoPoint(finalMyLoc.latitude,finalMyLoc.longitude));
                        locations.put("EndLocation",new GeoPoint(finalLatLng.latitude,finalLatLng.longitude));
                        locations.put("StartDate",new Date());
                        locations.put("EndDate",null);
                        db.collection("Locations").document(auth.getCurrentUser().getEmail()).set(locations);

                    }
                });
                dbStoreLocations.start();
                Intent intent = new Intent(setLocation.this,MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putDouble("endLat",finalLatLng.latitude);
                bundle.putDouble("endLong",finalLatLng.longitude);
                bundle.putDouble("circleRadius",radius);
                intent.putExtra("latLong",bundle);
                startActivity(intent);
                finish();
            }
        });
    }
    public void moveCamera(LatLng latLng,float zoom,String title){
        CameraUpdate desiredLocation = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        map.animateCamera(desiredLocation);
        finalLatLng = latLng;
        finalPlace = title;
        Log.d("coordinates",String.valueOf(finalLatLng.latitude));
        Log.d("coordinates",String.valueOf(finalLatLng.longitude));
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title);
        markerName = map.addMarker(markerOptions);
        CircleOptions circleOptions = new CircleOptions().center(latLng).radius(50).strokeColor(Color.RED)
                .fillColor(0x220000FF)
                .strokeWidth(5);
        final Circle mCircle = map.addCircle(circleOptions);
        radius = mCircle.getRadius();
        markerName.setDraggable(true);
        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                List<Address> address = new ArrayList<>();
                try{
                    address = geocoder.getFromLocation(marker.getPosition().latitude,marker.getPosition().longitude,1);
                }
                catch(Exception e){
                    Log.d("MapError",e.getMessage());
                    Toast.makeText(setLocation.this,"Cannot find location!",Toast.LENGTH_SHORT).show();
                }
                if(address.size() > 0){
                    Address addressRes = address.get(0);
                    moveCamera(marker.getPosition(),19, addressRes.getAddressLine(0));
                    marker.remove();
                    mCircle.remove();
                }
            }
        });
    }
    public void initGMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        if(location != null){
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            finalMyLoc = latLng;
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(latLng, 19);
            map.animateCamera(yourLocation);
        }
    }
    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();

    }
    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }
    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }
    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
