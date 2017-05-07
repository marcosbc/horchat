package com.horchat.horchat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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
        String target = intent.getExtras().getString(IRCBroadcastHandler.CONVERSATION);
        switch (action) {
            case IRCBroadcastHandler.CONVERSATION_NEW:
                mListener.onNewConversation(target);
                break;
            case IRCBroadcastHandler.CONVERSATION_TOPIC:
                mListener.onTopicChanged(target);
                break;
            case IRCBroadcastHandler.CONVERSATION_MESSAGE:
                mListener.onConversationMessage(target);
                break;
            case IRCBroadcastHandler.CONVERSATION_REMOVE:
                mListener.onRemoveConversation(target);
                break;
            default:
        }
    }
}
