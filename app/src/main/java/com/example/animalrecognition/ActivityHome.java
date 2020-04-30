package com.example.animalrecognition;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class ActivityHome extends AppCompatActivity
{
    static FirebaseAuth auth;
    private Button buttonClassify, buttonProfile, buttonStats;
    private TextView textButtonLogout;

    private void startProfile() {
        Intent intProfile = new Intent(ActivityHome.this, ActivityProfile.class);
        startActivity(intProfile);

        finish();
    }

    /*
    private void startGallery() {
        Intent intGallery = new Intent(ActivityHome.this, ActivityGallery.class);
        startActivity(intGallery);

        finish();
    }
    */

    private void startClassify() {
        Intent intClassify = new Intent(ActivityHome.this, ActivityClassify.class);
        startActivity(intClassify);

        finish();
    }

    private void startStats() {
        Intent intStats = new Intent(ActivityHome.this, ActivityStats.class);
        startActivity(intStats);

        finish();
    }

    private void startLogin() {
        Intent intLogin = new Intent(ActivityHome.this, ActivityLogin.class);
        startActivity(intLogin);

        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_home);

        auth = FirebaseAuth.getInstance();
        buttonClassify = findViewById(R.id.buttonClassify);
        buttonProfile = findViewById(R.id.buttonProfile);
        buttonStats = findViewById(R.id.buttonStats);
        textButtonLogout = findViewById(R.id.buttonLogout);

        textButtonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                Toast.makeText(ActivityHome.this, "Successfully logged out", Toast.LENGTH_SHORT).show();

                startLogin();
            }
        });

        buttonClassify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startClassify();
            }
        });

        buttonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProfile();
            }
        });

        buttonStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStats();
            }
        });
    }
}
