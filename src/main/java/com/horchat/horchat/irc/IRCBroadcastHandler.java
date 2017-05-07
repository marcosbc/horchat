package com.horchat.horchat.irc;

import android.content.Intent;

public class IRCBroadcastHandler {
    // Broadcast types
    public static final String SERVER_UPDATE        = "IRCBroadcastHandler__serverUpdate";
    public static final String CONVERSATION_NEW     = "IRCBroadcastHandler__conversationNew";
    public static final String CONVERSATION_TOPIC   = "IRCBroadcastHandler__conversationTopic";
    public static final String CONVERSATION_MESSAGE = "IRCBroadcastHandler__conversationMessage";
    public static final String CONVERSATION_REMOVE  = "IRCBroadcastHandler__conversationRemove";

    // Broadcast parameter identifiers
    public static final String CONVERSATION         = "IRCBroadcastHandler__conversation";

    public static Intent createConversationIntent(String broadcastType, String conversationName) {
        Intent conversationIntent = new Intent(broadcastType);
        conversationIntent.putExtra(CONVERSATION, conversationName);
        return conversationIntent;
    }

    public static Intent createServerIntent(String broadcastType) {
        return new Intent(broadcastType);
    }
}
