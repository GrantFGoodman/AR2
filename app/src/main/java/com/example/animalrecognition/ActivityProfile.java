package com.example.animalrecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class ActivityProfile extends AppCompatActivity {

    static FirebaseAuth auth;
    static FirebaseUser user;
    static FirebaseFirestore fStore;
    public static final String TAG = "TAG";
    static DatabaseReference databaseReference;
    private Button buttonHome, buttonApply;
    ImageView profilePicture;
    private TextView emailHeader;
    private EditText entryUserName, entryUserProfession;
    private String userUid, userName, profession;
    int takeImageCode = 10001;

    private void startHome() {
        Intent intHome = new Intent(ActivityProfile.this, ActivityHome.class);
        startActivity(intHome);

        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);

        auth = FirebaseAuth.getInstance();
        user = auth.getInstance().getCurrentUser();
        fStore = FirebaseFirestore.getInstance();

        buttonHome = findViewById(R.id.buttonHome);
        buttonApply = findViewById(R.id.buttonApply);
        emailHeader = findViewById(R.id.emailHeader);
        entryUserName = findViewById(R.id.entryUserName);
        entryUserProfession = findViewById(R.id.entryUserProfession);

        //entryUserName.setText(user.getDisplayName());
        emailHeader.setText(user.getEmail());

        userUid = auth.getCurrentUser().getUid();

        DocumentReference documentReferenceInfo = fStore.collection("Users").document(userUid);
        documentReferenceInfo.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                entryUserName.setText(documentSnapshot.getString("name"));
                entryUserProfession.setText(documentSnapshot.getString("profession"));
            }
        });


        buttonApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {/*
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(entryUserName.getText().toString().trim()).build();
                user.updateProfile(profileUpdates);*/

                userName = entryUserName.getText().toString().trim();
                profession = entryUserProfession.getText().toString().trim();

                DocumentReference documentReferenceUpdate = fStore.collection("Users").document(userUid);

                documentReferenceUpdate.update("name", userName, "profession", profession)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ActivityProfile.this, "Profile Update Successful", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ActivityProfile.this, "Profile Update Unsuccessful", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });


        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startHome();
            }
        });

    }

    public void handleImageClick(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, takeImageCode);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == takeImageCode) {
            switch (resultCode) {
                case RESULT_OK:
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    profilePicture=findViewById(R.id.profilePicture);
                    profilePicture.setImageBitmap(bitmap);
                    handleUpload(bitmap);
            }
        }
    }

    private void handleUpload(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        String uID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final  StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("ProfileImages")
                .child(uID + ".jpeg");

        reference.putBytes(baos.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        getDownloadURL(reference);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failure to upload profile picture " + e.toString());
                    }
                });
    }

    private void getDownloadURL(StorageReference reference) {
        reference.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d(TAG, "Profile picture URL retrieved " + uri);
                        setUserProfileURL(uri);
                    }
                });
    }

    private void setUserProfileURL(Uri uri){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();

        user.updateProfile(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ActivityProfile.this, "Profile picture successfully updated ", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ActivityProfile.this, "Profile picture update unsuccessful", Toast.LENGTH_SHORT).show();
                    }
                });

    }


}