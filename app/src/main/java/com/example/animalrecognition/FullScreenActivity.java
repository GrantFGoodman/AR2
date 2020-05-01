package com.example.animalrecognition;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class FullScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);

        ImageView imageView = (ImageView) findViewById(R.id.image_view);

        //getSupportActionBar().hide();
        Objects.requireNonNull(getSupportActionBar()).setTitle("Full Screen Image");

        Intent i = getIntent();

        int position = Objects.requireNonNull(i.getExtras()).getInt("id");

        com.example.animalrecognition.ImageAdapter imageAdapter = new com.example.animalrecognition.ImageAdapter(this);

        imageView.setImageResource(imageAdapter.imageArray[position]);
    }
}
