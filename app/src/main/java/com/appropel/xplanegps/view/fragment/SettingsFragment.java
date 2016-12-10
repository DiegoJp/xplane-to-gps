package com.appropel.xplanegps.view.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import com.appropel.xplanegps.R;
import com.appropel.xplanegps.common.util.Expressions;
import com.appropel.xplanegps.dagger.DaggerWrapper;
import com.appropel.xplanegps.model.Preferences;

import javax.inject.Inject;

/**
 * Activity for user preferences.
 */
public final class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener
{
    /** Key for shared pref. */
    public static final String PREF_VALUE = "settings";

    /** X-Plane version. */
    private ListPreference xplaneVersion;

    /** Broadcast subnet checkbox. */
    private CheckBoxPreference broadcastSubnet;

    /** Simulator IP address. */
    private EditTextPreference simulatorAddress;

    /** Reception port. */
    private EditTextPreference port;

    /** Port forward address. */
    private EditTextPreference forwardAddress;

    /** Preferences. */
    @Inject
    Preferences preferences;

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        addPreferencesFromResource(R.xml.settings);

        DaggerWrapper.INSTANCE.getDaggerComponent().inject(this);

        // Find preferences the hard way.
        xplaneVersion = (ListPreference) findPreference("xplane_version");
        broadcastSubnet = (CheckBoxPreference) findPreference("broadcast_subnet");
        simulatorAddress = (EditTextPreference) findPreference("sim_address");
        port = (EditTextPreference) findPreference("port");
        forwardAddress = (EditTextPreference) findPreference("forward_address");
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        updatePreferenceSummary();
    }

    @Override
    public void onPause()
    {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String shared)
    {
        validate();
        updatePreferenceSummary();
    }

    /**
     * Update displayed value of preferences.
     */
    private void updatePreferenceSummary()
    {
        xplaneVersion.setSummary(preferences.getXplaneVersion());
        broadcastSubnet.setEnabled(preferences.isAutoconfigure());
        simulatorAddress.setSummary(preferences.getSimulatorAddress());
        simulatorAddress.setEnabled(preferences.isAutoconfigure() && !preferences.isBroadcastSubnet());
        port.setSummary(preferences.getReceivePort());
        forwardAddress.setSummary(preferences.getForwardAddress());
        forwardAddress.setEnabled(preferences.isUdpForward());
    }

    /**
     * Validates the preference values and shows an alert dialog if there is a problem.
     */
    private void validate()
    {
        if (!Expressions.isValidIpAddress(preferences.getSimulatorAddress()))
        {
            showAlertDialog(getString(R.string.sim_invalid));
            preferences.setSimulatorAddress(getString(R.string.localhost));
        }
        
        final int port = Integer.valueOf(preferences.getReceivePort());
        if (port < 1024)
        {
            showAlertDialog(getString(R.string.port_gt));
            preferences.setReceivePort(getString(R.string.default_port));
        }
        else if (port > 65535)
        {
            showAlertDialog(getString(R.string.port_lt));
            preferences.setReceivePort(getString(R.string.default_port));
        }

        if (!Expressions.isValidIpAddress(preferences.getForwardAddress()))
        {
            showAlertDialog(getString(R.string.forward_invalid));
            preferences.setForwardAddress(getString(R.string.localhost));
        }
    }

    /**
     * Show dialog alerting user to validation failure.
     * @param message message.
     */
    private void showAlertDialog(final String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                {
                    public void onClick(final DialogInterface dialogInterface, final int which)
                    {
                        dialogInterface.dismiss();
                    }
                });
        builder.show();
    }
}
