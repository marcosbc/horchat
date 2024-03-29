package com.horchat.horchat.activity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.horchat.horchat.R;
import com.horchat.horchat.adapter.DrawerListAdapter;
import com.horchat.horchat.db.DatabaseHelper;
import com.horchat.horchat.fragment.ConversationFragment;
import com.horchat.horchat.irc.IRCBinder;
import com.horchat.horchat.irc.IRCBroadcastHandler;
import com.horchat.horchat.irc.IRCService;
import com.horchat.horchat.listener.ConversationListener;
import com.horchat.horchat.listener.SessionListener;
import com.horchat.horchat.model.Account;
import com.horchat.horchat.model.Conversation;
import com.horchat.horchat.model.DrawerEntry;
import com.horchat.horchat.model.DrawerItem;
import com.horchat.horchat.model.DrawerSection;
import com.horchat.horchat.model.Message;
import com.horchat.horchat.model.Server;
import com.horchat.horchat.model.Session;
import com.horchat.horchat.receiver.ConversationReceiver;
import com.horchat.horchat.receiver.SessionReceiver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements ServiceConnection, SessionListener, ConversationListener {

    public static final String ID = "horchat";

    public static final String CONVERSATION_FRAGMENT = "ConversationFragment";
    public static final String ID_SESSION = "session";
    public static final String SERVER = "MainActivity__server";
    public static final String ACCOUNT = "MainActivity__account";
    public static final String CONVERSATION = "MainActivity__conversation";

    public static final int PICK_CHANNEL_REQUEST = 101;
    public static final int PICK_USER_REQUEST = 102;

    private Session mSession;
    private DatabaseHelper mDb;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private SessionReceiver mSessionReceiver;
    private ConversationReceiver mConversationReceiver;
    private String mCurrentConversation;
    private IRCBinder mBinder;
    private boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Set up layouts
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // Configure toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_nav_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        // Setup the database handler
        mDb = new DatabaseHelper(this);
        // Setup the session if it wasn't defined yet
        if (savedInstanceState != null) {
            /* TODO: Verify */
            Log.i(ID, "Activity has been re-loaded");
            mSession = (Session) savedInstanceState.getSerializable(ID_SESSION);
        } else {
            // Check if the user was already logged in
            mSession = mDb.getCurrentSession();
            if (mSession == null) {
                Log.d(ID, "Creating new account");
                // Check if credentials were sent from login activity
                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                    Log.d(ID, "Creating account with credentials from login activity");
                    mSession = mDb.createSession(extras);
                }
                if (mSession != null) {
                    Log.d(ID, "Account was successfully created");
                }
                // If we got nothing, logout
                else {
                    Log.d(ID, "Account was not created!");
                    // The user has not logged in yet, go to server connection activity
                    logout();
                    return;
                }
            }
        }
        // Inflate navigation drawer list
        mDrawerList = (ListView) findViewById(R.id.drawer_list);
        LayoutInflater inflater = getLayoutInflater();
        // Add drawer header
        ViewGroup drawerHeader = (ViewGroup) inflater.inflate(R.layout.navigation_drawer_header,
                mDrawerList, false);
        mDrawerList.addHeaderView(drawerHeader, null, false);
        // Add onClick listener
        mDrawerList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onNavigationDrawerItemClick(mDrawerList.getAdapter().getItem(position));
            }
        });
        // Update the username in the navigation drawer
        TextView username = (TextView) drawerHeader.findViewById(R.id.navigation_username);
        username.setText(getString(R.string.navigation_username,
                mSession.getAccount().getNickname(), mSession.getAccount().getUsername()));
        isConnected = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register receivers
        mSessionReceiver = new SessionReceiver(this);
        registerReceiver(mSessionReceiver, new IntentFilter(IRCBroadcastHandler.SERVER_UPDATE));
        mConversationReceiver = new ConversationReceiver(mSession.getId(), this);
        registerReceiver(mConversationReceiver, new IntentFilter(IRCBroadcastHandler.CONVERSATION_NEW));
        registerReceiver(mConversationReceiver, new IntentFilter(IRCBroadcastHandler.CONVERSATION_TOPIC));
        registerReceiver(mConversationReceiver, new IntentFilter(IRCBroadcastHandler.CONVERSATION_MESSAGE));
        registerReceiver(mConversationReceiver, new IntentFilter(IRCBroadcastHandler.CONVERSATION_REMOVE));
        // Bind to the IRC service
        Intent ircServiceIntent = new Intent(this, IRCService.class);
        startService(ircServiceIntent);
        bindService(ircServiceIntent, this, 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unbind to the IRC service and unregister broadcast receivers
        unbindService(this);
        unregisterReceiver(mSessionReceiver);
        unregisterReceiver(mConversationReceiver);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        mBinder = (IRCBinder) binder;
        IRCService service = mBinder.getService();
        Server server = mSession.getServer();
        Log.d(ID, "onServiceConnected: Status is: " + server.getStatus());
        /* Check if this is the first connect attempt to IRC server */
        List<Conversation> fullConversationList = mDb.getConversations(mSession.getId());
        if (server.getStatus() == Server.STATUS_PRECONNECTING) {
            server.setStatus(Server.STATUS_CONNECTING);
            service.setAutoJoinChannelList(fullConversationList);
            service.connect(mSession);
        } else {
            onStatusCheck();
        }
        // Populate conversations
        for (Conversation conversation: fullConversationList) {
            if (conversation.getType() != Conversation.TYPE_SERVER) {
                mSession.newConversation(conversation);
            }
        }
        // Set the current conversation
        String currentConversationName = mDb.getSetting(DatabaseHelper.LAST_CONVERSATION_NAME);
        if (currentConversationName != null) {
            mSession.setCurrentConversation(mSession.getConversation(currentConversationName));
        }
        // Open current conversation
        Conversation currentConversation = mSession.getCurrentConversation();
        if (currentConversation == null) {
            currentConversation = mSession.getServerConversation();
        }
        openConversation(currentConversation);
        if (mCurrentConversation == null) {
            mCurrentConversation = currentConversation.getName();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBinder = null;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        /* TODO: Make use of savedInstance (e.g. orientation change) */
        savedInstanceState.putSerializable(ID_SESSION, mSession);
    }

    /* Toolbar configurations */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        /* TODO: Add search functionality */
        /*
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        */
        /* Will always return true */
        return true;
    }

    /* Called each time the menu opens */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        invalidateOptionsMenu();
        if (mSession != null) {
            Conversation currentConversation = mSession.getCurrentConversation();
            /* Make "Close Conversation" button non-visible by default */
            MenuItem item = menu.findItem(R.id.action_closeConversation);
            item.setVisible(false);
            if (currentConversation != null) {
                int titleRes = 0;
                /* Add "Close Channel" if a Channel is the current conversation */
                if (currentConversation.getType() == Conversation.TYPE_CHANNEL) {
                    titleRes = R.string.menu_leaveChannel;
                }
                /* Add "Close Conversation" if a Private Conversation is the current conversation */
                else if (currentConversation.getType() == Conversation.TYPE_USER) {
                    titleRes = R.string.menu_closePrivateConversation;
                }
                if (titleRes != 0) {
                    item.setTitle(titleRes);
                    item.setVisible(true);
                }
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /* Toolbar clicks */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                /* Populate and open navigation drawer */
                Log.d(ID, "Toolbar: Populating navigation drawer");
                populateDrawer();
                Log.d(ID, "Toolbar: Opening navigation drawer");
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.action_options:
                /* Dropdown button will be handled via Views */
                Log.d(ID, "Toolbar: Opening options menu");
                break;
            case R.id.action_closeConversation:
                /* Close current conversation */
                Log.d(ID, "Toolbar: Leaving channel");
                Conversation currentConversation = mSession.getCurrentConversation();
                if (currentConversation != null) {
                    closeConversation(currentConversation.getName());
                }
                break;
            case R.id.action_settings:
                /* Open a new activity for settings */
                Log.d(ID, "Toolbar: Opening settings activity");
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.action_logout:
                /* Perform logout of the user */
                Log.d(ID, "Toolbar: Logging out");
                Toast.makeText(getApplicationContext(), R.string.toast_logout, Toast.LENGTH_SHORT)
                        .show();
                logout();
                break;
            default:
                Log.d(ID, "Toolbar: Other button clicked, *ignoring*");
        }
        return true;
    }

    /* Called when getting result from an activity (e.g. navigation drawer menu) */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICK_CHANNEL_REQUEST:
                if (resultCode == RESULT_OK) {
                    mDrawerLayout.closeDrawers();
                    String channel = data.getStringExtra(PickChannelActivity.PICK);
                    pickChannel(channel);
                }
                break;
            default:
        }
    }

    // A method to find height of the status bar
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /* Populate navigation drawer */
    private void populateDrawer() {
        LayoutInflater inflater = getLayoutInflater();
        List<DrawerItem> drawerItemList = new ArrayList<DrawerItem>();
        // Conversation with server (for information messages)
        String serverName = mSession.getServerConversation().getName();
        drawerItemList.add(new DrawerEntry(serverName, R.drawable.ic_menu_server,
                mSession.isConversationRead(serverName, Conversation.TYPE_SERVER),
                mSession.isConversationSelected(serverName, Conversation.TYPE_SERVER)));
        // Channels
        drawerItemList.add(new DrawerSection(getResources().getString(R.string.navigation_channels)));
        // Populate channel list
        Collection<String> channels =
                mSession.getConversationNamesByType(Conversation.TYPE_CHANNEL);
        if (channels != null) {
            for (String channelName: channels) {
                drawerItemList.add(new DrawerEntry(channelName, 0,
                        mSession.isConversationRead(channelName, Conversation.TYPE_CHANNEL),
                        mSession.isConversationSelected(channelName, Conversation.TYPE_CHANNEL)));
            }
        }
        drawerItemList.add(new DrawerEntry(getResources().getString(R.string.navigation_joinChannel), R.drawable.ic_menu_join_channel));
        // Private messages
        drawerItemList.add(new DrawerSection(getResources().getString(R.string.navigation_pms)));
        // Populate private messages (by default no PM open)
        Collection<String> userConv = mSession.getConversationNamesByType(Conversation.TYPE_USER);
        if (userConv != null) {
            for (String nickname: userConv) {
                drawerItemList.add(new DrawerEntry(nickname, 0,
                        mSession.isConversationRead(nickname, Conversation.TYPE_USER),
                        mSession.isConversationSelected(nickname, Conversation.TYPE_USER)));
            }
        }
        drawerItemList.add(new DrawerEntry(getString(R.string.navigation_sendPrivateMessage),
                R.drawable.ic_menu_user_conversation));
        // Set list adapter
        mDrawerList.setAdapter(new DrawerListAdapter(getApplicationContext(), drawerItemList));
    }

    /* Navigation drawer item click */
    private void onNavigationDrawerItemClick(Object item) {
        DrawerEntry drawerEntry = (DrawerEntry) item;
        String itemName = drawerEntry.getItemName();
        if (drawerEntry.hasIcon()) {
            if (itemName.equals(
                    getResources().getString(R.string.navigation_joinChannel))) {
                // Clicked on "Join channel"
                Intent activityIntent = new Intent(this, PickChannelActivity.class);
                activityIntent.putExtra(PickChannelActivity.SESSION, mSession);
                startActivityForResult(activityIntent, PICK_CHANNEL_REQUEST);
            } else if (itemName.equals(
                    getResources().getString(R.string.navigation_sendPrivateMessage))) {
                /* Clicked on "Open new conversation" */
                mDrawerLayout.closeDrawers();
                pickUserDialog();
            } else {
                /* Server conversation clicked */
                mDrawerLayout.closeDrawers();
                openConversation(mSession.getServerConversation());
            }
        } else {
            Log.d(ID, "Clicked on item: " + itemName);
            /* Channel/private conversation clicked */
            mDrawerLayout.closeDrawers();
            openConversation(mSession.getConversation(itemName));
        }
    }

    /* Join a channel */
    private void joinChannel(final String name) {
        // TODO: Support password-protected channels
        // TODO: Detect channel ban
        Log.d(ID, "Joining channel " + name);
        new Thread() {
            @Override
            public void run() {
                mBinder.getService().getClient(mSession).joinChannel(name);
            }
        }.start();
    }

    /* Leave a channel */
    private void closeConversation(final String name) {
        Log.d(ID, "Leaving channel " + name);
        new Thread() {
            @Override
            public void run() {
                mBinder.getService().getClient(mSession).partChannel(name);
            }
        }.start();
        /* Open the server conversation */
        openConversation(mSession.getServerConversation());
        /* Remove the conversation from the list */
        mSession.closeConversation(name);
        mDb.closeConversation(name, mSession.getId());
        populateDrawer();
    }

    /* Dialog for when a user has been disconnected from the server */
    private void userDisconnectDialog() {
        LayoutInflater inflater = getLayoutInflater();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setTitle(R.string.userDisconnect_title)
                .setMessage(getString(R.string.userDisconnect_message,
                        mSession.getServer().getHost()))
                .setCancelable(false)
                .setNeutralButton(getString(android.R.string.ok),
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        logout();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /* Dialog for choosing a username */
    public void pickUserDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_pick_user, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        final EditText input = (EditText) view.findViewById(R.id.pickUser);
        alertDialogBuilder
                .setView(view)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.pickUser_choose),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pickUser(input.getText().toString());
                    }
                })
                .setNegativeButton(getString(R.string.pickUser_cancel),
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /* Validate user with server and change view to private message */
    public void pickUser(String name) {
        Toast.makeText(getApplicationContext(), "Selected user " + name, Toast.LENGTH_SHORT).show();
        // TODO: Check if user exists
        Conversation conversation = createConversation(name, Conversation.TYPE_USER);
        // Open the new conversation
        openConversation(conversation);
    }

    /* Validate channel with server and change view to channel */
    public void pickChannel(String name) {
        Conversation conversation = mSession.getConversation(name);
        if (conversation != null) {
            /* Channel was already joined */
            openConversation(conversation);
        } else{
            mCurrentConversation = name;
            joinChannel(name);
        }
    }

    /* Opens a conversation */
    public void openConversation(Conversation conversation) {
        if (conversation != null) {
            /* Set current conversation */
            mToolbar.setTitle(conversation.getName());
            // TODO: Set in db
            mSession.setCurrentConversation(conversation);
            /* Create and attach fragment */
            Bundle args = new Bundle();
            args.putSerializable(CONVERSATION, conversation);
            /* Instantiate Conversation fragment */
            ConversationFragment conversationFragment = new ConversationFragment();
            conversationFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content, conversationFragment, CONVERSATION_FRAGMENT);
            transaction.commit();
            /* Mark conversation as read */
            conversation.markAsRead();
            /* Set as current conversation in the database */
            mDb.setSetting(DatabaseHelper.LAST_CONVERSATION_NAME, conversation.getName());
        } else {
            Toast.makeText(getApplicationContext(), R.string.toast_notOpenConversation,
                    Toast.LENGTH_SHORT).show();
        }
        populateDrawer();
    }

    /* Logs a user out, and starts the login-related activity */
    public void logout() {
        Log.d(ID, "Logging out...");
        Intent activityIntent = new Intent(this, ConnectToServerActivity.class);
        /* Clear navigation history */
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        /* Remove login mark in settings */
        if (mDb != null) {
            mDb.logout();
            // TODO: Stop the service
        }
        /* Stop the IRC service */
        if (mSession != null) {
            // TODO: Add server disconnection logic
            mSession.getServer().setAllowReconnection(false);
        }
        /* Disconnect from the server */
        new Thread() {
            @Override
            public void run() {
                if (mBinder != null) {
                    mBinder.getService().getClient(mSession).disconnect();
                }
            }
        }.start();
        Intent ircServiceIntent = new Intent(this, IRCService.class);
        stopService(ircServiceIntent);
        /* Start the activity */
        startActivity(activityIntent);
        finish();
    }

    public void sendMessage(final String text) {
        if (text == null || text.equals("")) {
            return;
        }
        Conversation conversation = mSession.getCurrentConversation();
        if (conversation != null && conversation != mSession.getServerConversation()) {
            String sender = mSession.getAccount().getNickname();
            final String destination = conversation.getName();
            final int type = conversation.getType();
            new Thread() {
                @Override
                public void run() {
                    mBinder.getService().getClient(mSession).sendMessage(destination, text);
                }
            }.start();
            conversation.addMessage(new Message(text, sender, new Date()));
            refreshMessageList();
        }
    }

    private void refreshMessageList() {
        ConversationFragment fragment =
                (ConversationFragment) getSupportFragmentManager().findFragmentById(R.id.content);
        if (fragment != null) {
            fragment.refreshMessageList();
        }
    }

    public Conversation createConversation(String title, int type) {
        /* Create conversation if it doesn't exist */
        Conversation conversation = mSession.getConversation(title);
        if (conversation == null) {
            /* The conversation did not exist before, create and add to the database */
            conversation = mSession.newConversation(title, type);
            mDb.newConversation(conversation, mSession.getId());
        }
        return conversation;
    }

    public void onConversationMessage(Bundle args) {
        Log.d(ID, "Conversation message");
        String title = args.getString(Conversation.TITLE);
        String sender = args.getString(Conversation.SENDER);
        String text = args.getString(Conversation.MESSAGE);
        Conversation conversation = mSession.getConversation(title);
        if (conversation != null) {
            conversation.addMessage(new Message(text, sender, new Date()));
            /* If not the current conversation on the device, mark as unread */
            if (mSession.getCurrentConversation() != conversation) {
                conversation.markAsUnread();
            }
            populateDrawer();
        }
        refreshMessageList();
    }

    public void onNewConversation(Bundle args) {
        Log.d(ID, "New conversation");
        /* Create the new conversation */
        int type = args.getInt(Conversation.TYPE);
        String title = args.getString(Conversation.TITLE);
        String sender = args.getString(Conversation.SENDER);
        String text = args.getString(Conversation.MESSAGE);
        /* Create the new conversation */
        Conversation conversation = createConversation(title, type);
        populateDrawer();
        if (text != null) {
            /* If we received a message with the action */
            onConversationMessage(args);
        }
        /* Check if we were opening the conversation on our side */
        Account account = mSession.getAccount();
        if (account.getNickname().equals(sender) && title.equals(mCurrentConversation)) {
            openConversation(conversation);
        }
    }

    public void onRemoveConversation(Bundle args) {
        Log.d(ID, "Remove conversation");
        String channel = args.getString(Conversation.CHANNEL);
        String kickerNick = args.getString(Conversation.KICKERNICK);
        String kickerLogin = args.getString(Conversation.KICKERLOGIN);
        String kickerHost = args.getString(Conversation.KICKERHOST);
        String recipient = args.getString(Conversation.RECIPIENT);
        String reason = args.getString(Conversation.REASON);
        if (recipient.equals(mSession.getAccount().getNickname())) {
            /* We were kicked from the channel */
            /* If we were on that conversation, change to the default one */
            if (mSession.getCurrentConversation() == mSession.getConversation(channel)) {
                openConversation(mSession.getServerConversation());
            }
            /* Remove the object */
            mSession.closeConversation(channel);
            populateDrawer();
        }
    }

    public void onTopicChanged(Bundle args) {
        Log.d(ID, "Topic changed");
        /* TODO */
    }

    public void onStatusCheck() {
        Server server = mSession.getServer();
        if (server.isConnected()) {
            // Connection is OK
            Log.d(ID, "Server is OK");
            isConnected = true;
        } else {
            // Connection was lost
            // TODO: Add logic for enabling reconnection
            Log.d(ID, "Server is DOWN");
            if (isConnected) {
                /* User was previously connected */
                userDisconnectDialog();
            }
            isConnected = false;
        }
    }

    public void onStatusMessage(Bundle args) {
        Server server = mSession.getServer();
        /* Parse any server message */
        int type = args.getInt(Server.MESSAGE_TYPE);
        String msg = null;
        String user = null;
        String channel = null;
        switch (type) {
            case Server.MESSAGE_CONNECTING:
                msg = getString(R.string.status_connecting,
                        mSession.getServerConversation().getName());
                server.setStatus(Server.STATUS_CONNECTING);
                break;
            case Server.MESSAGE_CONNECTED:
                msg = getString(R.string.status_connected,
                        mSession.getServerConversation().getName());
                server.setStatus(Server.STATUS_CONNECTED);
                break;
            case Server.MESSAGE_DISCONNECTED:
                msg = getString(R.string.status_disconnected,
                        mSession.getServerConversation().getName());
                server.setStatus(Server.STATUS_DISCONNECTED);
                break;
            case Server.MESSAGE_JOIN:
                user = args.getString(Conversation.SENDER);
                channel = args.getString(Conversation.TITLE);
                msg = getString(R.string.status_join, user, channel);
                break;
            case Server.MESSAGE_LEAVE:
                user = args.getString(Conversation.SENDER);
                channel = args.getString(Conversation.TITLE);
                msg = getString(R.string.status_leave, user, channel);
                break;
            case Server.MESSAGE_KICK:
                user = args.getString(Conversation.KICKERNICK);
                channel = args.getString(Conversation.CHANNEL);
                msg = getString(R.string.status_kick, user, channel);
                break;
            case Server.MESSAGE_QUIT:
                user = args.getString(Conversation.SENDER);
                msg = getString(R.string.status_quit, user,
                        mSession.getServerConversation().getName());
                break;
            /* Methods not in use */
            case Server.MESSAGE_TOPIC:
            default:
                /* Don't do anything more in this method */
                return;
        }
        if (msg != null) {
            Conversation conversation = null;
            // Message to be added to a channel
            if (channel != null) {
                conversation = mSession.getConversation(channel);
            }
            // Message to be added to the server conversation
            if (conversation == null) {
                conversation = mSession.getServerConversation();
            }
            conversation.addMessage(new Message(msg, null, new Date(), Message.TYPE_SERVER));
            refreshMessageList();
        }
    }
}
