package com.horchat.horchat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.horchat.horchat.irc.IRCBroadcastHandler;
import com.horchat.horchat.listener.ConversationListener;

public class ConversationReceiver extends BroadcastReceiver {
    private long mSessionId;
    private ConversationListener mListener;

    public ConversationReceiver(long sessionId, ConversationListener listener) {
        mSessionId = sessionId;
        mListener = listener;
    }

    public void onReceive(Context context, Intent intent) {
        // TODO: Verify session id
        String action = intent.getAction();
        Bundle args = intent.getExtras();
        switch (action) {
            case IRCBroadcastHandler.CONVERSATION_NEW:
                mListener.onNewConversation(args);
                break;
            case IRCBroadcastHandler.CONVERSATION_TOPIC:
                mListener.onTopicChanged(args);
                break;
            case IRCBroadcastHandler.CONVERSATION_MESSAGE:
                mListener.onConversationMessage(args);
                break;
            case IRCBroadcastHandler.CONVERSATION_REMOVE:
                mListener.onRemoveConversation(args);
                break;
            default:
        }
    }
}
