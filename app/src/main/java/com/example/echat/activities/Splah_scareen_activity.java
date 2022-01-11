package com.example.echat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.echat.R;
import com.google.firebase.auth.FirebaseAuth;

public class Splah_scareen_activity extends AppCompatActivity {
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splah_scareen);

        auth = FirebaseAuth.getInstance();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {


                if (auth.getCurrentUser() != null)
                {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }

                else if(auth.getCurrentUser()== null) {
                    startActivity(new Intent(getApplicationContext(), SigninActivity.class));

                    finish();
                }

            }
        }, 1000); //here you change the delay
    }
}