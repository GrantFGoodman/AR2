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

import java.util.Objects;

public class ActivityLogin extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private EditText entryEmail, entryPassword;
    private String email, password;

    private void startHome() {
        Intent intHome = new Intent(ActivityLogin.this, ActivityHome.class);
        startActivity(intHome);

        finish();
    }

    private void startRegistration() {
        Intent intRegister = new Intent(ActivityLogin.this, ActivityRegister.class);
        startActivity(intRegister);

        finish();
    }

    private void sendPasswordResetEmail() {
        final String resetEmail = entryEmail.getText().toString().trim();

        if (resetEmail.isEmpty()) {
            Toast.makeText(ActivityLogin.this, "Supply valid email to send password reset instructions.", Toast.LENGTH_SHORT).show();
        } else {
            FirebaseAuth.getInstance().sendPasswordResetEmail(resetEmail)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ActivityLogin.this, "Password reset instructions sent to " + resetEmail, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ActivityLogin.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        entryEmail = findViewById(R.id.entryUserEmail);
        entryPassword = findViewById(R.id.entryUserPassword);
        TextView textButtonRegister = findViewById(R.id.buttonRegister);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        TextView textButtonForgotPassword = findViewById(R.id.buttonForgotPassword);

        // Automatically skip to the home screen if we already have a login cookie
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = auth.getCurrentUser();

                if (user != null) {
                    startHome();
                }
            }
        };

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Converts the contents of the entries into "clean" strings with no leading or trailing spaces
                email = entryEmail.getText().toString().trim();
                password = entryPassword.getText().toString().trim();

                // Validate a few inputs
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
                                Toast.makeText(ActivityLogin.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                startHome();
                            }
                            else {
                                Toast.makeText(ActivityLogin.this, "Login Failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
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

        textButtonForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPasswordResetEmail();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }
}