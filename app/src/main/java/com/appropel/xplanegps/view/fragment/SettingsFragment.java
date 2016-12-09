package com.appropel.xplanegps.view.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.appropel.xplanegps.R;
import com.appropel.xplanegps.dagger.DaggerWrapper;
import com.appropel.xplanegps.model.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Activity for user preferences.
 */
public final class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener
{
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsFragment.class);

    /** Preferences. */
    @Inject
    Preferences preferences;

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        DaggerWrapper.INSTANCE.getDaggerComponent().inject(this);

        // Set up validation rules.
//        validator = new Validator(this);
//        validator.setValidationListener(this);
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
        updatePreferenceSummary();

        // Store current tab.
//        final SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
//        sharedPreferences.edit().putString(TAB_TAG_KEY, SETTINGS_TAB_TAG).apply();
    }

    @Override
    public void onPause()
    {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String shared)
    {
        updatePreferenceSummary();
    }

    /**
     * Update displayed value of preferences.
     */
    private void updatePreferenceSummary()
    {
//        final SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
//        xplaneVersion.setSummary(sharedPreferences.getString("xplane_version", ""));
//        broadcastSubnet.setEnabled(sharedPreferences.getBoolean("autoconfigure", false));
//        simulatorAddress.setSummary(sharedPreferences.getString("sim_address", ""));
//        simulatorAddress.setEnabled(sharedPreferences.getBoolean("autoconfigure", false)
//                && !sharedPreferences.getBoolean("broadcast_subnet", false));
////        port.setSummary(sharedPreferences.getString("port", String.valueOf(UdpReceiverThread.DEFAULT_PORT)));
//        forwardAddress.setSummary(sharedPreferences.getString("forward_address", ""));
//        forwardAddress.setEnabled(sharedPreferences.getBoolean("enable_udp_forward", false));
    }

    @Override
    public boolean onPreferenceTreeClick(final PreferenceScreen preferenceScreen, final Preference preference)
    {
        LOGGER.debug("PreferenceScreen {} Preference {}", preferenceScreen, preference);
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

//    @Override
//    public void onValidationFailed(final View failedView, final Rule<?> failedRule)
//    {
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
//        builder.setMessage(failedRule.getFailureMessage())
//                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
//                {
//                    public void onClick(final DialogInterface dialogInterface, final int which)
//                    {
//                        dialogInterface.dismiss();
//                    }
//                });
//        builder.show();
//    }
}
