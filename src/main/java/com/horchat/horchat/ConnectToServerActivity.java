package com.horchat.horchat;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
