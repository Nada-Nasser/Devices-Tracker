package com.example.devicetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
{
    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSIONS = 111;
    ArrayList<User> FindersList;
    FindersCustomAdapter myFindersCustomAdapter;

    ListView FindersListView;
    GlobalInfo globalInfo;

    //get access to location Permissions
    final private int REQUEST_CODE_ASK_LOCATION_PERMISSIONS = 123;

    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        globalInfo = new GlobalInfo(this);
        globalInfo.loadData();

        databaseReference = FirebaseDatabase.getInstance().getReference();

        CheckLocationPermissions();

        FindersListView = findViewById(R.id.friendsList);

        FindersList = new ArrayList<>();

        myFindersCustomAdapter = new FindersCustomAdapter(FindersList);

        FindersListView.setAdapter(myFindersCustomAdapter);

        CheckContactsPermissions();

        FindersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                User finder = FindersList.get(i);

                GlobalInfo.updateUserInfo(finder.getPhoneNumber());

                Intent googleMapsIntent = new Intent(getApplicationContext(),MapsActivity.class);
                googleMapsIntent.putExtra("phoneNumber" , finder.getPhoneNumber());
                googleMapsIntent.putExtra("name" , finder.getName());

                startActivity(googleMapsIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckLocationPermissions();
    }

    private void refreshFindersList()
    {
        FirebaseDatabase.getInstance().getReference("users").child(GlobalInfo.getPhoneNumber())
                .child("Finders").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                try
                {
                    Map<String, Object> td = new HashMap<>();
                    FindersList.clear();

                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren())
                    {
                        String Number = childSnapshot.getKey();
                        boolean canFindMe = childSnapshot.getValue(boolean.class);

                        if(Number != null && canFindMe)
                        {
                            td.put(Number , true);

                            //FindersList.add(new User(Number, "Name"));

                            //Toast.makeText(getApplicationContext(),Number,Toast.LENGTH_SHORT).show();
                        }
                    }



                    // get all contact to list
                    ArrayList<User> list_contact = new ArrayList<>();
                    Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

                    while (cursor.moveToNext())
                    {
                        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                        String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        list_contact.add(new User(GlobalInfo.FormatPhoneNumber(phoneNumber), name));

                        if(!phoneNumber.equalsIgnoreCase("0"))
                            Log.i("Numbers", "onDataChange: " + phoneNumber + " Formated : " + GlobalInfo.FormatPhoneNumber(phoneNumber));
                    }

                    // if the name is save chane his text
                    for (String Numbers : td.keySet()) {
                        for (User cs : list_contact)
                        {
                            //IsFound = SettingSaved.WhoIFindIN.get(cs.Detals);  // for case who i could find list
                            if (cs.getPhoneNumber().length() > 0)
                                if (Numbers.contains(cs.getPhoneNumber())) {
                                    FindersList.add(new User(cs.getPhoneNumber(), cs.getName()));

                                    Toast.makeText(getApplicationContext() ,  "SHOW : " + cs.getPhoneNumber() + ":" + cs.getName() , Toast.LENGTH_SHORT).show();
                                    break;
                                }
                        }
                    }
                    cursor.close();

                    if(FindersList.isEmpty())
                    {
                        FindersList.add(new User("none" , "none"));
                    }

                    myFindersCustomAdapter.notifyDataSetChanged();

                }catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        myFindersCustomAdapter.notifyDataSetChanged();
    }

    class FindersCustomAdapter extends BaseAdapter {

        ArrayList <User> contactsList;

        public FindersCustomAdapter(ArrayList<User> contactsList) {
            this.contactsList = contactsList;
        }

        @Override
        public int getCount() {
            return contactsList.size();
        }

        @Override
        public Object getItem(int i) {
            return contactsList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            final User contactItem = contactsList.get(i);
            LayoutInflater inflater = getLayoutInflater();

            if(!contactItem.getName().equalsIgnoreCase("none"))
            {
                View contactsView = inflater.inflate(R.layout.single_row_conact , null);
                TextView contactNameTextView = contactsView.findViewById(R.id.contact_username);
                TextView contactPhoneNumber = contactsView.findViewById(R.id.contact_phone_number);
                contactNameTextView.setText(contactItem.getName());
                contactPhoneNumber.setText(contactItem.getPhoneNumber());
                return contactsView;

            }
            else
            {
                View contactsView = inflater.inflate(R.layout.news_ticket_no_news , null);

                return contactsView;
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu , menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId()){
            case R.id.addTracker:
                Intent trackerIntent = new Intent(this,MyTrackers.class);
                startActivity(trackerIntent);
                return true;
            case R.id.help:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //access to Permissions
    void CheckLocationPermissions(){
        if (Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ASK_LOCATION_PERMISSIONS);
                return ;
            }
        }

        StartLocationServices();// init the contact list
    }

    //access to Permissions
    private void CheckContactsPermissions()
    {
        if (Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) !=
                    PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                        REQUEST_CODE_ASK_CONTACTS_PERMISSIONS);
                return ;
            }
        }

        refreshFindersList();// init the contact list
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_LOCATION_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    StartLocationServices();// init the contact list
                } else {
                    // Permission Denied
                    Toast.makeText( this,"your message" , Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case REQUEST_CODE_ASK_CONTACTS_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    refreshFindersList();
                } else {
                    // Permission Denied
                    Toast.makeText( this,"your message" , Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @SuppressLint("MissingPermission")
    public void StartLocationServices()
    {
        LocationListener trackerListener = new TrackerListener(this);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, trackerListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, trackerListener);

        Intent mIntent = new Intent(this, UpdateLocationService.class);
        UpdateLocationService.enqueueWork(this, mIntent);
    }



}