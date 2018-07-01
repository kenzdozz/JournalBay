package com.example.kenzdozz.journalbay;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddActivity extends AppCompatActivity {

    EditText title, body;
    String userId;
    Switch top;
    Button save;
    String reference, titleExtra, bodyExtra;
    Date dateExtra;
    Boolean topExtra, edit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null)
            finish();
        assert user != null;
        userId = user.getUid();

        setContentView(R.layout.activity_add);

        title = findViewById(R.id.title);
        body = findViewById(R.id.body);
        top = findViewById(R.id.top);

        Intent intent = getIntent();
        if (intent.hasExtra("reference")) {
            edit = true;
            reference = intent.getStringExtra("reference");
            titleExtra = intent.getStringExtra("title");
            bodyExtra = intent.getStringExtra("body");
            dateExtra = (Date)intent.getSerializableExtra("date");
            topExtra = intent.getBooleanExtra("top", false);

            title.setText(titleExtra);
            body.setText(bodyExtra);
            if (topExtra)
                top.setChecked(true);
            snack(dateExtra.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save)
            saveNote();
        return super.onOptionsItemSelected(item);
    }

    private void saveNote(){
        String titleText = title.getText().toString();
        String bodyText = body.getText().toString();
        if (titleText.isEmpty() || bodyText.isEmpty()){
            snack(getString(R.string.empty_fields));
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> newNote = new HashMap<>();
        newNote.put("title", titleText);
        newNote.put("body", bodyText);
        newNote.put("date", new Date());
        newNote.put("top", top.isChecked());

        if (edit) {
            newNote.put("date", dateExtra);
            db.collection("users").document(userId).collection("notes").document(reference)
                    .update(newNote).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    snack("Saved successfully!");
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            snack("Saving failed");
                        }
                    });
        }else {
            db.collection("users").document(userId).collection("notes")
                    .add(newNote).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    snack("Note Added");
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            snack("Adding failed");
                        }
                    });
        }
        finish();
    }

    private void snack(String msg) {
        Snackbar.make(findViewById(R.id.main_layout), msg, Snackbar.LENGTH_SHORT).show();
    }
}
