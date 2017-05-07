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
    Collection<String> mJoinedChannelNames;
    public Session(long id, Account account, Server server) {
        mId = id;
        mAccount = account;
        mServer = server;
        mJoinedChannelNames = new TreeSet<String>();
    }
    public Session(Cursor cursor) {
        mJoinedChannelNames = new TreeSet<String>();
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
    public void joinChannel(String name) {
        Log.d("horchat", "Joining channel: " + name);
        mJoinedChannelNames.add(name);
    }
    public boolean didJoinChannel(String name) {
        for (String channel: mJoinedChannelNames) {
            if (channel.equals(name)) {
                return true;
            }
        }
        return false;
    }
    public Collection<String> getJoinedChannelNames() {
        return mJoinedChannelNames;
    }
}
