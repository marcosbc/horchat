package com.horchat.horchat.model;

import android.text.format.DateFormat;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message implements Serializable {
    public static final int NUM_TYPES = 2;
    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_SERVER = 1;

    private String mText;
    private String mSender;
    private Date mDate;
    private int mType;

    public Message(String text, String sender, Date date) {
        populateMessage(text, sender, date);
        mType = TYPE_DEFAULT;
    }
    public Message(String text, String sender, Date date, int type) {
        populateMessage(text, sender, date);
        mType = type;
    }
    private void populateMessage(String text, String sender, Date date) {
        mText = text;
        mSender = sender;
        mDate = date;
    }
    public String getText() {
        return mText;
    }
    public String getSender() {
        return mSender;
    }
    public Date getDate() {
        return mDate;
    }
    public String getFormattedDate() {
        SimpleDateFormat df = new SimpleDateFormat();
        return df.format(mDate);
    }
    public int getType() {
        return mType;
    }
}
