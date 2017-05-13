package com.horchat.horchat.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Conversation implements Serializable {
    /* Broadcasting-related */
    public static final String TYPE        = "Conversation__TYPE";
    public static final String TITLE       = "Conversation__TITLE";
    public static final String SENDER      = "Conversation__SENDER";
    public static final String LOGIN       = "Conversation__LOGIN";
    public static final String HOSTNAME    = "Conversation__HOSTNAME";
    public static final String MESSAGE     = "Conversation__MESSAGE";
    public static final String TARGET      = "Conversation__TARGET";
    public static final String TOPIC       = "Conversation__TOPIC";
    public static final String SETBY       = "Conversation__SETBY";
    public static final String DATE        = "Conversation__DATE";
    public static final String CHANGED     = "Conversation__CHANGED";
    public static final String CHANNEL     = "Conversation__CHANNEL";
    public static final String KICKERNICK  = "Conversation__KICKERNICK";
    public static final String KICKERLOGIN = "Conversation__KICKERLOGIN";
    public static final String KICKERHOST  = "Conversation__KICKERHOST";
    public static final String RECIPIENT   = "Conversation__RECIPIENT";
    public static final String REASON      = "Conversation__REASON";
    /* Used for determining conversation type */
    public static final int TYPE_SERVER    = 1;
    public static final int TYPE_CHANNEL   = 2;
    public static final int TYPE_USER      = 3;

    // TODO: Implement conversation history
    private final String mName;
    private final int mType;
    private int mStatus;
    private final List<Message> mMessages;
    private boolean mIsRead;

    public Conversation(String name, int type) {
        mName = name.toLowerCase();
        mType = type;
        mMessages = new ArrayList<Message>();
        mIsRead = true;
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
    public boolean isRead() {
        return mIsRead;
    }
    public void markAsUnread() {
        mIsRead = false;
    }
    public void markAsRead() {
        mIsRead = true;
    }
}
