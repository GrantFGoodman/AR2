package com.example.animalrecognition;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ActivityProfile extends AppCompatActivity {

    static FirebaseAuth auth;
    static FirebaseUser user;
    private Button buttonHome;
    private TextView nameHeader, emailHeader;

    private void startHome() {
        Intent intHome = new Intent(ActivityProfile.this, ActivityHome.class);
        startActivity(intHome);

        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        buttonHome = findViewById(R.id.buttonHome);
        nameHeader = findViewById(R.id.nameHeader);
        emailHeader = findViewById(R.id.emailHeader);

        nameHeader.setText("Name: " + user.getDisplayName());
        emailHeader.setText("Email: " + user.getEmail());

        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startHome();
            }
        });
    }
}