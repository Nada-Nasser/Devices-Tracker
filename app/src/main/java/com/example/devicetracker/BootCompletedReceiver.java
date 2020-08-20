package com.example.devicetracker;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if(intent.getAction().equalsIgnoreCase("android.intent.action.BOOT_COMPLETED"))
        {
            GlobalInfo globalInfo = new GlobalInfo(context);
            globalInfo.loadData();

            StartLocationServices(context);
        }
    }

    @SuppressLint("MissingPermission")
    public void StartLocationServices(Context context)
    {
        LocationListener trackerListener = new TrackerListener(context);
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, trackerListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, trackerListener);

        Intent mIntent = new Intent(context, UpdateLocationService.class);
        UpdateLocationService.enqueueWork(context, mIntent);
    }
}
