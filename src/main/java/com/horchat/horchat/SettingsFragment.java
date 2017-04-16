package com.horchat.horchat;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable the app bar
        setHasOptionsMenu(true);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);
    }
}