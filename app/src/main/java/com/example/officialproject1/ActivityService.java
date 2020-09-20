package com.example.officialproject1;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

public class ActivityService extends IntentService {
    protected static final String TAG = ActivityService.class.getSimpleName();
    public ActivityService(){
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String dActivity = "Detecting Activity...";
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        DetectedActivity activity = result.getMostProbableActivity();
        switch (activity.getType()){
            case DetectedActivity.IN_VEHICLE:
                dActivity = "In Vehicle";
                break;
            case DetectedActivity.ON_FOOT:
                dActivity = "On Foot";
                break;
            case DetectedActivity.STILL:
                dActivity = "Still";
                break;
            case DetectedActivity.RUNNING:
                dActivity = "On Foot";
                break;
            case DetectedActivity.WALKING:
                dActivity = "On Foot";
                break;
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("DETECTED_ACTIVITY",dActivity);
        editor.putString("OriginalActivity",String.valueOf(activity.getType()));
        editor.putString("Confidence",String.valueOf(activity.getConfidence()));
        editor.apply();
    }
}
