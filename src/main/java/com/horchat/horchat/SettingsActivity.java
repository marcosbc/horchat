package com.horchat.horchat;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatActivity {
    private static final String ID = "horchat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.content, new SettingsFragment())
                .commit();
    }

    /* Make the Home/Up button work the same way as the Back button */
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}