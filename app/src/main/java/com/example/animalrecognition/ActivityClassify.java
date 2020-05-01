package com.example.animalrecognition;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ActivityClassify extends AppCompatActivity {

    // Unused, but unsafe to delete

    private void startStudio() {
        Intent intStudio = new Intent(ActivityClassify.this, ActivityStudio.class);
        startActivity(intStudio);

        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_statistics);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        Button buttonHome = findViewById(R.id.buttonHome);

        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStudio();
            }
        });
    }
}