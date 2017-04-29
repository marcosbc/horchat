package com.horchat.horchat.model;

import android.database.Cursor;
import android.util.Log;

import java.io.Serializable;

public class Account implements Serializable {
    /* Private attributes */
    private int _id;
    private String username;
    private String nickname;
    private String realname;
    private String password;
    private String host;
    private int port;
    /* Class constructor */
    public Account(Cursor cursor) {
        // Only one row will exist
        cursor.moveToFirst();
        if (cursor.getColumnCount() > 0) {
            int id = 0;
            setId(cursor.getInt(id++));
            setUsername(cursor.getString(id++));
            setNickname(cursor.getString(id++));
            setPassword(cursor.getString(id++));
            setRealname(cursor.getString(id++));
            setHost(cursor.getString(id++));
            setPort(Integer.parseInt(cursor.getString(id++)));
        }
    }
    /* Getters and setters */
    private void setId(int id) {
        this._id = id;
    }
    public int getId() {
        return _id;
    }
    private void setUsername(String username) {
        this.username = username;
    }
    public String getUsername() {
        return username;
    }
    private void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public String getNickname() {
        return nickname;
    }
    private void setPassword(String password) {
        this.password = password;
    }
    public String getPassword() {
        /* TODO: Use this method for decrypting encrypted password */
        return password;
    }
    private void setRealname(String realname) {
        this.realname = realname;
    }
    public String getRealname() {
        return realname;
    }
    private void setHost(String host) {
        this.host = host;
    }
    public String getHost() {
        return host;
    }
    private void setPort(int port) {
        this.port = port;
    }
    public int getPort() {
        return port;
    }
}
