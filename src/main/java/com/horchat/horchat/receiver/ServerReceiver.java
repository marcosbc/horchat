package com.horchat.horchat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.horchat.horchat.listener.ServerListener;

public class ServerReceiver extends BroadcastReceiver {
    private ServerListener mListener;

    public ServerReceiver(ServerListener listener) {
        mListener = listener;
    }

    public void onReceive(Context context, Intent intent) {
        mListener.onStatusUpdate();
    }
}
