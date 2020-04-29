package com.example.animalrecognition;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ActivityHome extends AppCompatActivity
{
    static FirebaseAuth auth;
    private ImageButton buttonProfile;
    private TextView textButtonLogout;

    private void startProfile() {
        Intent intProfile = new Intent(ActivityHome.this, ActivityProfile.class);
        startActivity(intProfile);

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
        buttonProfile = findViewById(R.id.buttonProfile);
        textButtonLogout = findViewById(R.id.buttonLogout);

        textButtonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                Toast.makeText(ActivityHome.this, "Successfully logged out", Toast.LENGTH_SHORT).show();

                startLogin();
            }
        });

        buttonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProfile();
            }
        });
    }
}
