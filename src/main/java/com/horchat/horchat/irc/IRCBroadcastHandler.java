package com.horchat.horchat.irc;

import android.content.Intent;

public class IRCBroadcastHandler {
    public static final String TYPE                 = "IRCBroadcastHandler__type";
    // Broadcast types
    public static final String SERVER_UPDATE        = "IRCBroadcastHandler__serverUpdate";
    public static final String CONVERSATION_NEW     = "IRCBroadcastHandler__conversationNew";
    public static final String CONVERSATION_TOPIC   = "IRCBroadcastHandler__conversationTopic";
    public static final String CONVERSATION_MESSAGE = "IRCBroadcastHandler__conversationMessage";
    public static final String CONVERSATION_REMOVE  = "IRCBroadcastHandler__conversationRemove";

    // Broadcast parameter identifiers
    public static final String SERVER         = "IRCBroadcastHandler__server";
    public static final String CONVERSATION         = "IRCBroadcastHandler__conversation";

    public static Intent createServerIntent(String broadcastType) {
        return createIntent(broadcastType, SERVER);
    }
    public static Intent createConversationIntent(String broadcastType) {
        return createIntent(broadcastType, CONVERSATION);
    }
    private static Intent createIntent(String broadcastType, String type) {
        Intent intent = new Intent(broadcastType);
        intent.putExtra(TYPE, type);
        return intent;
    }
}
