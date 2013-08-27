package com.appropel.xplanegps.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.view.View;
import android.widget.TextView;
import com.appropel.xplanegps.R;
import com.appropel.xplanegps.thread.UdpReceiverThread;
import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Rules;
import com.mobsandgeeks.saripaar.Validator;
import roboguice.activity.RoboPreferenceActivity;
import roboguice.inject.InjectPreference;

/**
 * Activity for user preferences.
 */
public final class SettingsActivity extends RoboPreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener, Validator.ValidationListener
{
    /** Reception port. */
    @InjectPreference("port")
    private EditTextPreference port;

    /** Port forward address. */
    @InjectPreference("forward_address")
    private EditTextPreference forwardAddress;

    /** Validator. */
    private Validator validator;

    /** {@inheritDoc} */
    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        validator = new Validator(this);
        validator.setValidationListener(this);
        validator.put(port.getEditText(), Rules.gt(getString(R.string.port_gt), 1023));
        validator.put(port.getEditText(), Rules.lt(getString(R.string.port_lt), 65536));
        validator.put(forwardAddress.getEditText(),
                Rules.regex(getString(R.string.forward_invalid), Rules.REGEX_IP_ADDRESS, true));
    }

    /** {@inheritDoc} */
    @Override
    protected void onResume()
    {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        updatePreferenceSummary();
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
        validator.validate();
        updatePreferenceSummary();
    }

    /**
     * Update displayed value of preferences.
     */
    private void updatePreferenceSummary()
    {
        final SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        port.setSummary(sharedPreferences.getString("port", String.valueOf(UdpReceiverThread.DEFAULT_RECEIVE_PORT)));
        forwardAddress.setSummary(sharedPreferences.getString("forward_address", ""));
        forwardAddress.setEnabled(sharedPreferences.getBoolean("enable_udp_forward", false));
    }

    /** {@inheritDoc} */
    public void preValidation()
    {
        // These values must be updated or they are not always current and cause spurious validation failures.
        port.getEditText().setText(port.getText(), TextView.BufferType.NORMAL);
        forwardAddress.getEditText().setText(forwardAddress.getText(), TextView.BufferType.NORMAL);
    }

    /** {@inheritDoc} */
    public void onSuccess()
    {
    }

    /** {@inheritDoc} */
    public void onFailure(final View failedView, final Rule<?> failedRule)
    {
        // Reset failed value to default.
        if (port.getEditText().equals(failedView))
        {
            port.setText(String.valueOf(UdpReceiverThread.DEFAULT_RECEIVE_PORT));
        }
        else if (forwardAddress.getEditText().equals(failedView))
        {
            forwardAddress.setText("127.0.0.1");
        }

        // Show dialog alerting user to validation failure.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(failedRule.getFailureMessage())
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(final DialogInterface dialogInterface, final int i)
                   {
                       dialogInterface.dismiss();
                   }
               });
        builder.show();
    }

    /** {@inheritDoc} */
    public void onValidationCancelled()
    {
    }
}
