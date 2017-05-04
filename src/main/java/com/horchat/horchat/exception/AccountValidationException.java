package com.horchat.horchat.exception;

import android.content.Context;

import com.horchat.horchat.R;
import com.horchat.horchat.model.Account;

public class AccountValidationException extends Exception {
    public final int mType;

    public AccountValidationException(int type) {
        mType = type;
    }
    public int getType() {
        return mType;
    }
    public int toResourceString() {
        int stringResourceId;
        switch (getType()) {
            case Account.E_USERNAMEEMPTY:
                stringResourceId = R.string.connectToServer_eUsernameEmpty;
                break;
            case Account.E_REALNAMEEMPTY:
                stringResourceId = R.string.connectToServer_eRealNameEmpty;
                break;
            case Account.E_NICKNAMEEMPTY:
                stringResourceId = R.string.connectToServer_eNicknameEmpty;
                break;
            case Account.E_NICKNAMELENGTH:
                stringResourceId = R.string.connectToServer_eNicknameLength;
                break;
            default:
                stringResourceId = R.string.connectToServer_eUnknown;
        }
        return stringResourceId;
    }
}
