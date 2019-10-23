package com.x3noku.daily_maps_android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseAuthActivity extends AppCompatActivity
        implements
        View.OnClickListener {

    private static final String TAG = "FirebaseAuthActivity";
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore fireStore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_auth);

        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        fireStore = FirebaseFirestore.getInstance();

        TextView registerLink = findViewById(R.id.registerLink);
        registerLink.setOnClickListener(this);

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);

        SignInButton signInButton = findViewById(R.id.signInGoogleButton);
        signInButton.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if( account != null ) {
                    firebaseAuthWithGoogle(account);
                }
                else {
                    Log.i(TAG, "onActivityResult: Account Is Null");
                }
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Snackbar.make(
                        findViewById(R.id.parentLayoutFirebaseAuth),
                        getString(R.string.error_auth) + e.getLocalizedMessage(),
                        Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            DocumentReference userDocument = fireStore.collection("users").document( currentUser.getUid() );
                            userDocument.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        assert document != null;
                                        if( !document.exists() ) {
                                            // Document Doesn't exist, Register New UserInfo
                                            Log.e(TAG, "No such document");
                                            registerUserInFireStore( currentUser.getUid(), currentUser.getDisplayName() );
                                        }
                                    }
                                    else {
                                        // Something went wrong
                                        Log.e(TAG, "get failed with ", task.getException());
                                    }
                                }
                            });
                            updateUI(currentUser);
                        }

                        else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(
                                    findViewById(R.id.parentLayoutFirebaseAuth),
                                    getString(R.string.error_auth) + task.getException().getLocalizedMessage(),
                                    Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void updateUI(FirebaseUser currentUser) {
        if( currentUser != null ) {
            startActivity(new Intent(getBaseContext(), MainActivity.class));
        }
    }

    private void registerUserInFireStore(String userId, String userName) {
        fireStore
            .collection("users")
            .document(userId)
            .set( new UserInfo(userName) );
    }

    private void signInWithEmailAndPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            updateUI(currentUser);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Snackbar.make(
                                    findViewById(R.id.parentLayoutFirebaseAuth),
                                    getString(R.string.error_auth),
                                    Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch( v.getId() ) {
            case R.id.registerLink:
                startActivity(new Intent(getBaseContext(), FirebaseRegisterActivity.class));
                break;
            case R.id.signInGoogleButton:
                    signIn();
                break;
            case R.id.loginButton:
                String email = ((EditText)findViewById(R.id.emailEditText)).getText().toString();
                String password = ((EditText)findViewById(R.id.passwordEditText)).getText().toString();
                if( !email.equals("") && !password.equals("") ) {
                    signInWithEmailAndPassword(email, password);
                }
                else {
                    Snackbar
                        .make(
                            findViewById(R.id.parentLayoutFirebaseAuth),
                            getString(R.string.error_empty_fields),
                            Snackbar.LENGTH_SHORT
                        )
                        .show();

                }
                break;
            default:
                break;
        }
    }

}
