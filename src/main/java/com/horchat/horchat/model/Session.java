package com.horchat.horchat.model;

import android.database.Cursor;

import com.horchat.horchat.exception.AccountValidationException;
import com.horchat.horchat.exception.ServerValidationException;

import java.io.Serializable;

public class Session implements Serializable {
    private Account mAccount;
    private Server mServer;
    public Session(Account account, Server server) {
        mAccount = account;
        mServer = server;
    }
    public Session(Cursor cursor) {
        // We're supposing that the fields were already validated
        cursor.moveToFirst();
        if (cursor.getColumnCount() > 0) {
            int id = 0;
            try {
                long uid = cursor.getLong(id++);
                String username = cursor.getString(id++);
                String realName = cursor.getString(id++);
                String nickname = cursor.getString(id++);
                mAccount = new Account(username, realName, nickname);
                mAccount.setId(uid);
            } catch (AccountValidationException e) {
                // We're supposing that the fields were already validated during registration
                // TODO: Improve this
            }
            try {
                String hostname = cursor.getString(id++);
                String port = String.valueOf(cursor.getInt(id++));
                String password = cursor.getString(id++);
                mServer = new Server(hostname, port, password);
            } catch (ServerValidationException e) {
                // We're supposing that the fields were already validated during registration
                // TODO: Improve this
            }
        }
    }
    public Account getAccount() {
        return mAccount;
    }
    public Server getServer() {
        return mServer;
    }
}
