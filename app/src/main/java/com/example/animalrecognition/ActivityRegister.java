package com.example.animalrecognition;
import android.content.Intent;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;


public class ActivityRegister extends AppCompatActivity
{
    private FirebaseAuth auth;
    private EditText entryUserName, entryUserId, entryUserEmail, entryUserPassword;
    private Button buttonRegister;
    private TextView textButtonLogin, textButtonForgotPassword;
    private String username, email, userId, password;

    private void startHome()
    {
        Intent intHome = new Intent(ActivityRegister.this, ActivityHome.class);
        startActivity(intHome);

        finish();
    }

    private void startLogin()
    {
        Intent intLogin = new Intent(ActivityRegister.this, ActivityLogin.class);
        startActivity(intLogin);

        finish();
    }

    private void sendPasswordResetEmail()
    {
        final String resetEmail = entryUserEmail.getText().toString().trim();

        if (resetEmail == null || resetEmail.isEmpty()) {
            Toast.makeText(ActivityRegister.this, "Supply valid email to send password reset instructions.", Toast.LENGTH_SHORT).show();
        } else {
            FirebaseAuth.getInstance().sendPasswordResetEmail(resetEmail)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ActivityRegister.this, "Password reset instructions sent to " + resetEmail, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ActivityRegister.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        entryUserName = findViewById(R.id.entryUserName);
        entryUserId = findViewById(R.id.entryUserId);
        entryUserEmail = findViewById(R.id.entryUserEmail);
        entryUserPassword = findViewById(R.id.entryUserPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textButtonLogin = findViewById(R.id.buttonLogin);
        textButtonForgotPassword = findViewById(R.id.buttonForgotPassword);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                // Converts the contents of the entries into "clean" strings with no leading or trailing spaces
                username = entryUserName.getText().toString().trim();
                email = entryUserEmail.getText().toString().trim();
                userId = entryUserId.getText().toString().trim();
                password = entryUserPassword.getText().toString().trim();

                if (username.isEmpty()) {
                    entryUserName.setError("Please enter your username");
                    entryUserName.requestFocus();
                }
                else if (userId.isEmpty()) {
                    entryUserId.setError("Please enter your student ID");
                    entryUserId.requestFocus();
                }
                else if (email.isEmpty()) {
                    entryUserEmail.setError("Please enter your email");
                    entryUserEmail.requestFocus();
                }
                else if (password.isEmpty()) {
                    entryUserPassword.setError("Please enter your password");
                    entryUserPassword.requestFocus();
                }
                else {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(ActivityRegister.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete (@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Create and setup new user
                                User user = new User(username, email, userId);

                                FirebaseDatabase.getInstance().getReference("Users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user);

                                startHome();
                            }
                            else {
                                Toast.makeText(ActivityRegister.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        textButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLogin();
            }
        });

        textButtonForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPasswordResetEmail();
            }
        });
    }
}


