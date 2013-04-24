package com.appropel.xplanegps.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import com.appropel.xplanegps.R;
import roboguice.activity.RoboPreferenceActivity;
import roboguice.inject.InjectPreference;

public class SettingsActivity extends RoboPreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener

{
    @InjectPreference("forward_address")
    private EditTextPreference forwardAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    /** {@inheritDoc} */
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String s)
    {
        forwardAddress.setSummary(sharedPreferences.getString("forward_address", ""));
        forwardAddress.setEnabled(sharedPreferences.getBoolean("enable_udp_forward", false));
    }
}
