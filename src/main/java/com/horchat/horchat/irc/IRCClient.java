package com.horchat.horchat.irc;

import android.content.Intent;
import android.util.Log;

import com.horchat.horchat.model.Account;
import com.horchat.horchat.model.Channel;
import com.horchat.horchat.model.Conversation;
import com.horchat.horchat.model.Server;
import com.horchat.horchat.model.Session;

import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class IRCClient extends PircBot {
    private static final String ID = "horchat";

    private final IRCService mService;
    private final Session mSession;
    private Map<String, Channel> mChannelList;
    private boolean mRegistered;

    /* Class constructor */
    public IRCClient(IRCService service, Session session) {
        Log.d(ID, "IRCClient::IRCClient called");
        mService = service;
        mSession = session;
        mChannelList = new HashMap<String, Channel>();
        // If nick already exists, change to a different one automatically
        setAutoNickChange(true);
        mRegistered = false;
    }
    public void setUsername(String username) {
        setLogin(username);
    }
    public void setRealName(String realName) {
        setVersion(realName);
    }
    public void setNickname(String nickname) {
        setName(nickname);
    }
    public void onConnect() {
        Log.d(ID, "IRCClient::onConnect called");
        Server server = mSession.getServer();
        server.setStatus(Server.STATUS_CONNECTED);
        server.setAllowReconnection(true);
        // Send a server update
        mService.sendBroadcast(
                IRCBroadcastHandler.createServerIntent(IRCBroadcastHandler.SERVER_UPDATE)
        );
        // Create broadcast message for successful login
        /*
        Intent createConversationIntent = IRCBroadcastHandler.createConversationIntent(
                IRCBroadcastHandler.CONVERSATION_MESSAGE,
                ServerInfo.ALL_CONVERSATIONS
        );
        mService.sendBroadcast(createConversationIntent);
        */
    }
    public List<Channel> getChannelList() {
        List<Channel> channels = new ArrayList<Channel>();
        SortedSet<String> keys = new TreeSet<String>(mChannelList.keySet());
        for(String key: keys) {
            channels.add(mChannelList.get(key));
        }
        return channels;
    }
    public void onRegistered() {
        mRegistered = true;
        for (Conversation conversation: mService.getAutoJoinChannelList()) {
            if (conversation.getType() == Conversation.TYPE_CHANNEL) {
                joinChannel(conversation.getName());
            }
        }
    }
    @Override
    protected void onServerResponse (int code, String response) {
        if (code == 4) {
            onRegistered();
        }
        // Parse message and send to client
        // TODO
    }
    @Override
    protected void onChannelInfo(String channel, int userCount, String topic) {
        Log.d(ID, "Called onChannelInfo: " + channel + ", " + userCount + ", " + topic);
        Channel previousChannel = mChannelList.get(channel);
        Channel newChannel = new Channel(channel);
        if (previousChannel != null) {
            // TODO: Set channel attributes: User count, topic...
            //previousChannel.update(newChannel);
        } else {
            mChannelList.put(channel, newChannel);
        }
    }
    @Override
    protected void onJoin(String channel, String sender, String login, String hostname) {
        Log.d(ID, "User joined " + channel + ": " + login + " " + sender + " " + hostname);
        Intent intent = IRCBroadcastHandler.createConversationIntent(
                IRCBroadcastHandler.CONVERSATION_NEW);
        intent.putExtra(Conversation.TYPE, Conversation.TYPE_CHANNEL);
        intent.putExtra(Conversation.TITLE, channel);
        intent.putExtra(Conversation.SENDER, sender);
        intent.putExtra(Conversation.LOGIN, login);
        intent.putExtra(Conversation.HOSTNAME, hostname);
        mService.sendBroadcast(intent);
    }
    @Override
    protected void onMessage(String channel, String sender, String login, String hostname,
                             String message) {
        Log.d(ID, "Got message from " + sender + " in " + channel + ": " + message);
        onMessageHandler(Conversation.TYPE_CHANNEL, sender, login, hostname, message, channel);
    }
    @Override
    protected void onPrivateMessage(String sender, String login, String hostname, String message) {
        Log.d(ID, "Got private message from " + sender + ": " + message);
        onMessageHandler(Conversation.TYPE_USER, sender, login, hostname, message, sender);
    }
    private void onMessageHandler(int type, String sender, String login, String hostname,
                                  String message, String title) {
        Intent intent = null;
        if (mSession.getConversation(title) == null) {
            /* It's a new conversation */
            intent = IRCBroadcastHandler.createConversationIntent(
                    IRCBroadcastHandler.CONVERSATION_NEW);
        } else {
            /* New message to the conversation */
            intent = IRCBroadcastHandler.createConversationIntent(
                    IRCBroadcastHandler.CONVERSATION_MESSAGE);
        }
        intent.putExtra(Conversation.TYPE, type);
        intent.putExtra(Conversation.SENDER, sender);
        intent.putExtra(Conversation.LOGIN, login);
        intent.putExtra(Conversation.HOSTNAME, hostname);
        intent.putExtra(Conversation.MESSAGE, message);
        if (title != null && title.length() > 0) {
            /* Private messages don't set channel */
            intent.putExtra(Conversation.TITLE, title);
        }
        mService.sendBroadcast(intent);
    }
    @Override
    protected void onKick(String channel, String kickerNick, String kickerLogin,
                          String kickerHostname, String recipientNick, String reason) {
        Log.d(ID, "Received kick of " + recipientNick + " from " + channel);
        Intent intent = IRCBroadcastHandler.createConversationIntent(
                IRCBroadcastHandler.CONVERSATION_REMOVE);
        intent.putExtra(Conversation.CHANNEL, channel);
        intent.putExtra(Conversation.KICKERNICK, kickerNick);
        intent.putExtra(Conversation.KICKERLOGIN, kickerLogin);
        intent.putExtra(Conversation.KICKERHOST, kickerHostname);
        intent.putExtra(Conversation.RECIPIENT, recipientNick);
        intent.putExtra(Conversation.REASON, reason);
        mService.sendBroadcast(intent);
    }
    @Override
    protected void onTopic(String target, String topic, String setBy, long date, boolean changed) {
        Log.d(ID, "Received topic change: " + topic);
        Intent intent = IRCBroadcastHandler.createConversationIntent(
                IRCBroadcastHandler.CONVERSATION_TOPIC);
        intent.putExtra(Conversation.TARGET, target);
        intent.putExtra(Conversation.TOPIC, topic);
        intent.putExtra(Conversation.SETBY, setBy);
        intent.putExtra(Conversation.DATE, date);
        intent.putExtra(Conversation.CHANGED, changed);
        mService.sendBroadcast(intent);
    }
    @Override
    protected void onFinger(String sourceNick, String sourceLogin, String sourceHostname,
                            String target) {
        Log.d(ID, "Received FINGER: " + sourceNick);
    }
    @Override
    protected void onPing(String sourceNick, String sourceLogin, String sourceHostname,
                          String target, String pingValue) {
        Log.d(ID, "Received PING: " + sourceNick);
    }
    @Override
    protected void onVersion(String sourceNick, String sourceLogin, String sourceHostname,
                             String target) {
        Log.d(ID, "Received VERSION: " + sourceNick);
    }
    @Override
    protected void onUserList(String channel, User[] users) {
        Log.d(ID, "Received user list: " + users);
    }
}
