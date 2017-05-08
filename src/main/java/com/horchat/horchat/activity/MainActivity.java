package com.horchat.horchat.activity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
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
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.horchat.horchat.R;
import com.horchat.horchat.adapter.DrawerListAdapter;
import com.horchat.horchat.db.DatabaseHelper;
import com.horchat.horchat.irc.IRCBinder;
import com.horchat.horchat.irc.IRCBroadcastHandler;
import com.horchat.horchat.irc.IRCService;
import com.horchat.horchat.listener.ConversationListener;
import com.horchat.horchat.listener.SessionListener;
import com.horchat.horchat.model.Conversation;
import com.horchat.horchat.model.DrawerEntry;
import com.horchat.horchat.model.DrawerItem;
import com.horchat.horchat.model.DrawerSection;
import com.horchat.horchat.model.Server;
import com.horchat.horchat.model.Session;
import com.horchat.horchat.receiver.ConversationReceiver;
import com.horchat.horchat.receiver.SessionReceiver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements ServiceConnection, SessionListener, ConversationListener {

    public static final String ID = "horchat";

    public static final String ID_SESSION = "session";
    public static final String SERVER = "MainActivity__server";
    public static final String ACCOUNT = "MainActivity__account";

    public static final int PICK_CHANNEL_REQUEST = 101;
    public static final int PICK_USER_REQUEST = 102;

    private Session mSession;
    private DatabaseHelper mDb;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private SessionReceiver mSessionReceiver;
    private ConversationReceiver mConversationReceiver;

    private IRCBinder mBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Set up layouts
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // Configure toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        // Configure navigation drawer
        /*
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override public boolean onNavigationItemSelected(MenuItem menuItem) {
                return true;
            }
        });
        */
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_nav_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        //mServiceConnection.onServiceDisconnected(null);
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
        ircServiceIntent.setAction(IRCService.ACTION_FOREGROUND);
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
        if (server.getStatus() == Server.STATUS_PRECONNECTING) {
            server.setStatus(Server.STATUS_CONNECTING);
            service.connect(mSession);
        } else {
            onStatusUpdate();
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
            case R.id.action_settings:
                /* Open a new activity for settings */
                Log.d(ID, "Toolbar: Opening settings activity");
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.action_logout:
                /* Perform logout of the user */
                Log.d(ID, "Toolbar: Logging out");
                Toast.makeText(getApplicationContext(), R.string.toast_logout, Toast.LENGTH_SHORT).show();
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
                    String channel = data.getStringExtra(PickChannelActivity.PICK);
                    pickChannel(channel);
                }
                break;
            default:
        }
    }

    /* Populate navigation drawer */
    private void populateDrawer() {
        LayoutInflater inflater = getLayoutInflater();
        // Channels
        List<DrawerItem> drawerItemList = new ArrayList<DrawerItem>();
        drawerItemList.add(new DrawerSection(getResources().getString(R.string.navigation_channels)));
        // Populate channel list
        Collection<String> channelConv = mSession.getConversationNamesByType(Conversation.TYPE_CHANNEL);
        if (channelConv != null) {
            for (String channelName: channelConv) {
                drawerItemList.add(new DrawerEntry(channelName, 0));
            }
        }
        drawerItemList.add(new DrawerEntry(getResources().getString(R.string.navigation_joinChannel), R.drawable.ic_menu_allfriends));
        // Private messages
        drawerItemList.add(new DrawerSection(getResources().getString(R.string.navigation_pms)));
        // Populate private messages (by default no PM open)
        Collection<String> userConv = mSession.getConversationNamesByType(Conversation.TYPE_USER);
        if (userConv != null) {
            for (String username: userConv) {
                drawerItemList.add(new DrawerEntry(username, 0));
            }
        }
        drawerItemList.add(new DrawerEntry(getResources().getString(R.string.navigation_sendPrivateMessage), R.drawable.ic_menu_start_conversation));
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
                // Clicked on "Open new conversation"
                pickUserDialog();
            } else {
                Log.w(ID, "Wrong item clicked: " + itemName);
            }
        } else {
            Log.d(ID, "Clicked on item: " + itemName);
            openConversation(mSession.getConversation(itemName));
            mDrawerLayout.closeDrawers();
        }
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
        mSession.newConversation(name, Conversation.TYPE_USER);
        mDrawerLayout.closeDrawers();
        populateDrawer();
        // Open the new conversation
        openConversation(mSession.getConversation(name));
    }

    /* Validate channel with server and change view to channel */
    public void pickChannel(final String name) {
        // TODO: Support channels with keys
        new Thread() {
            @Override
            public void run() {
                mBinder.getService().getClient(mSession).joinChannel(name);
            }
        }.start();
        // TODO: Add some sort of callback
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
        String message = null;
        if (!mSession.hasConversation(name)) {
            message = getString(R.string.pickChannel_notJoined) + " " + name;
        } else {
            message = getString(R.string.pickChannel_joined) + " " + name;
            mDrawerLayout.closeDrawers();
            populateDrawer();
            openConversation(mSession.getConversation(name));
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /* Opens a conversation */
    public void openConversation(final Conversation conversation) {
        if (conversation != null) {
            new Thread() {
                @Override
                public void run() {
                    if (conversation.getType() == conversation.TYPE_USER) {
                        mBinder.getService().getClient(mSession).sendCTCPCommand(conversation.getName(), "PING");
                        mBinder.getService().getClient(mSession).sendCTCPCommand(conversation.getName(), "FINGER");
                        mBinder.getService().getClient(mSession).sendCTCPCommand(conversation.getName(), "VERSION");
                    }
                    mBinder.getService().getClient(mSession).sendMessage(conversation.getName(), "hola!!!");
                }
            }.start();
            mToolbar.setTitle(conversation.getName());
        } else {
            Toast.makeText(getApplicationContext(), R.string.toast_notOpenConversation,
                    Toast.LENGTH_SHORT).show();
        }
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
        //mBinder.getService().getClient().disconnect();
        Intent ircServiceIntent = new Intent(this, IRCService.class);
        stopService(ircServiceIntent);
        /* Start the activity */
        startActivity(activityIntent);
        finish();
    }

    public void onConversationMessage(String target) {
        Log.d(ID, "Conversation message: " + target);
    }

    public void onNewConversation(String target) {
        Log.d(ID, "New conversation: " + target);
    }

    public void onRemoveConversation(String target) {
        Log.d(ID, "Remove conversation: " + target);
    }

    public void onTopicChanged(String target) {
        Log.d(ID, "Topic changed: " + target);
    }

    public void onStatusUpdate() {
        Server server = mSession.getServer();
        Log.d(ID, "Status update with connected status " + server.isConnected());
        if (server.isConnected()) {
            // Connection is OK
            Log.d(ID, "Server is OK");
        } else {
            // Connection was lost - Reconnect
            Log.d(ID, "Server is DOWN");
        }
    }

    public void sendMessage() {
        // TODO - Implement sendMessage logic
    }
}
