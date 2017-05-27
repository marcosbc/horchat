package com.horchat.horchat.fragment;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.horchat.horchat.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Enable the app bar
        setHasOptionsMenu(true);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(
                R.string.preference_title);
    }
}