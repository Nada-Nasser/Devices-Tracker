package com.example.devicetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
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

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Map;

public class MyTrackers extends AppCompatActivity
{
    ArrayList<User> trackersList;
    MyTrackersCustomAdapter myTrackersCustomAdapter;

    ListView trackersListView;
    GlobalInfo globalInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_trackers);

        trackersListView = findViewById(R.id.contacts_listView);

        globalInfo = new GlobalInfo(this);

        trackersList = new ArrayList<>();

        myTrackersCustomAdapter = new MyTrackersCustomAdapter(trackersList);

        trackersListView.setAdapter(myTrackersCustomAdapter);

        refreshMyTrackersList();

        trackersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l)
            {
                try {
                    User deletedContact = trackersList.get(pos);
                    globalInfo.myTrackers.remove(deletedContact.getPhoneNumber());

                    String path = "users/" + deletedContact.getPhoneNumber() + "/Finders";

                    FirebaseDatabase.getInstance().getReference(path)
                            .child(globalInfo.getPhoneNumber()).removeValue();
                    refreshMyTrackersList();

                    globalInfo.saveData();
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_contact_list,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.add:
                addContact();
                return true;
            case R.id.goback:
                Intent intent = new Intent(this,MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addContact()
    {
        if ( Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) !=
                    PackageManager.PERMISSION_GRANTED  ){
                requestPermissions(new String[]{
                                android.Manifest.permission.READ_CONTACTS},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return ;
            }
        }

        PickContact();// init the contact list
    }

    //get acces to location permission
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PickContact();// init the contact list
                } else {
                    // Permission Denied
                    Toast.makeText( this,"permission denied" , Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    // pick phone number

    void PickContact(){
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT_REQUEST_CODE);
    }

    // Declare
    static final int PICK_CONTACT_REQUEST_CODE = 1;
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode)
        {
            case (PICK_CONTACT_REQUEST_CODE) :
                if (resultCode == Activity.RESULT_OK)
                {
                    Uri contactData = data.getData();
                    Cursor c =  getContentResolver().query(contactData, null, null, null, null);

                    if (c.moveToFirst())
                    {
                        String id       = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                        String cNumber="No number";
                        if (hasPhone.equalsIgnoreCase("1"))
                        {
                            Cursor phones = getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,
                                    null, null);

                            phones.moveToFirst();
                            cNumber =globalInfo.FormatPhoneNumber (phones.getString(phones.getColumnIndex("data1")));
                            System.out.println("number is:"+cNumber);
                        }

                        // Update firebase
                        globalInfo.myTrackers.put(cNumber,name);
                        FirebaseDatabase.getInstance().getReference("users")
                                .child(cNumber).child("Finders").child(globalInfo.getPhoneNumber())
                                .setValue(true);


                        //update list
                        refreshMyTrackersList();
                        globalInfo.saveData();
                        //update database
                    }
                    c.close();
                }
                break;
        }
    }

    private void refreshMyTrackersList()
    {
        trackersList.clear();

        for (Map.Entry tracker : globalInfo.myTrackers.entrySet())
        {
            trackersList.add(new User(tracker.getKey().toString(),tracker.getValue().toString()));
        }

        myTrackersCustomAdapter.notifyDataSetChanged();
    }


    class MyTrackersCustomAdapter extends BaseAdapter
    {
        ArrayList <User> contactsList;

        public MyTrackersCustomAdapter(ArrayList<User> contactsList) {
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
            LayoutInflater inflater = getLayoutInflater();
            View contactsView = inflater.inflate(R.layout.single_row_conact , null);

            TextView contactNameTextView = contactsView.findViewById(R.id.contact_username);
            TextView contactPhoneNumber = contactsView.findViewById(R.id.contact_phone_number);

            final User contactItem = contactsList.get(i);

            contactNameTextView.setText(contactItem.getName());
            contactPhoneNumber.setText(contactItem.getPhoneNumber());

            return contactsView;
        }
    }
}