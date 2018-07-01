package com.example.kenzdozz.journalbay;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    GoogleSignInClient googleSignInClient;
    private FirebaseAuth mAuth;
    int SIGN_IN_RC = 234;
    String TAG = "MainActivity";
    ProgressBar loginProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginProgress = findViewById(R.id.login_progress);
        SignInButton signInButton = findViewById(R.id.sign_in_button);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.oauth_client_id)).requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
                loginProgress.setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AboutActivity.class));
            }
        });
    }

    private void signIn() {
        FirebaseAuth.getInstance().signOut();
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, SIGN_IN_RC);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null)
            snack("Welcome, Please Login");
        else {
            snack("Welcome, " + user.getDisplayName());
            startActivity(new Intent(this, HomeActivity.class));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_RC) {
            try {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                GoogleSignInAccount account = task.getResult(ApiException.class);
                fireBaseSignIn(account);
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed " + e.getMessage(), e);
            }
        }
    }

    private void fireBaseSignIn(GoogleSignInAccount acct) {
        Log.d(TAG, "fireBaseSignIn:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            loginProgress.setVisibility(View.INVISIBLE);
                            if (user != null)
                                launchHome(user);
                            else
                                snack("Login failed");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            loginProgress.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }

    private void launchHome(FirebaseUser user) {

        String userName = user.getDisplayName();
        assert userName != null;
        String[] names = userName.split(" ");
        String lastName = names[0].isEmpty() ? "" : names[0];
        String firstName = names[1].isEmpty() ? "" : names[1];

        Map<String, Object> newUser = new HashMap<>();
        newUser.put("firstName", firstName);
        newUser.put("lastName", lastName);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(user.getUid())
                .set(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        snack("Added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        snack("Failed");
                        Log.w(TAG, "Error adding document", e);
                    }
                });

        startActivity(new Intent(this, HomeActivity.class));
    }

    private void snack(String msg) {
        Snackbar.make(findViewById(R.id.main_layout), msg, Snackbar.LENGTH_SHORT).show();
    }
}
