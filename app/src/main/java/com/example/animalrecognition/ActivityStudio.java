package com.example.animalrecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.custom.FirebaseCustomLocalModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.annotation.Nullable;

public class ActivityStudio extends AppCompatActivity {

    private static FirebaseAuth auth;
    private static FirebaseFirestore fStore;
    private StorageReference refImageLists;
    private ImageView picture;
    private Button buttonHome, buttonGo, buttonCamera, buttonUpload, buttonYes, buttonNo;
    private LinearLayout layoutOptions;
    private TextView headerAccuracy;
    private static int takeImageCode = 10001;
    private static int uploadImageCode = 10002;
    private String uId;

    private Bitmap currentImage;

    private int size = 100;
    private int depth = 3;
    private int output = 64;
    private String[] labels =
            {
                    "a butterfly",
                    "a cat",
                    "a chicken",
                    "a cow",
                    "a dog",
                    "an elephant",
                    "a horse",
                    "a sheep",
                    "a spider",
                    "a squirrel"
            };

    // Uploads images to be stored in the firebase backend
    private void handleUpload(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayStream);

        final StorageReference ref = refImageLists.child("image_"+bitmap.hashCode());

        ref.putBytes(byteArrayStream.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //getDownloadURL(reference);
                    }
                });
    }

    // Shows or hides (tied to display boolean) the little accuracy question below an image
    private void toggleImageOptions(boolean display, String animalName) {
        if (display == true) {
            buttonGo.setVisibility(View.GONE);

            headerAccuracy.setText(String.format("Is this %s?", animalName));
            headerAccuracy.setVisibility(View.VISIBLE);
            layoutOptions.setVisibility(View.VISIBLE);
        } else {
            buttonGo.setVisibility(View.VISIBLE);

            headerAccuracy.setVisibility(View.GONE);
            layoutOptions.setVisibility(View.GONE);
        }
    }

    private void startHome() {
        Intent intHome = new Intent(ActivityStudio.this, ActivityHome.class);
        startActivity(intHome);

        finish();
    }

    // Opens camera widget
    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, takeImageCode);
        }
    }

    // Opens gallery widget
    private void getPictureFromGallery() {
        Intent intGallery = new Intent();

        intGallery.setType("image/*");
        intGallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intGallery, "Select Picture"), uploadImageCode);
    }

    // The following handles processing of all image submission (camera or gallery)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == takeImageCode) {
            switch (resultCode) {
                case RESULT_OK:
                    Toast.makeText(ActivityStudio.this, "Uploading from camera", Toast.LENGTH_SHORT).show();

                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    handleUpload(bitmap);

                    picture.setImageBitmap(bitmap);
                    currentImage = bitmap;
            }
        } else if (requestCode == uploadImageCode) {
            switch (resultCode) {
                case RESULT_OK:
                    Toast.makeText(ActivityStudio.this, "Uploading from gallery", Toast.LENGTH_SHORT).show();

                    Uri selectedImage = data.getData();

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                        handleUpload(bitmap);

                        picture.setImageBitmap(bitmap);
                        currentImage = bitmap;
                    } catch (IOException e) {
                        Toast.makeText(ActivityStudio.this, "Upload failed", Toast.LENGTH_SHORT).show();
                    }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_studio);

        auth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        uId = auth.getCurrentUser().getUid();
        refImageLists = FirebaseStorage.getInstance().getReference().child("ImageLists").child(uId);
        picture = findViewById(R.id.picture);
        buttonHome = findViewById(R.id.buttonHome);
        buttonGo = findViewById(R.id.buttonGo);
        buttonCamera = findViewById(R.id.buttonCamera);
        buttonUpload = findViewById(R.id.buttonUpload);
        buttonYes = findViewById(R.id.buttonYes);
        buttonNo = findViewById(R.id.buttonNo);
        layoutOptions = findViewById(R.id.layoutOptions);
        headerAccuracy = findViewById(R.id.headerAccuracy);

        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startHome();
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
        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleImageOptions(false, "");
                Toast.makeText(ActivityStudio.this, "Hooray!", Toast.LENGTH_SHORT).show();

                DocumentReference documentReferenceInfo = fStore.collection("Users").document(uId);
                documentReferenceInfo.update("correctGuesses", FieldValue.increment(1));

                picture.setImageResource(R.drawable.ic_gallery);
                currentImage = null;
            }
        });
        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleImageOptions(false, "");
                Toast.makeText(ActivityStudio.this, "Feedback Noted", Toast.LENGTH_SHORT).show();

                DocumentReference documentReferenceInfo = fStore.collection("Users").document(uId);
                documentReferenceInfo.update("incorrectGuesses", FieldValue.increment(1));

                picture.setImageResource(R.drawable.ic_gallery);
                currentImage = null;
            }
        });

        buttonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start recognizing the image
                if (currentImage != null) {
                    FirebaseCustomLocalModel model = new FirebaseCustomLocalModel.Builder()
                            .setAssetFilePath("AniRec_Model2.tflite")
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

                    Bitmap bitmap = Bitmap.createScaledBitmap(currentImage, size, size, true);

                    // Normalize channel values to [-1.0, 1.0]
                    int batchNum = 0;
                    float[][][][] input = new float[1][size][size][depth];
                    for (int x = 0; x < size; x++) {
                        for (int y = 0; y < size; y++) {
                            int pixel = bitmap.getPixel(x, y);
                            input[batchNum][x][y][0] = (Color.red(pixel) - 127) / 128.0f;
                            input[batchNum][x][y][1] = (Color.green(pixel) - 127) / 128.0f;
                            input[batchNum][x][y][2] = (Color.blue(pixel) - 127) / 128.0f;
                        }
                    }

                    FirebaseModelInputs inputs = null;
                    try {
                        inputs = new FirebaseModelInputs.Builder()
                                .add(input)
                                .build();
                    } catch (FirebaseMLException e) {
                        Log.i("MLKit", e.getMessage());
                    }

                    // Actually interpret the image and show the result to the user
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

                                                // Pick out the prediction with the highest weighting
                                                if (probabilities[i] > highestProbability) {
                                                    likeliest = labels[i % labels.length];
                                                    highestProbability = probabilities[i];
                                                }
                                            }

                                            toggleImageOptions(true, likeliest);
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.i("MLKit", e.getMessage());
                                        }
                                    });
                } else {
                    Toast.makeText(ActivityStudio.this, "Please upload or capture photo first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}