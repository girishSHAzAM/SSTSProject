package com.example.officialproject1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
public class DisplayActivity extends AppCompatActivity {
    Handler welcomeHandler;
    Thread welcomeThread;
    Button startTrip, rideHistory, checkProfile;
    TextView hello;
    LoadingDialog loader;
    FirebaseAuth userAuth;
    FirebaseFirestore db;
    private int PERMISSION_CODE = 1;
    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5469;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        statusCheck();
        checkPermission();
        loader = new LoadingDialog(DisplayActivity.this);
        loader.startLoadDialog();
        userAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        hello = findViewById(R.id.UserWelcome);
        welcomeHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                hello.setText("Hello, " + bundle.getString("Name"));
                loader.dismissLoadDialog();
            }
        };
        welcomeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                db.collection("users").document(userAuth.getCurrentUser().getEmail()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();
                        Bundle bundle = new Bundle();
                        Message message = welcomeHandler.obtainMessage();
                        bundle.putString("Name", document.getString("Name"));
                        message.setData(bundle);
                        welcomeHandler.sendMessage(message);
                        if (document.getString("OldEmail") != null) {
                            db.collection("users").document(document.getString("OldEmail")).delete();
                        }
                    }
                });
            }
        });
        welcomeThread.start();
        startTrip = findViewById(R.id.start_trip);
        rideHistory = findViewById(R.id.ride_history);
        checkProfile = findViewById(R.id.Profile);
        startTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(DisplayActivity.this, "Checking permissions...", Toast.LENGTH_SHORT).show();
                if ((ContextCompat.checkSelfPermission(DisplayActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(DisplayActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED)) {

                        Intent startTrip = new Intent(DisplayActivity.this, setLocation.class);
                        startActivity(startTrip);
                        Toast.makeText(DisplayActivity.this, "Enter destination", Toast.LENGTH_SHORT).show();


                } else {
                    requestLocPermissions();
                }
            }
        });
        rideHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent rideIntent = new Intent(DisplayActivity.this, TripHistory.class);
                startActivity(rideIntent);
                Toast.makeText(DisplayActivity.this, "Your rides so far!", Toast.LENGTH_SHORT).show();
            }
        });
        checkProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentProf = new Intent(DisplayActivity.this, ProfileActivity.class);
                startActivity(intentProf);
                Toast.makeText(DisplayActivity.this, "Your profile!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                checkPermission();
            }
        }

    }
    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            }
        }
    }
    public void requestLocPermissions() {
        if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) && (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO))) {
            new AlertDialog.Builder(this)
                    .setTitle("Location and SMS Permissions Needed")
                    .setMessage("Location and SMS permissions are needed for the proper functioning of this application.")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(DisplayActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.SEND_SMS}, PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.SEND_SMS}, PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent startTrip = new Intent(DisplayActivity.this, setLocation.class);
                startActivity(startTrip);
                Toast.makeText(DisplayActivity.this, "Enter destination", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DisplayActivity.this, "Please enable required permissions to use the app.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.display_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.manual:
                Intent intent1 = new Intent(DisplayActivity.this, ManualActivity.class);
                startActivity(intent1);
                return true;
            case R.id.SignOut:
                AlertDialog.Builder builder = new AlertDialog.Builder(DisplayActivity.this);
              builder.setMessage("Do you wish to sign out ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                userAuth.signOut();
                                Intent intent2 = new Intent(DisplayActivity.this, login_activity.class);
                                Toast.makeText(DisplayActivity.this, "You are logged out", Toast.LENGTH_SHORT).show();
                                startActivity(intent2);
                                finish();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.setTitle("Sign Out ?");
                alert.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    private void buildAlertMessageNoGps() {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        final android.app.AlertDialog alert = builder.create();
        alert.show();
    }
}

