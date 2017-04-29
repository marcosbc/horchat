package com.horchat.horchat.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.horchat.horchat.R;

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