package com.example.devicetracker;


import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class UserLocationInfo
{
    public double Latitude;
    public double  Longitude;
    public String  LastOnlineDate;

    public UserLocationInfo(double latitude, double longitude, String lastOnlineDate) {
        Latitude = latitude;
        Longitude = longitude;
        LastOnlineDate = lastOnlineDate;
    }

    public UserLocationInfo() {
    }

    @Override
    public String toString() {
        return "UserLocationInfo{" +
                "Latitude=" + Latitude +
                ", Longitude=" + Longitude +
                ", LastOnlineDate='" + LastOnlineDate + '\'' +
                '}';
    }
/*
    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }

    public String getLastOnlineDate() {
        return LastOnlineDate;
    }

    public void setLastOnlineDate(String lastOnlineDate) {
        LastOnlineDate = lastOnlineDate;
    }
*/
    @Exclude
    public Map<String, Object> toMap()
    {
        HashMap<String, Object> result = new HashMap<>();
        result.put("Latitude", Latitude);
        result.put("Longitude", Longitude);
        result.put("LastOnlineDate", LastOnlineDate);
        return result;
    }
}
