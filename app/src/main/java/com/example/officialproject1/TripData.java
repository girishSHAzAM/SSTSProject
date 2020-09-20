package com.example.officialproject1;

import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

public class TripData {
    private String startDate;
    private String endDate;
    private GeoPoint startLocation;
    private GeoPoint endLocation;
    private String tripScore;
    private String tripStatus;

    public TripData(String startDate, String endDate, GeoPoint startLocation, GeoPoint endLocation, String tripScore, String tripStatus) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.tripScore = tripScore;
        this.tripStatus = tripStatus;
    }
    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public GeoPoint getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(GeoPoint startLocation) {
        this.startLocation = startLocation;
    }

    public GeoPoint getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(GeoPoint endLocation) {
        this.endLocation = endLocation;
    }

    public String getTripScore() {
        return tripScore;
    }

    public void setTripScore(String tripScore) {
        this.tripScore = tripScore;
    }

    public String getTripStatus() {
        return tripStatus;
    }

    public void setTripStatus(String tripStatus) {
        this.tripStatus = tripStatus;
    }
}
