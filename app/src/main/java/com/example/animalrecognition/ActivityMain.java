
/*
package com.example.animalrecognition;


        import android.content.Intent;
        import androidx.appcompat.app.AppCompatActivity;
        import com.google.firebase.auth.FirebaseAuth;

// This activity is the first activity run with the project, and it pushes the user to the home page if they're already logged in or the login screen if they're not.
public class ActivityMain extends AppCompatActivity
{
    static FirebaseAuth auth;

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent;

        if (auth.getInstance().getCurrentUser() == null) {
            intent = new Intent(ActivityMain.this, ActivityLogin.class);
        } else {
            intent = new Intent(ActivityMain.this, ActivityHome.class);
        }

        startActivity(intent);
        finish();
    }
}
 */
