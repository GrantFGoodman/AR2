package com.example.animalrecognition;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.custom.FirebaseCustomLocalModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

// Borrows code implementation from https://firebase.google.com/docs/ml-kit/android/use-custom-models

public final class LearningModel {

    public static String Interpret(Bitmap bitmap, final InputStream labels) {

        FirebaseCustomLocalModel model = new FirebaseCustomLocalModel.Builder()
                .setAssetFilePath("recognitionModel.tflite")
                .build();

        FirebaseModelInterpreterOptions options = new FirebaseModelInterpreterOptions.Builder(model).build();
        FirebaseModelInterpreter interpreter = null;

        {
            try {
                interpreter = FirebaseModelInterpreter.getInstance(options);
            } catch (FirebaseMLException e) {
                Log.i("MLKit", e.getMessage());
            }
        }

        FirebaseModelInputOutputOptions inputOutputOptions = null;

        {
            try {
                inputOutputOptions = new FirebaseModelInputOutputOptions.Builder()
                        .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 100, 100, 3})
                        .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 64})
                        .build();
            } catch (FirebaseMLException e) {
                Log.i("MLKit", e.getMessage());
            }
        }

        bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true);

        int batchNum = 0;
        float[][][][] input = new float[1][100][100][3];
        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                int pixel = bitmap.getPixel(x, y);
                // Normalize channel values to [-1.0, 1.0]. This requirement varies by
                // model. For example, some models might require values to be normalized
                // to the range [0.0, 1.0] instead.
                input[batchNum][x][y][0] = (Color.red(pixel) - 127) / 128.0f;
                input[batchNum][x][y][1] = (Color.green(pixel) - 127) / 128.0f;
                input[batchNum][x][y][2] = (Color.blue(pixel) - 127) / 128.0f;
            }
        }

        FirebaseModelInputs inputs = null;
        try {
            Log.i("MLKit", input.toString());
            inputs = new FirebaseModelInputs.Builder()
                    .add(input)  // add() as many input arrays as your model requires
                    .build();
        } catch (FirebaseMLException e) {
            Log.i("MLKit", e.getMessage());
        }

        interpreter.run(inputs, inputOutputOptions)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseModelOutputs>() {
                            @Override
                            public void onSuccess(FirebaseModelOutputs result) {
                                float[][] output = result.getOutput(0);
                                float[] probabilities = output[0];

                                BufferedReader reader = new BufferedReader(new InputStreamReader(labels));

                                float highestProb = 0;
                                String likeliest = "";

                                for (int i = 0; i < probabilities.length; i++) {
                                    String label = null;
                                    try {
                                        label = reader.readLine();
                                    } catch (IOException e) {
                                        Log.i("MLKit", e.getMessage());
                                    }
                                    Log.i("MLKit", String.format("%s: %1.4f", label, probabilities[i]));

                                    if (probabilities[i] > highestProb) {
                                        highestProb = probabilities[i];
                                        likeliest = label;
                                    }
                                }
                                Log.i("MLKit", likeliest);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("MLKit", e.getMessage());
                            }
                        });

        return "Nothing";
    }
}
