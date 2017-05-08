package com.horchat.horchat.model;

import android.database.Cursor;
import android.util.Log;

import com.horchat.horchat.exception.AccountValidationException;
import com.horchat.horchat.exception.ServerValidationException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

public class Session implements Serializable {
    private long mId;
    private Account mAccount;
    private Server mServer;
    List<Conversation> mConversations;
    public Session(long id, Account account, Server server) {
        mId = id;
        mAccount = account;
        mServer = server;
        mConversations = new ArrayList<Conversation>();
    }
    public Session(Cursor cursor) {
        mConversations = new ArrayList<Conversation>();
        // We're supposing that the fields were already validated
        cursor.moveToFirst();
        if (cursor.getColumnCount() > 0) {
            int id = 0;
            try {
                long sessionId = cursor.getLong(id++);
                String username = cursor.getString(id++);
                String realName = cursor.getString(id++);
                String nickname = cursor.getString(id++);
                mAccount = new Account(username, realName, nickname);
                this.mId = sessionId;
                // TODO: Copy account parameters
            } catch (AccountValidationException e) {
                // We're supposing that the fields were already validated during registration
                // TODO: Improve this
            }
            try {
                String hostname = cursor.getString(id++);
                String port = String.valueOf(cursor.getInt(id++));
                String password = cursor.getString(id++);
                mServer = new Server(hostname, port, password);
                /* Enable server */
                mServer.setAllowReconnection(true);
                mServer.setStatus(Server.STATUS_PRECONNECTING);
            } catch (ServerValidationException e) {
                // We're supposing that the fields were already validated during registration
                // TODO: Improve this
            }
        }
    }
    public long getId() {
        return mId;
    }
    public Account getAccount() {
        return mAccount;
    }
    public Server getServer() {
        return mServer;
    }
    public void newConversation(String name, int type) {
        mConversations.add(new Conversation(name, type));
    }
    public boolean hasConversation(String name) {
        if (getConversation(name) != null) {
            return true;
        }
        return false;
    }
    public Conversation getConversation(String name) {
        for (Conversation conversation: mConversations) {
            if (conversation.getName().equals(name)) {
                return conversation;
            }
        }
        return null;
    }
    private List<Conversation> getConversationsByType(int type) {
        List<Conversation> conversations = new ArrayList<Conversation>();
        for (Conversation conversation : mConversations) {
            if (conversation.getType() == type) {
                conversations.add(conversation);
            }
        }
        return conversations;
    }
    public Collection<String> getConversationNamesByType(int type) {
        Collection<String> conversationNames = new TreeSet<String>();
        for (Conversation conversation: getConversationsByType(type)) {
            conversationNames.add(conversation.getName());
        }
        return conversationNames;
    }
}
