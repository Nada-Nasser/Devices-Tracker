package com.example.devicetracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;

public class TrackerListener implements LocationListener
{
    static Location location;
    static boolean isRunning = false;

    @SuppressLint("MissingPermission")
    public TrackerListener(Context context)
    {
        isRunning = true;
        LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);

        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location == null)
        {
            location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

        }

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        this.location = location;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}
