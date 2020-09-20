package com.example.officialproject1;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CrashDetectionService extends Service implements SensorEventListener {
    FirebaseAuth auth;
    FirebaseFirestore db;
    Thread crashThread;
    private SensorManager sensorManager;
    private Sensor sensor;
    public CrashDetectionService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        if(Build.VERSION.SDK_INT >= 26){
            NotificationChannel channel = new NotificationChannel("locationChannel","MyLocationChannel", NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            Notification notification = new NotificationCompat.Builder(this,"locationChannel")
                    .setContentTitle("CrashDetectionService")
                    .setContentText("")
                    .build();
            startForeground(1,notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                sensorManager.registerListener(CrashDetectionService.this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(sensorEvent);
        }
    }
    private void getAccelerometer(SensorEvent sensorEvent){
        float[] values = sensorEvent.values;
        float x = values[0];
        float y = values[1];
        float z = values[2];

        float gForce = (x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        Log.d("GFORCE",String.valueOf(gForce));
        db.collection("UserLocations").document(auth.getCurrentUser().getEmail()).update("GForce",(double)gForce);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
