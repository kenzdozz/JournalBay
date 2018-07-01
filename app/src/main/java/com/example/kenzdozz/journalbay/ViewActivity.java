package com.example.kenzdozz.journalbay;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ViewActivity extends AppCompatActivity {

    Boolean topExtra;
    String reference, titleExtra, bodyExtra, userId;
    TextView title, body, date;
    Date dateExtra;
    ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        FirebaseApp.initializeApp(this);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null)
            finish();
        assert user != null;
        userId = user.getUid();

        title = findViewById(R.id.title);
        date = findViewById(R.id.date);
        body = findViewById(R.id.body);
        loading = findViewById(R.id.loading);

        Intent intent = getIntent();
        if (!intent.hasExtra("reference"))
            finish();

        reference = intent.getStringExtra("reference");
    }

    @Override
    protected void onStart() {
        super.onStart();
        loading.setVisibility(View.VISIBLE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(userId).collection("notes").document(reference);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                titleExtra = documentSnapshot.getString("title");
                bodyExtra = documentSnapshot.getString("body");
                dateExtra = documentSnapshot.getDate("date");
                topExtra = documentSnapshot.getBoolean("top");

                loading.setVisibility(View.GONE);
                title.setText(titleExtra);
                body.setText(bodyExtra);
                SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault());
                String convertedDate = dateExtra.toString();
                try {
                    Date dateFormat = format.parse(dateExtra.toString());
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.getDefault());
                    convertedDate = simpleDateFormat.format(dateFormat);
                } catch (ParseException e) {
                    e.printStackTrace();
                    snack(e.getMessage());
                }
                date.setText(convertedDate);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                finish();
            }
        });
    }

    private void snack(String msg) {
        Snackbar.make(findViewById(R.id.main_layout), msg, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_menu, menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.edit)
            editNote();
        if (id == R.id.delete)
            deleteNote();
        return super.onOptionsItemSelected(item);
    }

    private void editNote() {
        Intent intent = new Intent(getApplicationContext(), AddActivity.class);
        intent.putExtra("reference", reference);
        intent.putExtra("title", titleExtra);
        intent.putExtra("body", bodyExtra);
        intent.putExtra("date", dateExtra);
        intent.putExtra("top", topExtra);
        startActivity(intent);
    }

    private void deleteNote() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).collection("notes").document(reference)
                .delete();
        finish();
    }
}
