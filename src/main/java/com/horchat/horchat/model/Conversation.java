package com.horchat.horchat.model;

import java.util.LinkedList;

public class Conversation {
    public static final int TYPE_QUERY = 1;
    public static final int TYPE_SERVER = 2;
    public static final int TYPE_CHANNEL = 3;
    public static final int TYPE_USER = 4;

    // TODO: Implement conversation history
    private final String mName;
    private final int mType;
    private int mStatus;
    private final LinkedList<Message> mBuffer;

    public Conversation(String name, int type) {
        mName = name.toLowerCase();
        mType = type;
        mBuffer = new LinkedList<Message>();
    }
    public String getName() {
        return mName;
    }
    public LinkedList<Message> getBuffer() {
        return mBuffer;
    }
    public void addMessage(Message message) {
        // Append message to the beginning of the list
        mBuffer.add(0, message);
    }
    public Message getNewMessage() {
        int lastElement = mBuffer.size() - 1;
        Message message = mBuffer.get(lastElement);
        mBuffer.remove(lastElement);
        return message;
    }
    public boolean hasNewMessage() {
        return mBuffer.size() > 0;
    }
    public void clearBuffer() {
        mBuffer.clear();
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
