package com.horchat.horchat.irc;

import android.app.Service;
import android.content.Intent;
import android.util.Log;

import com.horchat.horchat.model.Account;
import com.horchat.horchat.model.Conversation;
import com.horchat.horchat.model.Server;
import com.horchat.horchat.model.Session;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IRCService extends Service {
    public static final String ID = "horchat";

    private final IRCBinder mBinder;
    private Server mServer = null;
    private Map<Long, IRCClient> mConnections;
    private List<Conversation> mAutoJoinChannels;

    public IRCService() {
        mBinder = new IRCBinder(this);
        mConnections = new HashMap<Long, IRCClient>();
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(ID, "Service onCreate called");
        sendBroadcast(new Intent(IRCBroadcastHandler.SERVER_UPDATE));
    }
    @Override
    public IRCBinder onBind(Intent intent) {
        Log.d(ID, "Service onBind called");
        return mBinder;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(ID, "Service onStartCommand called");
        if (intent != null) {
            handleCommand(intent);
        }
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
    private void handleCommand(Intent intent) {
        // TODO: Implement
    }
    public synchronized IRCClient getClient(Session session) {
        // TODO: Support multiple sessions at once
        long sessionId = session.getId();
        IRCClient client = mConnections.get(sessionId);
        if (client == null) {
            client = new IRCClient(this, session);
            mConnections.put(sessionId, client);
        }
        return client;
    }
    public void setAutoJoinChannelList(List<Conversation> conversations) {
        mAutoJoinChannels = conversations;
    }
    public List<Conversation> getAutoJoinChannelList() {
        return mAutoJoinChannels;
    }
    public void connect(final Session session) {
        Log.d(ID, "Service connect called");
        final long sessionId = session.getId();
        final IRCService service = this;
        new Thread() {
            @Override
            public void run() {
                if (!session.getServer().allowReconnection()) {
                    return;
                }
                // TODO: Add message if errors
                try {
                    IRCClient client = getClient(session);
                    Account account = session.getAccount();
                    Server server = session.getServer();
                    String serverPassword = null;
                    // Configure client: Name, nickname, etc.
                    client.setUsername(account.getUsername());
                    client.setRealName(account.getRealName());
                    client.setNickname(account.getNickname());
                    // Configure server: SSL, etc.
                    // TODO: Support SSL
                    server.setSSL(false);
                    if (!server.getPassword().isEmpty()) {
                        serverPassword = server.getPassword();
                    }
                    // Send a server update
                    Intent serverIntent = IRCBroadcastHandler
                            .createServerIntent(IRCBroadcastHandler.SERVER_UPDATE);
                    serverIntent.putExtra(Server.MESSAGE_TYPE, Server.MESSAGE_CONNECTING);
                    service.sendBroadcast(serverIntent);
                    Log.d("horchat", "connecting...");
                    // Connect to the server
                    client.connect(server.getHost(), server.getPort(), serverPassword);
                    /* We will store the list of channels later */
                    client.listChannels();
                } catch (Exception e) {
                    Log.d("horchat", "Exception !!! " + e.toString());
                    if (e instanceof NickAlreadyInUseException) {
                        Log.d("horchat", "User already in use");
                        // Nickname is already in use
                        session.getServer().setAllowReconnection(false);
                    } else if (e instanceof IrcException) {
                        Log.d("horchat", "IRC exception");
                        // IRC login error
                        session.getServer().setAllowReconnection(false);
                    } else {
                        Log.d("horchat", "Other exception");
                        // Could not connect to server
                    }
                }
            }
        }.start();
    }
}
