package com.x3noku.daily_maps_android;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class MainActivity extends AppCompatActivity
        implements
        View.OnClickListener,
        AHBottomNavigation.OnTabSelectedListener {

    private static final String TAG = "MainActivity";
    private SharedPreferences sharedPreferences;
    private final String preferencesIntroKey = "preferencesIntroKey";
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore fireStore;
    private User userInfo;
    private  AHBottomNavigation bottomNavigation;
    private byte previousSelectedItem;
    private Dialog fullscreenDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*--- [Initialize all Data] ---*/
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        fireStore = FirebaseFirestore.getInstance();

        setContentView(R.layout.activity_main);
        createBottomNavigationMenu();

        //fullscreenDialog = new Dialog(this, R.style.AppTheme);
        //fullscreenDialog.setContentView(R.layout.dialog_fullscreen);
        /*--- [/Initialize all Data] ---*/

        Button logOutButton = findViewById(R.id.logOutButton);
        logOutButton.setOnClickListener(this);
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
            DocumentReference userDocument = fireStore.collection("users").document( currentUser.getUid() );
            userDocument.addSnapshotListener((documentSnapshot, e) -> {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                if ( documentSnapshot != null && documentSnapshot.exists() ) {
                    Log.d(TAG, "Current data: " + documentSnapshot.getData());
                    userInfo = documentSnapshot.toObject(User.class);
                    ((TextView)findViewById(R.id.textView)).setText("Hello, "+currentUser.getEmail()+"!");
                }
                else {
                    Log.d(TAG, "Current data: null");
                }
            });
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

    private void createBottomNavigationMenu() {
        bottomNavigation = findViewById(R.id.bottom_navigation);

    // Create items
        AHBottomNavigationItem firstItem = new AHBottomNavigationItem(R.string.label_tab_1, R.drawable.ic_home, R.color.color_tab_1);
        AHBottomNavigationItem secondItem = new AHBottomNavigationItem(R.string.label_tab_2, R.drawable.ic_add_circle, R.color.color_tab_2);
        AHBottomNavigationItem thirdItem = new AHBottomNavigationItem(R.string.label_tab_3, R.drawable.ic_profile, R.color.color_tab_3);
        previousSelectedItem = 0;
    // Add items
        bottomNavigation.addItem(firstItem);
        bottomNavigation.addItem(secondItem);
        bottomNavigation.addItem(thirdItem);
    // Set background color
        bottomNavigation.setDefaultBackgroundColor( ContextCompat.getColor(this, R.color.WHITE) );
    // Change colors
        bottomNavigation.setAccentColor( ContextCompat.getColor(this, R.color.color_accent_navigation) );
        bottomNavigation.setInactiveColor( ContextCompat.getColor(this, R.color.color_inactive_navigation) );
    // Force to tint the drawable (useful for font with icon for example)
        bottomNavigation.setForceTint(true);
    // Manage titles
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
    // Set listeners
        bottomNavigation.setOnTabSelectedListener(this);
    }

    private void openFullScreenDialog() {
        /*fullscreenDialog = new Dialog(this, R.style.AppTheme);
        fullscreenDialog.setContentView(R.layout.dialog_fullscreen);
        Toolbar toolbarFullscreenDialog = fullscreenDialog.findViewById(R.id.toolbarFullScreenDialog);
        toolbarFullscreenDialog.setNavigationOnClickListener(v -> {
            fullscreenDialog.dismiss();
            bottomNavigation.setCurrentItem(previousSelectedItem, false);
        });
        fullscreenDialog.show();
         */
        AddTaskFragment addTaskFragment = AddTaskFragment.display(getSupportFragmentManager(), bottomNavigation, previousSelectedItem);
    }

    @Override
    public boolean onTabSelected(int position, boolean wasSelected) {
        switch(position) {
            case 0:
                Toast.makeText(this, "Opened Home Page", Toast.LENGTH_SHORT).show();
                previousSelectedItem = 0;
                break;
            case 1:
                openFullScreenDialog();
                break;
            case 2:
                Toast.makeText(this, "Opened My Profile", Toast.LENGTH_SHORT).show();
                previousSelectedItem = 2;
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch( v.getId() ) {
            case R.id.logOutButton:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getBaseContext(), FirebaseAuthActivity.class));
                break;
        }
    }

}
