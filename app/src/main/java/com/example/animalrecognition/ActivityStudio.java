package com.example.animalrecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

public class ActivityStudio extends AppCompatActivity {

    private static FirebaseAuth auth;
    private StorageReference refImageLists;
    private GridView gridView;
    private Button buttonHome, buttonGo, buttonCamera, buttonUpload;
    private static int takeImageCode = 10001;
    private static int uploadImageCode = 10002;
    private String uId;

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

    private void startClassify() {
        Intent intClassify = new Intent(ActivityStudio.this, ActivityClassify.class);
        startActivity(intClassify);

        finish();
    }

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
                    //profilePicture.setImageBitmap(bitmap);
                    handleUpload(bitmap);
            }
        } else if (requestCode == uploadImageCode) {
            switch (resultCode) {
                case RESULT_OK:
                    Toast.makeText(ActivityStudio.this, "Uploading from gallery", Toast.LENGTH_SHORT).show();

                    Uri selectedImage = data.getData();

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                        handleUpload(bitmap);
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
        gridView = findViewById(R.id.gridView);

        gridView.setAdapter(new com.example.animalrecognition.ImageAdapter(this));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(ActivityStudio.this,"Clicked image", Toast.LENGTH_SHORT).show();
            }
         });

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

        final File test = ImageList.imagesDirectory;

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