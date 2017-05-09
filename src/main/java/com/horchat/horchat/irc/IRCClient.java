package com.horchat.horchat.irc;

import android.content.Intent;
import android.util.Log;

import com.horchat.horchat.model.Account;
import com.horchat.horchat.model.Channel;
import com.horchat.horchat.model.Conversation;
import com.horchat.horchat.model.Server;
import com.horchat.horchat.model.Session;

import org.jibble.pircbot.PircBot;

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

    /* Class constructor */
    public IRCClient(IRCService service, Session session) {
        Log.d(ID, "IRCClient::IRCClient called");
        mService = service;
        mSession = session;
        mChannelList = new HashMap<String, Channel>();
        // If nick already exists, change to a different one automatically
        setAutoNickChange(true);
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
        Log.d(ID, "Sending message");
        joinChannel("#genetest");
    }
    protected void onChannelInfo(String channel, int userCount, String topic) {
        Log.d("horchat", "Called onChannelInfo: " + channel + ", " + userCount + ", " + topic);
        Channel previousChannel = mChannelList.get(channel);
        Channel newChannel = new Channel(channel);
        if (previousChannel != null) {
            // TODO: Set channel attributes: User count, topic...
            //previousChannel.update(newChannel);
        } else {
            mChannelList.put(channel, newChannel);
        }
    }
    protected void onJoin(String channel, String sender, String login, String hostname) {
        // Refresh channel list
        Account account = mSession.getAccount();
        Log.d(ID, "User joined " + channel + ": " + login + " " + sender + " " + hostname);
        if (login.equals(account.getUsername())) {
            Log.d(ID, "It was me!");
            listChannels();
            mSession.newConversation(channel, Conversation.TYPE_CHANNEL);
        }
    }
    public List<Channel> getChannelList() {
        List<Channel> channels = new ArrayList<Channel>();
        SortedSet<String> keys = new TreeSet<String>(mChannelList.keySet());
        for(String key: keys) {
            channels.add(mChannelList.get(key));
        }
        return channels;
    }
    protected void onMessage(String channel, String sender, String login, String hostname,
                             String message) {
        mSession.newConversation(channel, Conversation.TYPE_CHANNEL);
        Log.d(ID, "Got message from " + sender + " in " + channel + ": " + message);
    }
    protected void onPrivateMessage(String sender, String login, String hostname, String message) {
        mSession.newConversation(sender, Conversation.TYPE_USER);
        Log.d(ID, "Got private message from " + sender + ": " + message);
    }
    protected void onFinger(String sourceNick, String sourceLogin, String sourceHostname,
                            String target) {
        Log.d(ID, "Received FINGER: " + sourceNick);
    }
    protected void onPing(String sourceNick, String sourceLogin, String sourceHostname,
                          String target, String pingValue) {
        Log.d(ID, "Received PING: " + sourceNick);
    }
    protected void onVersion(String sourceNick, String sourceLogin, String sourceHostname,
                             String target) {
        Log.d(ID, "Received VERSION: " + sourceNick);
    }
}
