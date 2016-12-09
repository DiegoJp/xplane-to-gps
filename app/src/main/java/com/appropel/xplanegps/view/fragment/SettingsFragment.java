package com.appropel.xplanegps.view.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.widget.TextView;

import com.appropel.xplanegps.R;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;

import java.util.List;

/**
 * Activity for user preferences.
 */
public final class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener, Validator.ValidationListener
{
    /** X-Plane version. */
//    @InjectPreference("xplane_version")
    private ListPreference xplaneVersion;
//
    /** Broadcast subnet checkbox. */
//    @InjectPreference("broadcast_subnet")
    private CheckBoxPreference broadcastSubnet;
//
    /** Simulator IP address. */
//    @InjectPreference("sim_address")
    private EditTextPreference simulatorAddress;
//
    /** Reception port. */
//    @InjectPreference("port")
    private EditTextPreference port;
//
    /** Port forward address. */
//    @InjectPreference("forward_address")
    private EditTextPreference forwardAddress;

    /** Validator. */
    private Validator validator;

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        validator = new Validator(this);
        validator.setValidationListener(this);
//        validator.put(simulatorAddress.getEditText(),
//                Rules.regex(getString(R.string.sim_invalid), Rules.REGEX_IP_ADDRESS, true));
//        validator.put(port.getEditText(), Rules.gt(getString(R.string.port_gt), 1023));
//        validator.put(port.getEditText(), Rules.lt(getString(R.string.port_lt), 65536));
//        validator.put(forwardAddress.getEditText(),
//                Rules.regex(getString(R.string.forward_invalid), Rules.REGEX_IP_ADDRESS, true));
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
//        updatePreferenceSummary();

        // Store current tab.
//        final SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
//        sharedPreferences.edit().putString(TAB_TAG_KEY, SETTINGS_TAB_TAG).apply();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String shared)
    {
        validator.validate();
        updatePreferenceSummary();
    }

    /**
     * Update displayed value of preferences.
     */
    private void updatePreferenceSummary()
    {
        final SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        xplaneVersion.setSummary(sharedPreferences.getString("xplane_version", ""));
        broadcastSubnet.setEnabled(sharedPreferences.getBoolean("autoconfigure", false));
        simulatorAddress.setSummary(sharedPreferences.getString("sim_address", ""));
        simulatorAddress.setEnabled(sharedPreferences.getBoolean("autoconfigure", false)
                && !sharedPreferences.getBoolean("broadcast_subnet", false));
//        port.setSummary(sharedPreferences.getString("port", String.valueOf(UdpReceiverThread.DEFAULT_PORT)));
        forwardAddress.setSummary(sharedPreferences.getString("forward_address", ""));
        forwardAddress.setEnabled(sharedPreferences.getBoolean("enable_udp_forward", false));
    }

    /**
     * Might be obsolete.
     */
    public void preValidation()
    {
        // These values must be updated or they are not always current and cause spurious validation failures.
        simulatorAddress.getEditText().setText(simulatorAddress.getText(), TextView.BufferType.NORMAL);
        port.getEditText().setText(port.getText(), TextView.BufferType.NORMAL);
        forwardAddress.getEditText().setText(forwardAddress.getText(), TextView.BufferType.NORMAL);
    }

    @Override
    public void onValidationSucceeded()
    {
        // Not used.
    }

    @Override
    public void onValidationFailed(final List<ValidationError> errors)
    {
//        // Reset failed value to default.
//        if (simulatorAddress.getEditText().equals(failedView))
//        {
//            simulatorAddress.setText("127.0.0.1");
//        }
//        else if (port.getEditText().equals(failedView))
//        {
////            port.setText(String.valueOf(UdpReceiverThread.DEFAULT_PORT));
//        }
//        else if (forwardAddress.getEditText().equals(failedView))
//        {
//            forwardAddress.setText("127.0.0.1");
//        }
//
//        // Show dialog alerting user to validation failure.
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setMessage(failedRule.getMessage(getActivity()))
//                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                    public void onClick(final DialogInterface dialogInterface, final int i)
//                    {
//                        dialogInterface.dismiss();
//                    }
//                });
//        builder.show();
    }
}
