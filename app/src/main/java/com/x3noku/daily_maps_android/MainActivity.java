package com.x3noku.daily_maps_android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private SharedPreferences sharedPreferences;
    private final String preferencesIntroKey = "preferencesIntroKey";
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        Button logOutButton = findViewById(R.id.logOutButton);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getBaseContext(), FirebaseAuthActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = mAuth.getCurrentUser();

        if(!isIntroWatched()) {
            watchIntro();
            startActivity(new Intent(getBaseContext(), IntroActivity.class));
        }
        else if(currentUser == null) {
            startActivity(new Intent(getBaseContext(), FirebaseAuthActivity.class));
        }
        else {
            //ToDo: Delete this trash
            ((TextView)findViewById(R.id.textView)).setText("Hello, "+currentUser.getEmail()+"!");
        }
    }

    private boolean isIntroWatched() {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        return sharedPreferences.getString(preferencesIntroKey, "").equals("1");
    }
    private void watchIntro() {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(preferencesIntroKey, "1");
        editor.apply();
    }


}
