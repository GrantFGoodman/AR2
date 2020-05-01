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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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
    private StorageReference refImageLists;
    private ImageView picture;
    private Button buttonHome, buttonGo, buttonCamera, buttonUpload;
    private static int takeImageCode = 10001;
    private static int uploadImageCode = 10002;
    private String uId;

    private Bitmap currentImage;

    private int size = 100;
    private int depth = 3;
    private int output = 64;
    private String[] labels =
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

    private void startHome() {
        Intent intHome = new Intent(ActivityStudio.this, ActivityHome.class);
        startActivity(intHome);

        finish();
    }

    /*
    private void startClassify() {
        Intent intClassify = new Intent(ActivityStudio.this, ActivityClassify.class);
        startActivity(intClassify);

        finish();
    }
     */

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, takeImageCode);
        }
    }

    private void getPictureFromGallery() {
        Intent intGallery = new Intent();

        intGallery.setType("image/*");
        intGallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intGallery, "Select Picture"), uploadImageCode);
    }

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

        uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        refImageLists = FirebaseStorage.getInstance().getReference().child("ImageLists").child(uId);
        auth = FirebaseAuth.getInstance();
        buttonHome = findViewById(R.id.buttonHome);
        buttonGo = findViewById(R.id.buttonGo);
        buttonCamera = findViewById(R.id.buttonCamera);
        buttonUpload = findViewById(R.id.buttonUpload);
        picture = findViewById(R.id.picture);

        /*
        gridView.setAdapter(new com.example.animalrecognition.ImageAdapter(this));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(ActivityStudio.this,"Clicked image", Toast.LENGTH_SHORT).show();
            }
         });
         */

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
        buttonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (currentImage != null) {
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

                    Bitmap bitmap = Bitmap.createScaledBitmap(currentImage, size, size, true);

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

                                            Toast.makeText(ActivityStudio.this, "Image contains a(n) " + likeliest, Toast.LENGTH_LONG).show();
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
                    Toast.makeText(ActivityStudio.this, "Please upload or capture photo", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //final File test = ImageList.imagesDirectory;

        /*
        islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(ActivityStudio.this,"Failed to download file", Toast.LENGTH_SHORT).show();
            }
        });

         */
    }
}