package com.example.officialproject1;
import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import java.util.HashMap;
import java.util.Map;
public class LocationService extends Service implements SensorEventListener{
    FirebaseAuth auth;
    FirebaseFirestore db;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationClient;
    LocationRequest mLocationRequestHighAccuracy;
    double endLat,endLong,radius;
    AlertDialog.Builder builder;
    AlertDialog alert;
    double lats,longs,currSpeed;
    Intent intent,speedIntent;
    Bundle locBundle;
    boolean crashed = false;
    private SensorManager sensorManager;
    private Sensor sensor;
    private int alertCounter = 0;
    private double GForce = 0;
    CountDownTimer timer;
    private final static long UPDATE_INTERVAL =  1000;
    private final static long FASTEST_INTERVAL = 1000;
    DataSQLHelper dataSQLHelper;
    double volume;
        @Override
        public void onCreate() {
            super.onCreate();

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            if (Build.VERSION.SDK_INT >= 26) {
                String CHANNEL_ID = "my_channel_01";
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                        "My Channel",
                        NotificationManager.IMPORTANCE_DEFAULT);

                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

                Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle("")
                        .setContentText("").build();

                startForeground(1, notification);
            }
            }
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            Bundle bundle = intent.getBundleExtra("latLong");
        endLat = bundle.getDouble("endLat");
        endLong = bundle.getDouble("endLong");
        radius = bundle.getDouble("circleRadius");
            createTimer();
            createCrashDialog();
            locBundle = new Bundle();
            dataSQLHelper = new DataSQLHelper(LocationService.this);
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            auth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            getLocation();
            return START_NOT_STICKY;
        }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void getLocation() {
            intent = new Intent("FinishTrip");
            speedIntent = new Intent("Speed");
            mLocationRequestHighAccuracy = new LocationRequest();
            mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequestHighAccuracy.setInterval(UPDATE_INTERVAL);
            mLocationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                stopSelf();
                return;
            }
            fusedLocationClient.requestLocationUpdates(mLocationRequestHighAccuracy, locationCallback = new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            Location location = locationResult.getLastLocation();
                            if (location != null) {
                                lats = locationResult.getLastLocation().getLatitude();
                                longs = locationResult.getLastLocation().getLongitude();
                                currSpeed = (locationResult.getLastLocation().getSpeed())*3.6;
                                speedIntent.putExtra("Speed",currSpeed);
                                sendBroadcast(speedIntent);
                                float[] distance = new float[2];
                                Location.distanceBetween(lats,longs,endLat,endLong,distance);
                                if(distance[0] <= radius) {
                                    intent.putExtra("finishTrip",true);
                                    sendBroadcast(intent);
                                }
                                else{
                                    intent.putExtra("finishTrip",false);
                                    sendBroadcast(intent);
                                }
                                Map<String,Object> userParameters = new HashMap<>();
                                dataSQLHelper.insertData(currSpeed,GForce);
                                userParameters.put("CurrentLocation", new GeoPoint(lats, longs));
                                userParameters.put("CurrentSpeed", currSpeed);
                                userParameters.put("GForce",GForce);
                                userParameters.put("Decibel",volume);
                                db.collection("UserLocations").document(auth.getCurrentUser().getEmail()).set(userParameters);
                            }
                        }
                    },
                    Looper.myLooper());
        }
    @Override
    public void onDestroy() {
            alert.dismiss();
            fusedLocationClient.removeLocationUpdates(locationCallback);
            sensorManager.unregisterListener(this,sensor);
            dataSQLHelper.close();
        super.onDestroy();
    }
    public void createTimer(){
        timer = new CountDownTimer(10000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                alert.setMessage("Press NO if otherwise, in "+millisUntilFinished/1000+" seconds");
            }

            @Override
            public void onFinish() {
                crashed = true;
                locBundle.putDouble("latitude",lats);
                locBundle.putDouble("longitude",longs);
                Intent i = new Intent();
                i.setAction("crashMessage");
                i.putExtra("Crashed",crashed);
                i.putExtra("locBundle",locBundle);
                sendBroadcast(i);
            }
        };
    }
    public void createCrashDialog(){
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Crash Detected");
        builder.setMessage("Press NO if otherwise, in 10 seconds");
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                alertCounter--;
                timer.cancel();
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        alert = builder.create();
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
    }
    public void showCrashDialog(){
        if(alertCounter < 1){
            alert.show();
            alertCounter++;
            timer.start();
        }
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        getAccelerometer(sensorEvent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    private void getAccelerometer(SensorEvent sensorEvent){
        float[] values = sensorEvent.values;
        float x = values[0];
        float y = values[1];
        float z = values[2];
        float gForce = (x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        GForce = gForce;
        if(GForce >= 10) {
            showCrashDialog();
        }
    }
}
