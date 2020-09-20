package com.example.officialproject1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.database.Cursor;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PerformanceActivity extends AppCompatActivity {
double mean = 0,standardDev = 0,variance = 0;
double score = 5f;
DataSQLHelper data;
TextView tMean,sddata;
boolean abruptEnd, crashed;
ScatterChart chart;
Intent intent;
FirebaseDatabase realtimeDB;
DatabaseReference dbRef;
Bundle recData;
double maxSpeed;
String email;
ArrayList<Entry> values;
Button button;
FirebaseFirestore db;
FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performance);
        sddata = findViewById(R.id.SDData);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        realtimeDB = FirebaseDatabase.getInstance();
        dbRef = realtimeDB.getReference("UserTripData");
        data = new DataSQLHelper(this);
        tMean = findViewById(R.id.MeanData);
        chart = findViewById(R.id.scatterChart);
        db.collection("Locations").document(auth.getCurrentUser().getEmail()).update("EndDate",new Date());
        button = findViewById(R.id.ratingGo);
        values = new ArrayList<>();
        intent = getIntent();
        recData = intent.getBundleExtra("ResultParameters");
        abruptEnd = recData.getBoolean("abruptEnd");
        crashed = recData.getBoolean("Crashed");
        getMean();
        getStandardDev();
        populateChart();
        checkScore();
        Thread storeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                dbTripStore();
            }
        });
        storeThread.start();
        data.dropTable();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PerformanceActivity.this, FinishActivity.class);
                recData.putDouble("Score",score);
                intent.putExtra("ResultParameters",recData);
                startActivity(intent);
                finish();
            }
        });
    }
    public void populateChart(){
        chart.getDescription().setEnabled(false);
        chart.setPinchZoom(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setMaxVisibleValueCount((int)maxSpeed);
        Legend l = chart.getLegend();
        ScatterDataSet set1 = new ScatterDataSet(values, "Speed");
        set1.setScatterShape(ScatterChart.ScatterShape.SQUARE);
        set1.setColor(ColorTemplate.COLORFUL_COLORS[0]);
        set1.setScatterShapeSize(8f);
        ScatterData data = new ScatterData(set1);
        chart.setData(data);
        chart.invalidate();
    }
    public void getMean(){
        Cursor cursor = data.getData();
        int totalValue = cursor.getCount();
        if(cursor.moveToFirst()){
            while (!cursor.isAfterLast()) {
                int key = cursor.getInt(cursor.getColumnIndex("_id"));
                double speed = cursor.getDouble(cursor.getColumnIndex("Speed"));
                if(maxSpeed < speed){
                    maxSpeed = speed;
                }
                values.add(new Entry((float)key,(float)speed));
                mean = mean + speed;
                cursor.moveToNext();
            }
        }
        mean = mean/totalValue;
        tMean.setText("Mean Speed: "+String.valueOf(mean));
    }
    public void getStandardDev(){
        Cursor cursor = data.getData();
        int totalValue = cursor.getCount();
        if(cursor.moveToFirst()){
            while (!cursor.isAfterLast()) {
                double speed = cursor.getDouble(cursor.getColumnIndex("Speed"));
                variance = variance + Math.pow(Math.abs(speed - mean),2);
                cursor.moveToNext();
            }
        }
        standardDev = Math.sqrt(variance/totalValue);
        sddata.setText("Variations in speed: " +String.valueOf(standardDev));
    }
    public void checkScore(){
        if((standardDev > (mean * 0.10)) && (standardDev <= (mean * 0.20)) ){
            score = score - 0.5f;
        }
        else if((standardDev > (mean * 0.20)) && (standardDev <= (mean * 0.30)) ){
            score = score - 1f;
        }
        else if((standardDev > (mean * 0.30)) && (standardDev <= (mean * 0.40)) ){
            score = score - 1.5f;
        }
        else if((standardDev > (mean * 0.40)) && (standardDev <= (mean * 0.50)) ){
            score = score - 2f;
        }
        else if((standardDev > (mean * 0.50)) && (standardDev <= (mean * 0.60)) ){
            score = score - 2.5f;
        }
        else if((standardDev > (mean * 0.60)) && (standardDev <= (mean * 0.70)) ){
            score = score - 3f;
        }
        else if((standardDev > (mean * 0.70)) && (standardDev <= (mean * 0.80)) ){
            score = score - 3.5f;
        }
        else if((standardDev > (mean * 0.80)) && (standardDev <= (mean * 0.90)) ){
            score = score - 4f;
        }
        else if((standardDev > (mean * 0.90)) && (standardDev <= (mean * 1)) ){
            score = score - 4.5f;
        }
        else if((standardDev < (mean * 1)) ){
            score = score - 5f;
        }
    }
    public void dbTripStore(){
        if(crashed == true){
            db.collection("Locations").document(auth.getCurrentUser().getEmail()).update("TripScore", "could not determine");
            db.collection("Locations").document(auth.getCurrentUser().getEmail()).update("TripStatus", "Crashed");
        }
        else {
            if (abruptEnd == false) {
                db.collection("Locations").document(auth.getCurrentUser().getEmail()).update("TripScore", String.valueOf(score));
                db.collection("Locations").document(auth.getCurrentUser().getEmail()).update("TripStatus", "Complete");
            }
            else {
                Map<String, Object> map = new HashMap<>();
                map.put("TripScore", "could not determine");
                map.put("TripStatus", "Incomplete");
                db.collection("Locations").document(auth.getCurrentUser().getEmail()).update(map);
            }
        }
        email = auth.getCurrentUser().getEmail();
        email = email.replace('.','_');
        db.collection("Locations").document(auth.getCurrentUser().getEmail()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                TripData tripData = new TripData(document.getDate("StartDate").toString(),document.getDate("EndDate").toString(),document.getGeoPoint("StartLocation"),document.getGeoPoint("EndLocation"),
                        document.getString("TripScore"),document.getString("TripStatus"));
                dbRef.child(email).child(document.getDate("StartDate").toString()).setValue(tripData);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
