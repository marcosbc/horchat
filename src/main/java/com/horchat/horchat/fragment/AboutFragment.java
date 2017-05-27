package com.horchat.horchat.fragment;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.horchat.horchat.R;

public class AboutFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Enable the app bar
        setHasOptionsMenu(true);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.about);
        // Populate version custom_preference
        Preference versionPreference = findPreference("version");
        if (versionPreference != null) {
            try {
                PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(
                        getActivity().getPackageName(), 0);
                versionPreference.setSummary(packageInfo.versionName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(
                R.string.preference_about);
        // Add on-click listener for URL
        final Preference urlPreference = findPreference("url");
        urlPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String url = getString(R.string.app_url);
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(myIntent);
                return true;
            }
        });
        // Add on-click listener for license
        final Preference licensePreference = findPreference("license");
        licensePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String url = getString(R.string.app_license_url);
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(myIntent);
                return true;
            }
        });
    }
}