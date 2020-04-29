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
import com.google.firebase.auth.UserProfileChangeRequest;

public class ActivityProfile extends AppCompatActivity {

    static FirebaseAuth auth;
    static FirebaseUser user;
    private Button buttonHome, buttonApply;
    private TextView emailHeader;
    private EditText entryUserName, entryUserProfession;

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
        buttonApply = findViewById(R.id.buttonApply);
        emailHeader = findViewById(R.id.emailHeader);
        entryUserName = findViewById(R.id.entryUserName);
        entryUserProfession = findViewById(R.id.entryUserProfession);

        entryUserName.setText(user.getDisplayName());
        emailHeader.setText(user.getEmail());

        buttonApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(entryUserName.getText().toString().trim()).build();
                user.updateProfile(profileUpdates);
            }
        });

        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startHome();
            }
        });

    }
}