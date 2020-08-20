package com.example.devicetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.common.internal.GmsLogger;

public class Login extends AppCompatActivity
{
    EditText phoneNumberEditText;
    GlobalInfo globalInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        phoneNumberEditText = findViewById(R.id.EDPhoneNumber);
    }

    public void BuNext(View view)
    {
        String phone = phoneNumberEditText.getText().toString();
        String phoneNumber = GlobalInfo.FormatPhoneNumber(phone);

        globalInfo = new GlobalInfo(this);
        globalInfo.updateInfo(phoneNumber);
        globalInfo.saveData();

        Intent trackersIntent = new Intent(this, MyTrackers.class);
        startActivity(trackersIntent);
        finish();
    }
}