package com.example.kenzdozz.journalbay.Data;

import android.graphics.drawable.Drawable;

import com.example.kenzdozz.journalbay.R;

import java.util.Date;

public class Note {

    private  String title, body;
    private  Date date;
    private  Boolean top;
    private String reference;

    public Note(){}

    public Note(String title, String body, Date date, Boolean top){
        this.title = title;
        this.body = body;
        this.date = date;
        this.top = top;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public Date getDate() {
        return date;
    }

    public Boolean getTop() {
        return top;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
