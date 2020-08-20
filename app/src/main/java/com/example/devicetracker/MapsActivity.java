package com.example.devicetracker;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private  LatLng finderLatLng;
    private  User Finder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        LoadMaps();
    }

    private void LoadMaps()
    {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void LoadLocation(String phoneNumber)
    {
        FirebaseDatabase.getInstance().getReference("users").child(phoneNumber)
                .child("Location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                try
                {
                    UserLocationInfo userLocationInfo = dataSnapshot.getValue(UserLocationInfo.class);

                    if(userLocationInfo != null)
                    {
                        finderLatLng = new LatLng(userLocationInfo.Latitude,userLocationInfo.Longitude);
                        AddFinderMarker();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"userLocationInfo = null" , Toast.LENGTH_SHORT).show();
                    }

                    /*
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren())
                    {
                        UserLocationInfo userLocationInfo = childSnapshot.getValue(UserLocationInfo.class);

                        if(userLocationInfo != null)
                        {
                            finderLatLng = new LatLng(userLocationInfo.Latitude,userLocationInfo.Longitude);
                            AddFinderMarker();
                        }
                    }*/
                }catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }

    private void AddFinderMarker()
    {
        mMap.clear();

        mMap.addMarker(new MarkerOptions()
                .position(finderLatLng)
                .title(Finder.getName())
                .snippet(Finder.getPhoneNumber()));

        LatLng myLoc = new LatLng( TrackerListener.location.getLatitude() , TrackerListener.location.getLongitude());

        mMap.addMarker(new MarkerOptions()
                .position(myLoc)
                .title("Your Location")
                .snippet(GlobalInfo.getPhoneNumber()));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(finderLatLng));
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        Bundle bundle = getIntent().getExtras();
        String phoneNumber = bundle.getString("phoneNumber" , "none");
        String name = bundle.getString("name" , "none");
        Finder = new User(phoneNumber,name);

        LoadLocation(phoneNumber);

    }
}