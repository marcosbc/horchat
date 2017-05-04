package com.horchat.horchat.model;

import android.database.Cursor;
import android.util.Log;

import com.horchat.horchat.exception.AccountValidationException;

import java.io.Serializable;

public class Account implements Serializable {
    /* Constants */
    public static final int E_USERNAMEEMPTY = 21;
    public static final int E_REALNAMEEMPTY = 22;
    public static final int E_NICKNAMEEMPTY = 23;
    public static final int E_NICKNAMELENGTH = 24;
    public static final int MAX_NICKNAME_LENGTH = 9;
    /* Private attributes */
    private long mId;
    private String mUsername;
    private String mRealName;
    private String mNickname;
    /* Class constructor */
    public Account(CharSequence username, CharSequence realName, CharSequence nickname)
            throws AccountValidationException {
        setUsername(username.toString());
        setRealName(realName.toString());
        setNickname(nickname.toString());
    }
    /* Getters and setters */
    public void setId(long id) {
        this.mId = id;
    }
    public long getId() {
        return mId;
    }
    private void setUsername(String username) throws AccountValidationException {
        if (username == null || username.isEmpty()) {
            throw new AccountValidationException(E_USERNAMEEMPTY);
        } else {
            this.mUsername = username;
        }
    }
    public String getUsername() {
        return mUsername;
    }
    private void setRealName(String realname) throws AccountValidationException {
        if (realname == null || realname.isEmpty()) {
            throw new AccountValidationException(E_REALNAMEEMPTY);
        } else {
            this.mRealName = realname;
        }
    }
    public String getRealName() {
        return mRealName;
    }
    private void setNickname(String nickname) throws AccountValidationException {
        if (nickname == null || nickname.isEmpty()) {
            throw new AccountValidationException(E_NICKNAMEEMPTY);
        } else if (nickname.length() > MAX_NICKNAME_LENGTH) {
            throw new AccountValidationException(E_NICKNAMELENGTH);
        } else {
            this.mNickname = nickname;
        }
    }
    public String getNickname() {
        return mNickname;
    }
}
