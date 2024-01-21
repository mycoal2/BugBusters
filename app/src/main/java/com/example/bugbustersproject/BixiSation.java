package com.example.bugbustersproject;

public class BixiSation {
    public String getIntersectionName() {
        return IntersectionName;
    }

    public void setIntersectionName(String intersectionName) {
        IntersectionName = intersectionName;
    }

    private String IntersectionName;

    public float getLatitude() {
        return Latitude;
    }

    public void setLatitude(float latitude) {
        Latitude = latitude;
    }

    private float Latitude;

    public float getLongitude() {
        return Longitude;
    }

    public void setLongitude(float longitude) {
        Longitude = longitude;
    }

    private float Longitude;
    public final int CAPACITY = 32;

    public BixiSation(String intersectionName, float latitude, float longitude) {
        IntersectionName = intersectionName;
        Latitude = latitude;
        Longitude = longitude;

    }
}
