package com.horchat.horchat.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.horchat.horchat.R;
import com.horchat.horchat.fragment.ServerCredentialsFragment;

public class ConnectToServerActivity extends AppCompatActivity {

    private static final String ID = "horchat";

    private TextView serverHost;
    private TextView serverPort;
    private TextView username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        // Configure toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Workaround not to show the title
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        /* Configure fragment container */
        if (savedInstanceState != null) {
            /* Avoid fragments overlap */
            return;
        }
        /* Start the initial fragment (server credentials ask/confirm) */
        ServerCredentialsFragment serverCredentialsFragment = new ServerCredentialsFragment();
        serverCredentialsFragment.setArguments(getIntent().getExtras());
        /* Add fragment to the FrameLayout */
        getSupportFragmentManager().beginTransaction()
                .add(R.id.connect_frame, serverCredentialsFragment).commit();
    }
}
