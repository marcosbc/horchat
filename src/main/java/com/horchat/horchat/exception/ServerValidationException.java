package com.horchat.horchat.exception;

import android.content.Context;

import com.horchat.horchat.R;
import com.horchat.horchat.model.Server;

public class ServerValidationException extends Exception {
    public final int mType;

    public ServerValidationException(int type) {
        mType = type;
    }
    public int getType() {
        return mType;
    }
    public int toResourceString() {
        int stringResourceId;
        switch (getType()) {
            case Server.E_HOSTNOTVALID:
                stringResourceId = R.string.connectToServer_eHostNotValid;
                break;
            case Server.E_PORTNOTINT:
                stringResourceId = R.string.connectToServer_ePortNotInt;
                break;
            case Server.E_PORTRANGE:
                stringResourceId = R.string.connectToServer_ePortRange;
                break;
            default:
                stringResourceId = R.string.connectToServer_eUnknown;
        }
        return stringResourceId;
    }
}
