package com.horchat.horchat.activity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.horchat.horchat.R;
import com.horchat.horchat.adapter.TargetListAdapter;
import com.horchat.horchat.irc.IRCBinder;
import com.horchat.horchat.irc.IRCBroadcastHandler;
import com.horchat.horchat.irc.IRCService;
import com.horchat.horchat.listener.SessionListener;
import com.horchat.horchat.model.Channel;
import com.horchat.horchat.model.Server;
import com.horchat.horchat.model.Session;
import com.horchat.horchat.model.TargetItem;
import com.horchat.horchat.receiver.SessionReceiver;

import java.util.ArrayList;
import java.util.List;

public class PickChannelActivity extends AppCompatActivity implements ServiceConnection,
        SessionListener {
    public static final String ID = "Horchat";
    public static final String TYPE = "PickTargetActivity__Type";
    public static final String PICK = "PickTargetActivity__Pick";
    public static final String PICKISNEW = "PickTargetActivity__PickIsNew";
    public static final String SESSION = "PickTargetActivity__Session";
    public static final int TYPE_CHANNEL = 11;
    public static final int TYPE_PRIVATE = 12;

    private ListView mTargetList;
    private Session mSession;
    private IRCBinder mBinder;
    private List<TargetItem> mChannels;
    private SessionReceiver mSessionReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_target);
        // Configure toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        // Configure layout
        mTargetList = (ListView) findViewById(R.id.target_list);
        mTargetList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onTargetItemClick(mTargetList.getAdapter().getItem(position));
            }
        });
        // Determine the type of target we want to pick (channel or user)
        Bundle extras = getIntent().getExtras();
        toolbar.setTitle(getResources().getString(R.string.pickChannel_title));
        mSession = null;
        if (extras != null) {
            mSession = (Session) extras.getSerializable(SESSION);
        }
        if (mSession == null) {
            // The session is invalid - Quit
            finish();
        }
        mChannels = new ArrayList<TargetItem>();
    }
    @Override
    public void onResume() {
        super.onResume();
        // Register receivers
        mSessionReceiver = new SessionReceiver(this);
        registerReceiver(mSessionReceiver, new IntentFilter(IRCBroadcastHandler.SERVER_UPDATE));
        // Bind to the IRC service
        Intent ircServiceIntent = new Intent(this, IRCService.class);
        startService(ircServiceIntent);
        bindService(ircServiceIntent, this, 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unbind the IRC service and unregister broadcast receivers
        unbindService(this);
        unregisterReceiver(mSessionReceiver);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        mBinder = (IRCBinder) binder;
        // Populate target list
        getChannelsFromService();
        populateTargetList();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBinder = null;
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

    /* Populates the target list */
    private void populateTargetList() {
        // First of all, add channels and actions to channel list
        if (mChannels.size() != 0) {
            mChannels.clear();
        }
        for (Channel channel: mBinder.getService().getClient(mSession).getChannelList()) {
            mChannels.add(channel);
        }
        mChannels.add(new TargetItem(getString(R.string.pickChannel_newChannel)));
        // Populate list
        TargetListAdapter listAdapter = new TargetListAdapter(this, mChannels);
        mTargetList.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
    }

    /* Target list clicks */
    private void onTargetItemClick(Object item) {
        TargetItem targetItem = (TargetItem) item;
        if (targetItem != null) {
            String itemName = targetItem.getName();
            if (targetItem.getType() == TargetItem.TYPE_CHANNEL) {
                joinChannel(itemName, false);
            } else {
                // Button clicked - Show dialog for creating new channel
                LayoutInflater inflater = LayoutInflater.from(this);
                View view = inflater.inflate(R.layout.dialog_new_channel, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                final EditText input = (EditText) view.findViewById(R.id.pickChannel_newChannel);
                alertDialogBuilder
                        .setView(view)
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.pickChannel_newChannel_create),
                                new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String name = input.getText().toString();
                                if (Channel.validate(name)) {
                                    joinChannel(name, true);
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            R.string.toast_newChannel, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.pickChannel_newChannel_cancel),
                                new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        } else {
            setResult(RESULT_CANCELED, new Intent());
            finish();
        }
    }

    /* Join channel */
    public void joinChannel(String name, boolean isNew) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(PICK, name);
        resultIntent.putExtra(PICKISNEW, isNew);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    /* Make the Home/Up button work the same way as the Back button */
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void getChannelsFromService() {
        new Thread() {
            @Override
            public void run() {
                mBinder.getService().getClient(mSession).listChannels();
            }
        }.start();
    }

    /* Non-implemented receiver */
    public void onStatusCheck() {
    }

    /* We only use this for getting channels */
    public void onStatusMessage(Bundle args) {
        int type = args.getInt(Server.MESSAGE_TYPE);
        if (type == Server.MESSAGE_CHANNELS) {
            /* We have received the message, so we can update the channel list */
            populateTargetList();
        }
    }
}
