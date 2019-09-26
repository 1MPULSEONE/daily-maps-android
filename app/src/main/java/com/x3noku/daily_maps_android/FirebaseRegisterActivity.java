package com.x3noku.daily_maps_android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import javax.crypto.Cipher;

public class FirebaseRegisterActivity extends AppCompatActivity
        implements
        View.OnClickListener {

    private static final String TAG = "FirebaseAuthActivity";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_register);

        mAuth = FirebaseAuth.getInstance();
        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {
        if( currentUser != null ) {
            startActivity(new Intent(getBaseContext(), MainActivity.class));
        }
    }

    private void registerUserInFireStore(String userId) {
        FirebaseFirestore fireStore = FirebaseFirestore.getInstance();

        fireStore
            .collection("users")
            .document(userId)
            .set( new UserInfo() );
    }

    @Override
    public void onClick(View v) {
        switch ( v.getId() ) {
            case R.id.registerButton:
                String email = ((EditText)findViewById(R.id.emailEditText)).getText().toString();
                String password = ((EditText)findViewById(R.id.passwordEditText)).getText().toString();
                String confirmPassword = ((EditText)findViewById(R.id.confirmPasswordEditText)).getText().toString();
                if( password.equals(confirmPassword) ) {
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "createUserWithEmail:success");
                                        FirebaseUser currentUser = mAuth.getCurrentUser();
                                        registerUserInFireStore( currentUser.getUid() );
                                        updateUI(currentUser);
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                        Snackbar.make(
                                                findViewById(R.id.parentLayoutFirebaseAuth),
                                                getString(R.string.error_auth),
                                                Snackbar.LENGTH_SHORT).show();

                                        updateUI(null);
                                    }
                                }
                            });
                }
                else {
                    Snackbar.make(
                            findViewById(R.id.parentLayoutFirebaseRegister),
                            getString(R.string.error_passwords_matching),
                            Snackbar.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

}
