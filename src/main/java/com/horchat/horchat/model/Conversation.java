package com.horchat.horchat.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Conversation implements Serializable {
    public static final int TYPE_SERVER = 1;
    public static final int TYPE_CHANNEL = 2;
    public static final int TYPE_USER = 3;

    // TODO: Implement conversation history
    private final String mName;
    private final int mType;
    private int mStatus;
    private final List<Message> mMessages;

    public Conversation(String name, int type) {
        mName = name.toLowerCase();
        mType = type;
        mMessages = new ArrayList<Message>();
    }
    public String getName() {
        return mName;
    }
    public List<Message> getMessages() {
        return mMessages;
    }
    public void addMessage(Message message) {
        // Append message to the beginning of the list
        mMessages.add(message);
    }
    public Message getNewMessage() {
        int lastElement = mMessages.size() - 1;
        Message message = mMessages.get(lastElement);
        mMessages.remove(lastElement);
        return message;
    }
    public boolean hasNewMessage() {
        return mMessages.size() > 0;
    }
    public void clearMessages() {
        mMessages.clear();
    }
    public void setStatus(int status) {
        // TODO: Validate status change
        mStatus = status;
    }
    public int getStatus() {
        return mStatus;
    }
    public int getType() {
        return mType;
    }
}
