package com.example.animalrecognition;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ActivityRegister extends AppCompatActivity
{
    public static final String TAG = "TAG";
    private FirebaseAuth auth;
    private FirebaseFirestore fStore;
    private EditText entryName, entryUserId, entryUserEmail, entryUserPassword;
    private Button buttonRegister;
    private TextView textButtonLogin;
    private String userName, email, userUid,  userId, password, professionDefault;

    private void startHome() {
        Intent intHome = new Intent(ActivityRegister.this, ActivityHome.class);
        startActivity(intHome);

        finish();
    }

    private void startLogin() {
        Intent intLogin = new Intent(ActivityRegister.this, ActivityLogin.class);
        startActivity(intLogin);

        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        entryName = findViewById(R.id.entryUserName);
        entryUserId = findViewById(R.id.entryUserId);
        entryUserEmail = findViewById(R.id.entryUserEmail);
        entryUserPassword = findViewById(R.id.entryUserPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textButtonLogin = findViewById(R.id.buttonLogin);
        professionDefault = "Animal Photographer";

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                // Converts the contents of the entries into "clean" strings with no leading or trailing spaces
                userName = entryName.getText().toString().trim();
                email = entryUserEmail.getText().toString().trim();
                userId = entryUserId.getText().toString().trim();
                password = entryUserPassword.getText().toString().trim();

                // Sanitize inputs to make sure they meet requirements before allowing registration
                // (Firebase checks these on the server end as well)
                if (userName.isEmpty()) {
                    entryName.setError("Please enter your name");
                    entryName.requestFocus();
                } else if (userName.length() < 2) {
                    entryName.setError("Name must be at least 2 characters");
                    entryName.requestFocus();
                }
                else if (email.isEmpty()) {
                    entryUserEmail.setError("Please enter your email");
                    entryUserEmail.requestFocus();
                }
                else if (userId.isEmpty()) {
                    entryUserId.setError("Please enter your student ID");
                    entryUserId.requestFocus();
                }
                else if (userId.length() != 10) {
                    entryUserId.setError("Student ID must be 10 characters");
                    entryUserId.requestFocus();
                }
                else if (password.isEmpty()) {
                    entryUserPassword.setError("Please enter your password");
                    entryUserPassword.requestFocus();
                }
                else {
                    // Actual user registration located here
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(ActivityRegister.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete (@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ActivityRegister.this, "Registration Successful", Toast.LENGTH_SHORT).show();

                                // Create and setup new user
                                User user = new User(userName, email, userId, professionDefault);

                                FirebaseDatabase.getInstance().getReference("Users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user);

                                userUid = auth.getCurrentUser().getUid();
                                DocumentReference documentReference = fStore.collection("Users").document(userUid);
                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("userName", userName);
                                userMap.put("email", email);
                                userMap.put("userId", userId);
                                userMap.put("profession", professionDefault);
                                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "User profile created for " + email);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "Failure to create profile " + e.toString());
                                    }
                                });

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
    }
}


