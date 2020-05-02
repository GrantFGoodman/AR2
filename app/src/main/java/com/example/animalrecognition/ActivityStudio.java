package com.example.animalrecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import javax.annotation.Nullable;

public class ActivityStudio extends AppCompatActivity {

    private String uId;
    private FirebaseFirestore fStore;
    private StorageReference refImageLists;
    private ImageView picture;
    private Button buttonGo;
    private LinearLayout layoutOptions;
    private TextView headerAccuracy;
    private TextView headerCounter;
    private Bitmap currentImage;
    private int localCounter = 0;

    private static int takeImageCode = 10001;
    private static int uploadImageCode = 10002;

    // Parameters for the network itself taken from the tensorflow tool
    private final int size = 100;
    private final int output = 10;
    private final String modelName = "AniRec_Model2.tflite";
    private final String[] labels =
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

    private void startHome() {
        Intent intHome = new Intent(ActivityStudio.this, ActivityHome.class);
        startActivity(intHome);

        finish();
    }

    // Uploads images to be stored in the firebase backend
    private void handleUpload(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayStream);

        refImageLists.child("image_" + localCounter).putBytes(byteArrayStream.toByteArray())
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(ActivityStudio.this, "Image upload failure", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Shows or hides (tied to display boolean) the little accuracy question below an image
    private void toggleImageOptions(boolean display, String animalName) {
        if (display) {
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

    private void showError(String err) {
        Log.i("MLKit", err);
        Toast.makeText(ActivityStudio.this, err, Toast.LENGTH_LONG).show();
    }

    // Adds a new image to the database and displays it locally
    private void pushImage(Bitmap bitmap) {
        localCounter++;

        handleUpload(bitmap);
        picture.setImageBitmap(bitmap);
        currentImage = bitmap;

        DocumentReference ref = fStore.collection("Users").document(uId);
        ref.update("counter", FieldValue.increment(1));

        updateCounterHeader();
    }

    // Pushes the image counter back and displays the previous image
    // No need to remove popped images from the database since they'll be rewritten later
    private void popImage() {
        localCounter--;

        downloadAndSetLeadingImage();

        DocumentReference ref = fStore.collection("Users").document(uId);
        ref.update("counter", FieldValue.increment(-1));
        updateCounterHeader();
    }

    // Displays the latest image (based on localCounter) and preps the machine learning algorithm to accept it
    private void downloadAndSetLeadingImage() {
        picture.setImageResource(R.drawable.ic_gallery);
        currentImage = null;

        // Download the latest image
        if (localCounter > 0) {
            try {
                final File localFile = File.createTempFile("Image", "bmp");
                localFile.deleteOnExit();
                refImageLists.child("image_" + localCounter).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());

                        picture.setImageBitmap(bitmap);
                        currentImage = bitmap;
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ActivityStudio.this, "Image download failure", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                showError(Objects.requireNonNull(e.getMessage()));
            }
        }
    }

    private void updateCounterHeader() {
        headerCounter.setText(String.format("%s Image%s Left", localCounter, localCounter == 1 ? "" : "s"));
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

    // Handles processing of image submission (camera or gallery)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && resultCode == RESULT_OK) {
            if (requestCode == takeImageCode) {
                Toast.makeText(ActivityStudio.this, "Uploading from camera", Toast.LENGTH_SHORT).show();

                Bitmap bitmap = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                pushImage(bitmap);
            } else if (requestCode == uploadImageCode) {
                Toast.makeText(ActivityStudio.this, "Uploading from gallery", Toast.LENGTH_SHORT).show();

                Uri selectedImage = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    pushImage(bitmap);
                } catch (IOException e) {
                    showError(Objects.requireNonNull(e.getMessage()));
                }
            }
        } else {
            showError("An error occurred");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_studio);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        fStore = FirebaseFirestore.getInstance();
        uId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        refImageLists = FirebaseStorage.getInstance().getReference().child("ImageLists").child(uId);
        picture = findViewById(R.id.picture);
        layoutOptions = findViewById(R.id.layoutOptions);
        headerAccuracy = findViewById(R.id.headerAccuracy);
        headerCounter = findViewById(R.id.counter);
        buttonGo = findViewById(R.id.buttonGo);
        Button buttonHome = findViewById(R.id.buttonHome);
        Button buttonCamera = findViewById(R.id.buttonCamera);
        Button buttonUpload = findViewById(R.id.buttonUpload);
        Button buttonYes = findViewById(R.id.buttonYes);
        Button buttonNo = findViewById(R.id.buttonNo);

        // Propagate the counter field with the stuff saved in the database
        DocumentReference documentReferenceInfo = fStore.collection("Users").document(uId);
        Task<DocumentSnapshot> snapshot = documentReferenceInfo.get();
        snapshot.addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    // Get the counter in a readable format (or 0 if it's null on the server)
                    Double counterRaw = Objects.requireNonNull(task.getResult()).getDouble("counter");

                    localCounter = (counterRaw != null) ? counterRaw.intValue() : 0;
                    updateCounterHeader();

                    downloadAndSetLeadingImage();
                }
            }
        });

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

                DocumentReference ref = fStore.collection("Users").document(uId);
                ref.update("correctGuesses", FieldValue.increment(1));

                popImage();
            }
        });
        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleImageOptions(false, "");
                Toast.makeText(ActivityStudio.this, "Feedback Noted", Toast.LENGTH_SHORT).show();

                DocumentReference ref = fStore.collection("Users").document(uId);
                ref.update("incorrectGuesses", FieldValue.increment(1));

                popImage();
            }
        });

        buttonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start recognizing the image
                if (currentImage != null) {
                    FirebaseCustomLocalModel model = new FirebaseCustomLocalModel.Builder()
                            .setAssetFilePath(modelName)
                            .build();

                    FirebaseModelInterpreterOptions options = new FirebaseModelInterpreterOptions.Builder(model).build();
                    FirebaseModelInterpreter interpreter = null;

                    try {
                        interpreter = FirebaseModelInterpreter.getInstance(options);
                    } catch (FirebaseMLException e) {
                        showError(Objects.requireNonNull(e.getMessage()));
                    }

                    FirebaseModelInputOutputOptions inputOutputOptions = null;

                    try {
                        inputOutputOptions = new FirebaseModelInputOutputOptions.Builder()
                                .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, size, size, 3})
                                .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, output})
                                .build();
                    } catch (FirebaseMLException e) {
                        showError(Objects.requireNonNull(e.getMessage()));
                    }

                    Bitmap bitmap = Bitmap.createScaledBitmap(currentImage, size, size, true);

                    // Normalize channel values to [-1.0, 1.0]
                    int batchNum = 0;
                    float[][][][] input = new float[1][size][size][3];
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
                        showError(Objects.requireNonNull(e.getMessage()));
                    }

                    // Actually interpret the image and show the result to the user
                    assert inputOutputOptions != null;
                    assert inputs != null;
                    assert interpreter != null;
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
                                            showError(Objects.requireNonNull(e.getMessage()));
                                        }
                                    });
                } else {
                    Toast.makeText(ActivityStudio.this, "Please upload or capture photo first", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}