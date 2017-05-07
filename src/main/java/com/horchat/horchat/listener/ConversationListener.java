package com.horchat.horchat.listener;

public interface ConversationListener {
    void onConversationMessage(String target);
    void onNewConversation(String target);
    void onRemoveConversation(String target);
    void onTopicChanged(String target);
}
