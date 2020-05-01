/*
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

// Borrows code implementation from https://firebase.google.com/docs/ml-kit/android/use-custom-models

public final class LearningModel {

    static int size = 100;
    static int depth = 3;
    static int output = 64;
    static String[] labels =
            {
                    "Sheep",
                    "Dog",
                    "Elephant",
                    "Cat",
                    "Spider",
                    "Squirrel",
                    "Chicken",
                    "Butterfly",
                    "Cow",
                    "Horse"
            };

    public final static String Interpret(Bitmap bitmap) {

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
                        .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, size, size, depth})
                        .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, output})
                        .build();
            } catch (FirebaseMLException e) {
                Log.i("MLKit", e.getMessage());
            }
        }

        bitmap = Bitmap.createScaledBitmap(bitmap, size, size, true);

        int batchNum = 0;
        float[][][][] input = new float[1][size][size][depth];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
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

                                float highestProbability = 0;
                                String likeliest = "";

                                for (int i = 0; i < probabilities.length; i++) {
                                    Log.i("MLKit", String.format("%s: %1.4f", labels[i % labels.length], probabilities[i]));

                                    if (probabilities[i] > highestProbability) {
                                        likeliest = labels[i % labels.length];
                                        highestProbability = probabilities[i];
                                    }
                                }

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("MLKit", e.getMessage());
                            }
                        });

        return "no";
    }
}
*/