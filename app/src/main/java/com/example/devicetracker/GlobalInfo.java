package com.example.devicetracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GlobalInfo
{
    private static String phoneNumber = "";

    public static String getPhoneNumber() {
        return FormatPhoneNumber(phoneNumber);
    }

    public static Map<String,String> myTrackers = new HashMap<>(); // key = phoneNumber , value = contact Name

    private SharedPreferences sharedPreferences;
    private Context context;

    public GlobalInfo(Context context)
    {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("TrackerGlobalInfo" , Context.MODE_PRIVATE);
    }

    public void saveData()
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("phoneNumber" , FormatPhoneNumber(phoneNumber));
        String TrackersList = HashMapToStringConverter(myTrackers);
        editor.putString("TrackersList" , TrackersList);
        editor.commit();
    }

    public boolean loadData()
    {
        myTrackers.clear();

        String TrackersList= sharedPreferences.getString("TrackersList" , "none");
        Log.i("TAG", "loadData: TrackersList = " + TrackersList);
        if(!TrackersList.equalsIgnoreCase("none"))
        {
            String[] contactsArray = TrackersList.split("%");

            for (int i = 0 ; i < contactsArray.length ; i+=2)
            {
                if(contactsArray[i].length()>0 && contactsArray[i+1].length()>0)
                {
                    String text = contactsArray[i] + "%" + contactsArray[i + 1];
                    Log.i("TAG", "loadData: " + text);

                    String number = contactsArray[i];
                    String name = contactsArray[i + 1];

                    myTrackers.put(FormatPhoneNumber(number), name);
                }
            }
        }

        phoneNumber = sharedPreferences.getString("phoneNumber" , "none");
        if (phoneNumber.equalsIgnoreCase("none"))
        {
            Intent loginIntent = new Intent(context,Login.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(loginIntent);
            return false;
        }

        return true;
    }

    private String HashMapToStringConverter(Map<String, String> hashMap)
    {
        String result  = "";

        for(Map.Entry entry : hashMap.entrySet())
        {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();

            result += key + "%" + value + "%";
        }

        Log.w("debugging", "HashMapToStringConverter: "+result);

        return result;
    }

    public static void updateInfo(String phone)
    {
        FormatPhoneNumber(phone);
        phoneNumber = phone;
        DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        Date date = new Date();

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(phoneNumber).child("updates").setValue(dateFormat.format(date));
    }

    public static void updateUserInfo(String phone)
    {
        FormatPhoneNumber(phone);

        DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        Date date = new Date();

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(phone).child("updates").setValue(dateFormat.format(date));
    }



    //format phone number
    public static String FormatPhoneNumber(String Oldnmber)
    {
        try{
            String numberOnly= Oldnmber.replaceAll("[^0-9]", "");
            if(Oldnmber.charAt(0)=='+') numberOnly="+" +numberOnly ;
            if (numberOnly.length()>=10)
                numberOnly=numberOnly.substring(numberOnly.length()-10);
            return(numberOnly);
        }
        catch (Exception ex){
            return(" ");
        }
    }
}
