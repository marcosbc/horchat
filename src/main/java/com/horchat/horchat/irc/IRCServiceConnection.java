package com.horchat.horchat.irc;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class IRCServiceConnection implements ServiceConnection {
    private IRCBinder mBinder = null;
    private IRCService mService = null;
    private boolean mBound = false;

    @Override
    public void onServiceConnected(ComponentName className, IBinder binder) {
        mBinder = (IRCBinder) binder;
        mService = mBinder.getService();
        mBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        mBound = false;
        mService = null;
    }

    public boolean isBound() {
        return mBound;
    }
}
