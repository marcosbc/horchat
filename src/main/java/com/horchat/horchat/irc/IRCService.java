package com.horchat.horchat.irc;

import android.app.Service;
import android.content.Intent;

public class IRCService extends Service {
    private final IRCBinder mBinder;
    public IRCService() {
        mBinder = new IRCBinder(this);
    }
    @Override
    public IRCBinder onBind(Intent intent) {
        return mBinder;
    }
}
