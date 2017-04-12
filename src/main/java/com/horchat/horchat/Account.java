package com.horchat.horchat;

import android.database.Cursor;

public class Account {
    /* Private attributes */
    private int _id;
    private String username;
    private String nickname;
    private String realname;
    private String password;
    private String host;
    private int port;
    /* Class constructor */
    public Account(int id, Cursor cursor) {
        // Only one row will exist
        cursor.moveToFirst();
        if (cursor.getColumnCount() > 0) {
        /* Note: The user id will be set later */
            setUsername(cursor.getString(0));
            setNickname(cursor.getString(1));
            setPassword(cursor.getString(2));
            setRealname(cursor.getString(3));
            setHost(cursor.getString(4));
            setPort(Integer.parseInt(cursor.getString(5)));
        }
    }
    /* Getters and setters */
    public void setId(int id) {
        this._id = id;
    }
    public int getId() {
        return _id;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getUsername() {
        return username;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public String getNickname() {
        return nickname;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getPassword() {
        /* TODO: Use this method for decrypting encrypted password */
        return password;
    }
    public void setRealname(String realname) {
        this.realname = realname;
    }
    public String getRealname() {
        return realname;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public String getHost() {
        return host;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public int getPort() {
        return port;
    }
}
