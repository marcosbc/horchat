package com.horchat.horchat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.horchat.horchat.listener.SessionListener;

public class SessionReceiver extends BroadcastReceiver {
    private SessionListener mListener;

    public SessionReceiver(SessionListener listener) {
        mListener = listener;
    }

    public void onReceive(Context context, Intent intent) {
        /* Parse server messages */
        Bundle args = intent.getExtras();
        if (args != null) {
            mListener.onStatusMessage(args);
        }
        /* Call status check */
        mListener.onStatusCheck();
    }
}
