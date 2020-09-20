package com.example.officialproject1;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
public class MainActivity extends AppCompatActivity {
    Intent serviceIntent,finishIntent;
    NotificationManager mNotificationManager;
    double endLat,endLong;
    Bundle finishBundle;
    TextView speedText;
    String eNumber;
    boolean isNotificationVisible = false;
    private int LOCATION_PERMISSION_CODE = 1;
    Button endTripBtn;
    FirebaseFirestore db;
    FirebaseAuth auth;
    UpdateReceiver updateReceiver;
    EndReceiver endReceiver;
    SpeedReceiver speedReceiver;
    Button googleMaps;
    Bundle bundle;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        finishBundle = new Bundle();
        serviceIntent = new Intent(this,LocationService.class);
        finishIntent = new Intent(MainActivity.this,PerformanceActivity.class);
        auth = FirebaseAuth.getInstance();
        endTripBtn = findViewById(R.id.btn_end_trip);
        speedText = findViewById(R.id.textView3);
        Intent intent = getIntent();
        bundle = intent.getBundleExtra("latLong");
        serviceIntent.putExtra("latLong",bundle);
        endLat = bundle.getDouble("endLat");
        endLong = bundle.getDouble("endLong");
        googleMaps = findViewById(R.id.btn_googleMaps);
        if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED)) {
            startLocationService();
            updateReceiver = new UpdateReceiver();
            registerReceiver(updateReceiver, new IntentFilter("crashMessage"));
            endReceiver = new EndReceiver();
            registerReceiver(endReceiver, new IntentFilter("FinishTrip"));
            speedReceiver = new SpeedReceiver();
            registerReceiver(speedReceiver,new IntentFilter("Speed"));
            getEmergencyNumber();
        }
        else{
            requestLocationPermission();
        }
        endTripBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finishBundle.putBoolean("abruptEnd",false);
                finishBundle.putBoolean("Crashed",false);
                finishIntent.putExtra("ResultParameters",finishBundle);
                stopService(serviceIntent);
                startActivity(finishIntent);
                finish();
            }
        });
        googleMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String latitude = String.valueOf(endLat);
                String longitude = String.valueOf(endLong);
                Uri mapUri = Uri.parse("google.navigation:q="+latitude+","+longitude);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW,mapUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                try{
                    if(mapIntent.resolveActivity(MainActivity.this.getPackageManager()) != null){
                        startActivity(mapIntent);
                    }
                }
                catch(Exception e){
                    Log.d("ErrorGMAP",e.getMessage());
                    Toast.makeText(MainActivity.this,"Error launching google maps",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void getEmergencyNumber(){
        db.collection("users").document(auth.getCurrentUser().getEmail()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot document = task.getResult();
                    eNumber = document.getString("eContact");
            }
        });
    }
    public void requestLocationPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
            new AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("Permissions required to preserve application functionality")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.SEND_SMS},
                                    LOCATION_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(MainActivity.this,"Permission Denied! Please Restart The Application.",Toast.LENGTH_SHORT).show();
                            dialogInterface.dismiss();
                        }
                    }).create().show();
        }else{
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.SEND_SMS},
                    LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == LOCATION_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(MainActivity.this,"Permission Granted!",Toast.LENGTH_SHORT).show();
                startLocationService();
            }
            else{
                Toast.makeText(MainActivity.this,"Permission Denied! Please Restart The Application.",Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void showNotification(boolean flag) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, "notify_001");
        Intent ii = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, ii,0);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        mBuilder.setContentTitle("End Trip");
        mBuilder.setContentText("It seems you have arrived at your destination. End Trip ?");
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setAutoCancel(true);
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "Your_channel_id";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }
        if (flag == true) {
            isNotificationVisible = true;
            mBuilder.build().flags |= Notification.FLAG_AUTO_CANCEL;
                mNotificationManager.notify(0, mBuilder.build());
        }
    }
    private class UpdateReceiver extends BroadcastReceiver {
            boolean crashValue = false;
        @Override
        public void onReceive(Context context, Intent intent) {
            crashValue = intent.getBooleanExtra("Crashed",false);
            MainActivity.this.getCrashValue(crashValue,intent.getBundleExtra("locBundle"));
        }
    }
    private class EndReceiver extends BroadcastReceiver{
            boolean finishTrip;
        @Override
        public void onReceive(Context context, Intent intent) {
                finishTrip = intent.getBooleanExtra("finishTrip",false);
                MainActivity.this.finishTrip(finishTrip);
        }
    }
    private class SpeedReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
              MainActivity.this.setSpeed(intent.getDoubleExtra("Speed",0));
        }
    }
    private void setSpeed(double speed){
        String strSpeed = String.format("%.2f",speed);
        Log.d("strSpeed",strSpeed);
        speedText.setText("Speed is: "+strSpeed+" KM/H");
    }
    public void finishTrip(boolean finishTrip){
        if(finishTrip == true){
                                                showNotification(true);
                                    endTripBtn.setVisibility(View.VISIBLE);
        }
        else{
            endTripBtn.setVisibility(View.INVISIBLE);
        }
    }
    public void getCrashValue(boolean crashValue,Bundle locBundle){
        if(crashValue == true){
            double lats = locBundle.getDouble("latitude");
            double longs = locBundle.getDouble("longitude");
            sendEmergencyMsg(lats,longs);
            finishBundle.putBoolean("abruptEnd",true);
            finishBundle.putBoolean("Crashed",true);
            finishIntent.putExtra("ResultParameters",finishBundle);
            stopService(serviceIntent);
            startActivity(finishIntent);
            finish();
        }
    }
    public void sendEmergencyMsg(double lats, double longs){
        StringBuffer msg = new StringBuffer("Help me at location http://maps.google.com?q=");
        msg.append(lats);
        msg.append(",");
        msg.append(longs);
        if(eNumber != null){
            SmsManager manager = SmsManager.getDefault();
            manager.sendTextMessage(eNumber,null,msg.toString(),null,null);
        }else{
            Toast.makeText(this,"Cannot send emergency notification !", Toast.LENGTH_SHORT).show();
        }
    }
    private void startLocationService(){
        if(!isLocationServiceRunning()){
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                MainActivity.this.startForegroundService(serviceIntent);
            }else{
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.example.officialproject1.LocationService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        if(isNotificationVisible == true) {
            mNotificationManager.cancel(0);
        }
        super.onDestroy();
    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder endAlert = new AlertDialog.Builder(MainActivity.this).setTitle("End Trip ?")
                                        .setMessage("Do you want to end the trip ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finishBundle.putBoolean("abruptEnd",true);
                        finishBundle.putBoolean("Crashed",false);
                        finishIntent.putExtra("ResultParameters",finishBundle);
                        stopService(serviceIntent);
                        startActivity(finishIntent);
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        endAlert.setCancelable(false);
        AlertDialog alert = endAlert.create();
        alert.show();
    }
}
