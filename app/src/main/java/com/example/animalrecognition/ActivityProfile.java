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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

import javax.annotation.Nullable;

public class ActivityProfile extends AppCompatActivity {

    private FirebaseUser user;
    private FirebaseFirestore fStore;
    public static final String TAG = "TAG";
    private ImageView profilePicture;
    private TextView userIdHeader;
    private EditText entryUserName, entryUserProfession;
    private String userUid, userName, profession;
    private int takeImageCode = 10001;

    private void startHome() {
        Intent intHome = new Intent(ActivityProfile.this, ActivityHome.class);
        startActivity(intHome);

        finish();
    }

    private void sendPasswordResetEmail() {
        final String email = user.getEmail();
        assert email != null;
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ActivityProfile.this, "Password reset instructions sent to " + email, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ActivityProfile.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        fStore = FirebaseFirestore.getInstance();

        Button buttonHome = findViewById(R.id.buttonHome);
        Button buttonApply = findViewById(R.id.buttonApply);
        Button buttonResetPassword = findViewById(R.id.buttonResetPassword);
        TextView emailHeader = findViewById(R.id.emailHeader);
        userIdHeader = findViewById(R.id.userIdHeader);
        entryUserName = findViewById(R.id.entryUserName);
        profilePicture = findViewById(R.id.profilePicture);
        entryUserProfession = findViewById(R.id.entryUserProfession);

        emailHeader.setText(user.getEmail());
        userUid = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        // Propagate the edit name and profession fields with the stuff saved in the database
        DocumentReference documentReferenceInfo = fStore.collection("Users").document(userUid);
        documentReferenceInfo.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                assert documentSnapshot != null;
                entryUserName.setText(documentSnapshot.getString("name"));
                entryUserProfession.setText(documentSnapshot.getString("profession"));

                userIdHeader.setText(documentSnapshot.getString("userId"));
            }
        });

        // Propagate profile picture from the stuff stored in database
        if (user.getPhotoUrl() != null) {
            Glide.with(this).load(user.getPhotoUrl()).into(profilePicture);
        }
        
        buttonApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Converts the contents of the entries into "clean" strings with no leading or trailing spaces
                userName = entryUserName.getText().toString().trim();
                profession = entryUserProfession.getText().toString().trim();

                DocumentReference documentReferenceUpdate = fStore.collection("Users").document(userUid);

                // Sanitize inputs to make sure they meet requirements
                if (userName.isEmpty()) {
                    entryUserName.setError("Please enter your name");
                    entryUserName.requestFocus();
                } else if (userName.length() < 2) {
                    entryUserName.setError("Name must be at least 2 characters");
                    entryUserName.requestFocus();
                } else if (profession.isEmpty()) {
                    entryUserProfession.setError("Please enter your profession");
                    entryUserProfession.requestFocus();
                } else {
                    // Push the new information to the database
                    documentReferenceUpdate.update("name", userName, "profession", profession)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ActivityProfile.this, "Profile Update Successful", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ActivityProfile.this, "Profile Update Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        buttonResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPasswordResetEmail();
            }
        });

        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startHome();
            }
        });
    }

    // The following functions handle profile picture uploading
    public void handleImageClick(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, takeImageCode);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && requestCode == takeImageCode && resultCode == RESULT_OK) {
            Bitmap bitmap = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
            profilePicture.setImageBitmap(bitmap);

            assert bitmap != null;
            handleUpload(bitmap);
        }
    }

    private void handleUpload(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayStream);

        String uID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        final  StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("ProfileImages")
                .child(uID + ".jpeg");

        reference.putBytes(byteArrayStream.toByteArray())
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

    private void setUserProfileURL(Uri uri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();

        user.updateProfile(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ActivityProfile.this, "Profile picture successfully updated", Toast.LENGTH_SHORT).show();
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