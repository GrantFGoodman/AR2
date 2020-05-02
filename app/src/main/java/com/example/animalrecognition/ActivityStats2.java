package com.example.animalrecognition;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ActivityStats2 extends AppCompatActivity {

    private void startStats() {
        Intent intStats = new Intent(ActivityStats2.this, ActivityStats.class);
        startActivity(intStats);

        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_statistics2);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        Button buttonBack = findViewById(R.id.buttonBack);

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStats();
            }
        });
    }
}