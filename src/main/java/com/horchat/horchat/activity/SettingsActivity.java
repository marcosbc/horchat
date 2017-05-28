package com.horchat.horchat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.horchat.horchat.R;
import com.horchat.horchat.fragment.AboutFragment;
import com.horchat.horchat.fragment.SettingsFragment;

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
        toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        Fragment fragment = null;
        // Get intent action
        Intent intent = getIntent();
        String action = intent.getAction();
        if (action != null && action.equals("com.horchat.horchat.PREFS_ABOUT")) {
            fragment = new AboutFragment();
        } else {
            fragment = new SettingsFragment();
        }
        // Display the fragment as the main content.
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, fragment)
                .commit();
    }

    /* Make the Home/Up button work the same way as the Back button */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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
}