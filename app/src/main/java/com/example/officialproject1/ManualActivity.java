package com.example.officialproject1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

public class ManualActivity extends AppCompatActivity {
ArrayList<String> guidelines;
LoadingDialog loadingDialog;
RecyclerView rViewGuide;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);
        rViewGuide = findViewById(R.id.viewGuide);
        loadingDialog = new LoadingDialog(this);
        loadingDialog.startLoadDialog();
        guidelines = new ArrayList<>();
        guidelines.add("IMPORTANT: NEVER REMOVE THE APP FROM MEMORY DURING THE TRIP");
        guidelines.add("1. Start a trip");
        guidelines.add("--> Enable floating windows for the app to receive alerts. ");
        guidelines.add("--> Enable background location permission for the app to get location updates in the background which is absolutely necessary" +
                "  for proper functioning");
        guidelines.add("--> Enable SMS permissions for the app to send notifications to your emergency contact.");
        guidelines.add("--> Proceed to enter your destination and set it accurately by adjusting the marker present in the MapView.");
        guidelines.add("--> The circle appearing around the marker indicates the drop-off radius(50 m) within which the 'End Trip' button is visible. ");
        guidelines.add("--> Then press the 'Start Tracking' button to start the background service. ");
        guidelines.add("--> Press the 'Google Maps' button to access Google Maps Navigation to your location ");
        guidelines.add("     1.1 Crash Detection");
        guidelines.add("          --> The factor that triggers the crash alert is a simple shake of the phone. " +
                                   "This is because it is highly improbable to simulate crashes in realtime and is beyond the scope of this application. " +
                                    "The application, right now, could only show the responses it can send in case of a supposed 'crash'.");
        guidelines.add("          --> The crash alert appears over the current window of your phone. ");
        guidelines.add("          --> Press No within 10 seconds if its a false positive. ");
        guidelines.add("          --> If the 10 second time limit has been exceeded, it is assumed to have been a crash. ");
        guidelines.add("          --> In that case, an emergency notification with your current co-ordinates are sent to your emergency contact and your trip is finished. ");
        guidelines.add("--> End trip to complete a trip and receive a score.");
        guidelines.add("--> Press the back button to finish trip abruptly under which case, you would not be given a score.");
        guidelines.add("2. View Trip History");
        guidelines.add("--> View your last 50 trips taken.");
        guidelines.add("3. View Profile");
        guidelines.add("--> View and edit your profile.");
        guidelines.add("NOTE: In case if you change your E-Mail Address, your old E-Mail Address cannot be used to register a new account for a period of 90 days. ");
        rViewGuide.setLayoutManager(new LinearLayoutManager(this));
        rViewGuide.setAdapter(new ManualRAdapter(this,guidelines));
        loadingDialog.dismissLoadDialog();
    }
}
