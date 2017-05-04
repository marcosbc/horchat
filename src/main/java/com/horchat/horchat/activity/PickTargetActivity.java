package com.horchat.horchat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.horchat.horchat.R;
import com.horchat.horchat.adapter.TargetListAdapter;
import com.horchat.horchat.model.TargetItem;

import java.util.ArrayList;
import java.util.List;

public class PickTargetActivity extends AppCompatActivity {
    public static final String ID = "Horchat";
    public static final String TYPE = "PickTargetActivity__Type";
    public static final String PICK = "PickTargetActivity__Pick";
    public static final int TYPE_CHANNEL = 11;
    public static final int TYPE_PRIVATE = 12;

    private ListView mTargetList;
    private int mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_target);
        // Configure toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        mType = 0;
        if (extras != null) {
            mType = extras.getInt(TYPE);
            if (mType == TYPE_CHANNEL) {
                toolbar.setTitle(getResources().getString(R.string.title_activity_pickChannel));
                populateTargetList(getChannelNames());
            } else if (mType == TYPE_PRIVATE) {
                toolbar.setTitle(getResources().getString(R.string.title_activity_pickUser));
                populateTargetList(getUsernames());
            }
        }
        if (mType == 0) {
            // Nothing to do here
            Log.e(ID, "Target type was not properly specified!");
            finish();
        }
    }

    /* Populates the target list */
    private void populateTargetList(List<TargetItem> targetList) {
        if (targetList != null) {
            mTargetList.setAdapter(new TargetListAdapter(this, targetList));
        }
    }

    /* Target list clicks */
    private void onTargetItemClick(Object item) {
        TargetItem targetItem = (TargetItem) item;
        Intent resultIntent = new Intent();
        if (targetItem != null) {
            String itemName = targetItem.getTargetName();
            Log.d(ID, "Clicked on item: " + itemName);
            // Finish activity with result
            resultIntent.putExtra(TYPE, mType);
            resultIntent.putExtra(PICK, itemName);
            setResult(RESULT_OK, resultIntent);
        } else {
            setResult(RESULT_CANCELED, resultIntent);
        }
        finish();
    }

    /* Make the Home/Up button work the same way as the Back button */
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /* Get list of channel names available */
    public List<TargetItem> getChannelNames() {
        List<TargetItem> channelNames = new ArrayList<TargetItem>();
        channelNames.add(new TargetItem("Channel 1"));
        channelNames.add(new TargetItem("Channel 2"));
        channelNames.add(new TargetItem("Channel 3"));
        channelNames.add(new TargetItem("Channel 4"));
        channelNames.add(new TargetItem("Channel 5"));
        return channelNames;
    }

    /* Get list of channel names available */
    public List<TargetItem> getUsernames() {
        List<TargetItem> usernames = new ArrayList<TargetItem>();
        usernames.add(new TargetItem("Account 1"));
        usernames.add(new TargetItem("Account 2"));
        usernames.add(new TargetItem("Account 3"));
        return usernames;
    }
}
