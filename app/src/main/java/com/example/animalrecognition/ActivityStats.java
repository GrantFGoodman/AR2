package com.example.animalrecognition;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Objects;

public class ActivityStats extends AppCompatActivity {

    private TextView headerCorrect, headerIncorrect, headerAccuracy;

    private void startHome() {
        Intent intHome = new Intent(ActivityStats.this, ActivityHome.class);
        startActivity(intHome);

        finish();
    }

    private void startSecondPage() {
        Intent intStats = new Intent(ActivityStats.this, ActivityStats2.class);
        startActivity(intStats);

        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_statistics);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        String uId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        Button buttonHome = findViewById(R.id.buttonHome);
        Button buttonPage = findViewById(R.id.buttonPage);
        headerCorrect = findViewById(R.id.headerCorrect);
        headerIncorrect = findViewById(R.id.headerIncorrect);
        headerAccuracy = findViewById(R.id.headerAccuracy);

        // Propagate the edit name and profession fields with the stuff saved in the database
        DocumentReference documentReferenceInfo = fStore.collection("Users").document(uId);
        documentReferenceInfo.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @SuppressLint({"DefaultLocale", "SetTextI18n"})
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                // All this tomfoolery gives us the guesses in a readable format (or 0 if it's null on the server)
                Double correctRaw = documentSnapshot.getDouble("correctGuesses");
                Double incorrectRaw = documentSnapshot.getDouble("incorrectGuesses");
                int correctGuesses = (correctRaw != null) ? correctRaw.intValue() : 0;
                int incorrectGuesses = (incorrectRaw != null) ? incorrectRaw.intValue() : 0;

                headerCorrect.setText("Correct Guesses: " + correctGuesses);
                headerIncorrect.setText("Incorrect Guesses: " + incorrectGuesses);

                if (correctGuesses > 0 && incorrectGuesses > 0) {
                    headerAccuracy.setText(String.format("Reported Accuracy: %.2f", ((double) correctGuesses / (correctGuesses + incorrectGuesses)) * 100) + "%");
                }
            }
        });

        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startHome();
            }
        });
        buttonPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSecondPage();
            }
        });
    }
}