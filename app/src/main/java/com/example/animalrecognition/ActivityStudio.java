package com.example.animalrecognition;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ActivityStudio extends AppCompatActivity {

    private static int RESULT_LOAD_IMAGE = 1;
    static FirebaseAuth auth;
    private Button buttonHome, buttonGo, buttonCamera, buttonUpload;

    private void startHome() {
        Intent intHome = new Intent(ActivityStudio.this, ActivityHome.class);
        startActivity(intHome);

        finish();
    }

    private void startClassify() {
        Intent intClassify = new Intent(ActivityStudio.this, ActivityClassify.class);
        startActivity(intClassify);

        finish();
    }

    private void takePicture() {

    }

    private void getPictureFromGallery() {
        Intent intGallery = new Intent();

        intGallery.setType("image/*");
        intGallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intGallery, "Select Picture"), RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_studio);

        auth = FirebaseAuth.getInstance();
        buttonHome = findViewById(R.id.buttonHome);
        buttonGo = findViewById(R.id.buttonGo);
        buttonCamera = findViewById(R.id.buttonCamera);
        buttonUpload = findViewById(R.id.buttonUpload);

        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startHome();
            }
        });
        buttonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startClassify();
            }
        });
        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPictureFromGallery();
            }
        });
    }
}