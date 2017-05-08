package com.horchat.horchat.model;

public class ServerInfo extends Conversation {
    public static final String ALL_CONVERSATIONS = "";
    public ServerInfo() {
        super(ALL_CONVERSATIONS, Conversation.TYPE_SERVER);
    }
}
