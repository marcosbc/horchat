package com.horchat.horchat.listener;

import android.os.Bundle;

public interface ConversationListener {
    void onConversationMessage(Bundle args);
    void onNewConversation(Bundle args);
    void onRemoveConversation(Bundle args);
    void onTopicChanged(Bundle args);
}
