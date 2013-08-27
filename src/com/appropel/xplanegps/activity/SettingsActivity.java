package com.appropel.xplanegps.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import com.appropel.xplanegps.R;
import com.appropel.xplanegps.thread.UdpReceiverThread;
import roboguice.activity.RoboPreferenceActivity;
import roboguice.inject.InjectPreference;

/**
 * Activity for user preferences.
 */
public final class SettingsActivity extends RoboPreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener

{
    /** Reception port. */
    @InjectPreference("port")
    private EditTextPreference port;

    /** Port forward address. */
    @InjectPreference("forward_address")
    private EditTextPreference forwardAddress;

    /** {@inheritDoc} */
    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

    /** {@inheritDoc} */
    @Override
    protected void onResume()
    {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged(getPreferenceScreen().getSharedPreferences(), "");
    }

    /** {@inheritDoc} */
    @Override
    protected void onPause()
    {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    /** {@inheritDoc} */
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String s)
    {
        // Validate port.
        final String portString = sharedPreferences.getString("port",
                String.valueOf(UdpReceiverThread.DEFAULT_RECEIVE_PORT));
        int portValue;
        try
        {
            portValue = Integer.valueOf(portString);
        }
        catch (final NumberFormatException ex)
        {
            portValue = UdpReceiverThread.DEFAULT_RECEIVE_PORT;
        }
        portValue = Math.max(UdpReceiverThread.MINIMUM_PORT, portValue);
        portValue = Math.min(UdpReceiverThread.MAXIMUM_PORT, portValue);
        port.setText(String.valueOf(portValue));

        // Update on-screen displayed values.
        port.setSummary(sharedPreferences.getString("port", String.valueOf(UdpReceiverThread.DEFAULT_RECEIVE_PORT)));
        forwardAddress.setSummary(sharedPreferences.getString("forward_address", ""));
        forwardAddress.setEnabled(sharedPreferences.getBoolean("enable_udp_forward", false));
    }
}
