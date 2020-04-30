package com.example.animalrecognition;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ActivityRegister extends AppCompatActivity
{
    public static final String TAG = "TAG";
    private FirebaseAuth auth;
    private FirebaseFirestore fStore;
    private EditText entryName, entryUserId, entryUserEmail, entryUserPassword;
    private Button buttonRegister;
    private ImageView profilePicture;
    private TextView textButtonLogin, textButtonForgotPassword;
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

    private void sendPasswordResetEmail() {
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
        textButtonForgotPassword = findViewById(R.id.buttonForgotPassword);

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

                if (userName.isEmpty()) {
                    entryName.setError("Please enter your username");
                    entryName.requestFocus();
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
                                User user = new User(userName, email, userId, professionDefault);

                                // This does not correctly set the DisplayName property to the name picked out during registration (stored under name)
                                FirebaseDatabase.getInstance().getReference("Users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user);

                                Toast.makeText(ActivityRegister.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                startHome();


                                userUid = auth.getCurrentUser().getUid();
                                DocumentReference documentReference = fStore.collection("Users").document(userUid);
                                Map<String,Object> userMap = new HashMap<>();
                                userMap.put("userName",userName);
                                userMap.put("email",email);
                                userMap.put("userId",userId);
                                userMap.put("profession",professionDefault);
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


