package com.example.officialproject1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TripHistory extends AppCompatActivity {
FirebaseDatabase realtimeDB;
DatabaseReference dbRef;
private int limit = 50;
private int tripCounter = 1;
FirebaseFirestore db;
FirebaseAuth auth;
String email;
RecyclerView viewTrips;
Address origAdd,destAdd;
LoadingDialog loadingDialog;
List<String> startDate,endDate,origin,destination,score,tripStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_history);
        loadingDialog = new LoadingDialog(this);
        loadingDialog.startLoadDialog();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        email = auth.getCurrentUser().getEmail().replace('.','_');
        realtimeDB = FirebaseDatabase.getInstance();
        dbRef = realtimeDB.getReference("UserTripData");
        viewTrips = findViewById(R.id.viewTrips);
        startDate = new ArrayList<>();
        endDate = new ArrayList<>();
        origin = new ArrayList<>();
        destination = new ArrayList<>();
        score = new ArrayList<>();
        tripStatus = new ArrayList<>();
        dbRef.child(email).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean result = getAllDetails((Map<String, Object>) dataSnapshot.getValue());
//                    if(startDate.size() != 0 || endDate.size() != 0 || origin.size() != 0 || destination.size() != 0 || score.size() != 0 || tripStatus.size() != 0) {
                if(result == true){
                        viewTrips.setLayoutManager(new LinearLayoutManager(TripHistory.this));
                        viewTrips.setAdapter(new R_Adapter(TripHistory.this, startDate, endDate, origin, destination, score, tripStatus));
                    }
                    else{
                        setContentView(R.layout.activity_no_trip_history);
                    }
                    loadingDialog.dismissLoadDialog();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                loadingDialog.dismissLoadDialog();
                Toast.makeText(TripHistory.this, "Error loading data", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    private boolean getAllDetails(Map<String,Object> tripDetails){
        if(tripDetails == null){
            return false;
        }
        else {
            for (Map.Entry<String, Object> entry : tripDetails.entrySet()) {
                if (tripCounter > limit) {
                    break;
                }
                Map singleTrip = (Map) entry.getValue();
                startDate.add((String) singleTrip.get("startDate"));
                endDate.add((String) singleTrip.get("endDate"));
                Map<String, Object> start = (Map) singleTrip.get("startLocation");
                Map<String, Object> end = (Map) singleTrip.get("endLocation");
                GeoPoint startPt = new GeoPoint((double) start.get("latitude"), (double) start.get("longitude"));
                GeoPoint endPt = new GeoPoint((double) end.get("latitude"), (double) end.get("longitude"));
                toAddress(startPt, endPt);
                score.add((String) singleTrip.get("tripScore"));
                tripStatus.add((String) singleTrip.get("tripStatus"));
                tripCounter++;
            }
            return true;
        }
    }
    private void toAddress(GeoPoint startPt, GeoPoint endPt){
        Geocoder geocoder = new Geocoder(TripHistory.this);
        try {
            List<Address> origLoc = geocoder.getFromLocation(startPt.getLatitude(), startPt.getLongitude(), 1);
            List<Address> destLoc = geocoder.getFromLocation(endPt.getLatitude(), endPt.getLongitude(), 1);
            origAdd = origLoc.get(0);
            destAdd = destLoc.get(0);
        } catch (IOException e) {
            Toast.makeText(TripHistory.this, "Cannot get origin and destination", Toast.LENGTH_SHORT).show();
        }
        origin.add(origAdd.getAddressLine(0));
        destination.add(destAdd.getAddressLine(0));
    }
}
