package com.horchat.horchat.irc;

import android.content.Intent;
import android.os.Bundle;
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
    private static final int MAX_CHANNEL_LIST = 100;

    private final IRCService mService;
    private final Session mSession;
    private Map<String, Channel> mChannelList;
    private boolean mRegistered;

    /* Class constructor */
    public IRCClient(IRCService service, Session session) {
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
        Server server = mSession.getServer();
        server.setAllowReconnection(true);
        // Send a server update
        Intent serverIntent = IRCBroadcastHandler
                .createServerIntent(IRCBroadcastHandler.SERVER_UPDATE);
        serverIntent.putExtra(Server.MESSAGE_TYPE, Server.MESSAGE_CONNECTED);
        mService.sendBroadcast(serverIntent);
    }
    @Override
    protected void onDisconnect () {
        // Send a server update
        Intent serverIntent = IRCBroadcastHandler
                .createServerIntent(IRCBroadcastHandler.SERVER_UPDATE);
        serverIntent.putExtra(Server.MESSAGE_TYPE, Server.MESSAGE_DISCONNECTED);
        mService.sendBroadcast(serverIntent);
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
        if (mChannelList.size() > MAX_CHANNEL_LIST) {
            // Freenode and other servers have way too many channels...
            return;
        }
        Log.d(ID, "Called onChannelInfo: " + channel + ", " + userCount + ", " + topic);
        Channel previousChannel = mChannelList.get(channel);
        Channel newChannel = new Channel(channel);
        if (previousChannel != null) {
            // TODO: Set channel attributes: User count, topic...
            //previousChannel.update(newChannel);
        } else {
            mChannelList.put(channel, newChannel);
        }
        // Send a server update
        Bundle extra = new Bundle();
        Intent serverIntent = IRCBroadcastHandler
                .createServerIntent(IRCBroadcastHandler.SERVER_UPDATE);
        serverIntent.putExtra(Server.MESSAGE_TYPE, Server.MESSAGE_CHANNELS);
        serverIntent.putExtras(extra);
        mService.sendBroadcast(serverIntent);
    }
    @Override
    protected void onJoin(String channel, String sender, String login, String hostname) {
        Log.d(ID, "User joined " + channel + ": " + login + " " + sender + " " + hostname);
        Bundle extra = new Bundle();
        extra.putInt(Conversation.TYPE, Conversation.TYPE_CHANNEL);
        extra.putString(Conversation.TITLE, channel);
        extra.putString(Conversation.SENDER, sender);
        extra.putString(Conversation.LOGIN, login);
        extra.putString(Conversation.HOSTNAME, hostname);
        // Send user-oriented broadcast
        Intent intent = IRCBroadcastHandler.createConversationIntent(
                IRCBroadcastHandler.CONVERSATION_NEW);
        intent.putExtras(extra);
        mService.sendBroadcast(intent);
        // Send a server update
        Intent serverIntent = IRCBroadcastHandler
                .createServerIntent(IRCBroadcastHandler.SERVER_UPDATE);
        serverIntent.putExtra(Server.MESSAGE_TYPE, Server.MESSAGE_JOIN);
        serverIntent.putExtras(extra);
        mService.sendBroadcast(serverIntent);
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
        Bundle extra = new Bundle();
        extra.putString(Conversation.CHANNEL, channel);
        extra.putString(Conversation.KICKERNICK, kickerNick);
        extra.putString(Conversation.KICKERLOGIN, kickerLogin);
        extra.putString(Conversation.KICKERHOST, kickerHostname);
        extra.putString(Conversation.RECIPIENT, recipientNick);
        extra.putString(Conversation.REASON, reason);
        // Create user-oriented intent
        Intent intent = IRCBroadcastHandler.createConversationIntent(
                IRCBroadcastHandler.CONVERSATION_REMOVE);
        intent.putExtras(extra);
        mService.sendBroadcast(intent);
        // Send a server update
        Intent serverIntent = IRCBroadcastHandler
                .createServerIntent(IRCBroadcastHandler.SERVER_UPDATE);
        serverIntent.putExtra(Server.MESSAGE_TYPE, Server.MESSAGE_KICK);
        serverIntent.putExtras(extra);
        mService.sendBroadcast(serverIntent);
    }
    @Override
    protected void onTopic(String target, String topic, String setBy, long date, boolean changed) {
        Log.d(ID, "Received topic change: " + topic);
        Bundle extra = new Bundle();
        extra.putString(Conversation.TARGET, target);
        extra.putString(Conversation.TOPIC, topic);
        extra.putString(Conversation.SETBY, setBy);
        extra.putLong(Conversation.DATE, date);
        extra.putBoolean(Conversation.CHANGED, changed);
        // Create user-oriented intent
        Intent intent = IRCBroadcastHandler.createConversationIntent(
                IRCBroadcastHandler.CONVERSATION_TOPIC);
        intent.putExtras(extra);
        mService.sendBroadcast(intent);
        // Send a server update
        Intent serverIntent = IRCBroadcastHandler
                .createServerIntent(IRCBroadcastHandler.SERVER_UPDATE);
        serverIntent.putExtra(Server.MESSAGE_TYPE, Server.MESSAGE_TOPIC);
        serverIntent.putExtras(extra);
        mService.sendBroadcast(serverIntent);
    }
    @Override
    protected void onPart(String channel, String sender, String login, String hostname) {
        Bundle extra = new Bundle();
        extra.putString(Conversation.TITLE, channel);
        extra.putString(Conversation.SENDER, sender);
        extra.putString(Conversation.LOGIN, login);
        extra.putString(Conversation.HOSTNAME, hostname);
        // Send a server update
        Intent serverIntent = IRCBroadcastHandler
                .createServerIntent(IRCBroadcastHandler.SERVER_UPDATE);
        serverIntent.putExtra(Server.MESSAGE_TYPE, Server.MESSAGE_LEAVE);
        serverIntent.putExtras(extra);
        mService.sendBroadcast(serverIntent);
    }
    @Override
    protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname,
                          String reason) {
        Bundle extra = new Bundle();
        extra.putString(Conversation.SENDER, sourceNick);
        extra.putString(Conversation.LOGIN, sourceLogin);
        extra.putString(Conversation.HOSTNAME, sourceHostname);
        extra.putString(Conversation.REASON, reason);
        // Send a server update
        Intent serverIntent = IRCBroadcastHandler
                .createServerIntent(IRCBroadcastHandler.SERVER_UPDATE);
        serverIntent.putExtra(Server.MESSAGE_TYPE, Server.MESSAGE_QUIT);
        serverIntent.putExtras(extra);
        mService.sendBroadcast(serverIntent);
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
    @Override
    protected void onInvite(String targetNick, String sourceNick, String sourceLogin,
                            String sourceHostname, String channel) {
        // TODO
    }
}
