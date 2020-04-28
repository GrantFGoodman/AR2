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
    EditText name_Text, ID_Text, email_Text, password_Text;
    Button buttonSignUp;
    TextView toSignIn;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        name_Text = findViewById(R.id.editTextName);
        ID_Text = findViewById(R.id.editTextID);
        email_Text = findViewById(R.id.editTextEmail);
        password_Text = findViewById(R.id.editTextPassword);

        toSignIn = findViewById(R.id.textViewToLogin);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        buttonSignUp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final String name = name_Text.getText().toString();
                final String ID = ID_Text.getText().toString();
                final String email = email_Text.getText().toString();
                String password = password_Text.getText().toString();
                if (name.isEmpty() && ID.isEmpty() && email.isEmpty() && password.isEmpty())
                {
                    Toast.makeText(ActivityRegister.this, "Fields are Empty!", Toast.LENGTH_SHORT).show();
                }
                else if (name.isEmpty())
                {
                    name_Text.setError("Please enter an email");
                    name_Text.requestFocus();
                }
                else if (ID.isEmpty())
                {
                    ID_Text.setError("Please enter a password");
                    ID_Text.requestFocus();
                }
                else if (email.isEmpty())
                {
                    email_Text.setError("Please enter an email");
                    email_Text.requestFocus();
                }
                else if (password.isEmpty())
                {
                    password_Text.setError("Please enter a password");
                    password_Text.requestFocus();
                }
                else if (!(name.isEmpty() && ID.isEmpty() &&  email.isEmpty() && password.isEmpty()))
                {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(ActivityRegister.this, new OnCompleteListener<AuthResult>(){
                        @Override
                        public void onComplete (@NonNull Task<AuthResult> task)
                        {
                            if (!task.isSuccessful()) {
                                Toast.makeText(ActivityRegister.this, "The email or ID provided are already registered.", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                User user = new User(name, ID, email);

                                FirebaseDatabase.getInstance().getReference("Users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user);
/*
                                FirebaseDatabase.getInstance().getReference("Users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                        }
                                    }
                                });*/
                                startActivity(new Intent(ActivityRegister.this, ActivityHome.class));

                                finish();
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(ActivityRegister.this, "Error Occurred!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        toSignIn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(ActivityRegister.this, ActivityLogin.class);
                startActivity(i);

                finish();
            }
        });

    }
}


