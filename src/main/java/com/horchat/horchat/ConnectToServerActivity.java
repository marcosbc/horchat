package com.horchat.horchat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class ConnectToServerActivity extends AppCompatActivity {

    private static final String ID = "horchat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        // Configure toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Workaround not to show the title
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

}
