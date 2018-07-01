package com.example.kenzdozz.journalbay.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kenzdozz.journalbay.AddActivity;
import com.example.kenzdozz.journalbay.Data.Note;
import com.example.kenzdozz.journalbay.R;
import com.example.kenzdozz.journalbay.ViewActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> notes;
    private Context context;
    private String userId;

    public NoteAdapter(List<Note> notes, String userId){
        this.notes = notes;
        this.userId = userId;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.note_card, viewGroup, false);
        return new NoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final NoteViewHolder noteViewHolder, final int position) {
        Note note = notes.get(position);
        noteViewHolder.title.setText(note.getTitle());
        CharSequence niceDateStr = DateUtils.getRelativeTimeSpanString(note.getDate().getTime() , Calendar.getInstance().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS);
        noteViewHolder.date.setText(niceDateStr.toString());
        int drawable = (note.getTop())? R.drawable.pin : R.drawable.no_pin;
        noteViewHolder.top.setImageResource(drawable);

        noteViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context.getApplicationContext(), ViewActivity.class);
                intent.putExtra("reference", notes.get(noteViewHolder.getAdapterPosition()).getReference());
                context.startActivity(intent);
            }
        });

        noteViewHolder.top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String reference = notes.get(noteViewHolder.getAdapterPosition()).getReference();
                Boolean top = notes.get(noteViewHolder.getAdapterPosition()).getTop();

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users").document(userId).collection("notes").document(reference)
                        .update("top", !top);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView title, date;
        ImageView top;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            title = itemView.findViewById(R.id.title);
            date = itemView.findViewById(R.id.date);
            top = itemView.findViewById(R.id.top);
        }
    }
}
