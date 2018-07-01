package com.example.kenzdozz.journalbay;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.kenzdozz.journalbay.Adapter.NoteAdapter;
import com.example.kenzdozz.journalbay.Data.Note;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    public String userId;

    RecyclerView recyclerView;
    NoteAdapter noteAdapter;
    List<Note> notes;
    FloatingActionButton fab;
    TextView emptyView;
    ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }else {
            userId = user.getUid();
        }

        setContentView(R.layout.activity_home);
        if (user != null)
            snack("Welcome, " + user.getDisplayName());

        fab = findViewById(R.id.fab);

        loading = findViewById(R.id.loading);
        emptyView = findViewById(R.id.empty_view);
        recyclerView = findViewById(R.id.recycler_view);
        notes = new ArrayList<>();
        noteAdapter = new NoteAdapter(notes, userId);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(noteAdapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AddActivity.class));
            }
        });

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {


            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    View itemView = viewHolder.itemView;

                    Paint p = new Paint();
                    Bitmap icon;

                    if (dX > 0) {
                        p.setARGB(255, 0, 145, 0);
                        c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                                (float) itemView.getBottom(), p);
                        Drawable d = getResources().getDrawable(R.drawable.edit);
                        icon = drawableToBitmap(d);
                        c.drawBitmap(icon,
                                (float) itemView.getLeft() + convertDpToPx(),
                                (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight())/2,
                                p);
                    } else {

                        p.setARGB(255, 145, 0, 0);
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                (float) itemView.getRight(), (float) itemView.getBottom(), p);
                        Drawable d = getResources().getDrawable(R.drawable.delete);
                        icon = drawableToBitmap(d);
                        c.drawBitmap(icon,
                                (float) itemView.getRight() - convertDpToPx() - icon.getWidth(),
                                (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight())/2,
                                p);
                    }

                    viewHolder.itemView.setTranslationX(dX);

                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }

            Bitmap drawableToBitmap (Drawable drawable) {

                if (drawable instanceof BitmapDrawable) {
                    return ((BitmapDrawable)drawable).getBitmap();
                }

                Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);

                return bitmap;
            }

            private int convertDpToPx(){
                return Math.round(16 * (getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder noteViewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT) {
                    String reference = notes.get(noteViewHolder.getAdapterPosition()).getReference();

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("users").document(userId).collection("notes").document(reference)
                            .delete();
                }else {
                    Intent intent = new Intent(getApplicationContext(), AddActivity.class);
                    intent.putExtra("reference", notes.get(noteViewHolder.getAdapterPosition()).getReference());
                    intent.putExtra("title", notes.get(noteViewHolder.getAdapterPosition()).getTitle());
                    intent.putExtra("body", notes.get(noteViewHolder.getAdapterPosition()).getBody());
                    intent.putExtra("date", notes.get(noteViewHolder.getAdapterPosition()).getDate());
                    intent.putExtra("top", notes.get(noteViewHolder.getAdapterPosition()).getTop());
                    startActivity(intent);
                }
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        loading.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference collection = db.collection("users").document(userId).collection("notes");
        collection.orderBy("top", Query.Direction.DESCENDING).orderBy("date", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                upDateUI(queryDocumentSnapshots);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        snack("Network Error! " + e.getMessage());
                        Log.e("HERE", e.getMessage());
                    }
                });
        collection.orderBy("top", Query.Direction.DESCENDING).orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                upDateUI(queryDocumentSnapshots);
            }
        });
    }

    private void upDateUI(QuerySnapshot queryDocumentSnapshots) {
        loading.setVisibility(View.GONE);
        notes.clear();
        if (queryDocumentSnapshots != null) {
            for (DocumentSnapshot snapshot: queryDocumentSnapshots){
                Note note = snapshot.toObject(Note.class);
                String ref = snapshot.getReference().getId();
                assert note != null;
                note.setReference(ref);
                notes.add(note);
            }
        }
        noteAdapter.notifyDataSetChanged();
        if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty())
                emptyView.setVisibility(View.VISIBLE);
            else
                emptyView.setVisibility(View.GONE);

    }

    private void snack(String msg) {
        Snackbar.make(findViewById(R.id.main_layout), msg, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout)
            logout();
        if (id == R.id.about)
            startActivity(new Intent(getApplicationContext(), AboutActivity.class));
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        GoogleSignInClient googleSignInClient;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.oauth_client_id)).requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInClient.signOut();
        if (mAuth != null) {
            mAuth.signOut();
            finish();
        }
    }
}
