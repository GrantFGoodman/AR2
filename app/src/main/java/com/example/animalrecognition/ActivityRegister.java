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
    private EditText entryName, entryId, entryEmail, entryPassword;
    private Button buttonSignup;
    private TextView textButtonSignup;
    private String name, email, id, password;

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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        entryName = findViewById(R.id.editTextName);
        entryId = findViewById(R.id.editTextID);
        entryEmail = findViewById(R.id.editTextEmail);
        entryPassword = findViewById(R.id.editTextPassword);
        textButtonSignup = findViewById(R.id.textViewToLogin);
        buttonSignup = findViewById(R.id.buttonSignUp);

        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                // Converts the contents of the entries into "clean" strings with no leading or trailing spaces
                name = entryName.getText().toString().trim();
                email = entryEmail.getText().toString().trim();
                id = entryId.getText().toString().trim();
                password = entryPassword.getText().toString().trim();

                if (name.isEmpty()) {
                    entryName.setError("Please enter your name");
                    entryName.requestFocus();
                }
                else if (id.isEmpty()) {
                    entryId.setError("Please enter an ID");
                    entryId.requestFocus();
                }
                else if (email.isEmpty()) {
                    entryEmail.setError("Please enter an email");
                    entryEmail.requestFocus();
                }
                else if (password.isEmpty()) {
                    entryPassword.setError("Please enter a password");
                    entryPassword.requestFocus();
                }
                else {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(ActivityRegister.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete (@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Create and setup new user
                                User user = new User(name, id, email);

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

        textButtonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLogin();
            }
        });

    }
}


