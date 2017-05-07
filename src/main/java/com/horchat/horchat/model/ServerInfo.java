package com.horchat.horchat.model;

public class ServerInfo extends Conversation {
    public static final String ALL_CONVERSATIONS = "";
    public ServerInfo() {
        super(ALL_CONVERSATIONS);
    }
    public int getType() {
        return Conversation.TYPE_SERVER;
    }
}
