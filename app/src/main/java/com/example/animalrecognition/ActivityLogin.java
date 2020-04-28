package com.example.animalrecognition;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ActivityLogin extends AppCompatActivity {

    static FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private EditText entryEmail, entryPassword;
    private Button buttonLogin;
    private TextView textButtonRegister;
    private String email, password;

    private void startHome()
    {
        Intent intHome = new Intent(ActivityLogin.this, ActivityHome.class);
        startActivity(intHome);

        finish();
    }

    private void startRegistration()
    {
        Intent intRegister = new Intent(ActivityLogin.this, ActivityRegister.class);
        startActivity(intRegister);

        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        entryEmail = findViewById(R.id.editTextEmail);
        entryPassword = findViewById(R.id.editTextPassword);
        textButtonRegister = findViewById(R.id.textViewToRegister);
        buttonLogin = findViewById(R.id.buttonLogin);

        authListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = auth.getCurrentUser();

                if (user != null) {
                    startHome();
                }
                /* Pointing this out to the user seems redundant
                else {
                    Toast.makeText(ActivityLogin.this, "You are not already logged in.", Toast.LENGTH_SHORT).show();
                }*/
            }
        };

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Converts the contents of the entries into "clean" strings with no leading or trailing spaces
                email = entryEmail.getText().toString().trim();
                password = entryPassword.getText().toString().trim();

                if (email.isEmpty()) {
                  entryEmail.setError("Please enter an email");
                  entryEmail.requestFocus();
                } else if (password.isEmpty()) {
                    entryPassword.setError("Please enter a password");
                    entryPassword.requestFocus();
                } else {
                    // Attempt to actually log the user in
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(ActivityLogin.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful()) {
                                startHome();
                            }
                            else {
                                Toast.makeText(ActivityLogin.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

         textButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRegistration();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }
}