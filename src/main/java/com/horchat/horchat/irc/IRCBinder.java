package com.horchat.horchat.irc;

import android.os.Binder;

public class IRCBinder extends Binder {
    private final IRCService mService;
    public IRCBinder(IRCService service) {
        super();
        this.mService = service;
    }
    public IRCService getService() {
        return mService;
    }
}
