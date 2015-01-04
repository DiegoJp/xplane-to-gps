package com.appropel.xplanegps.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;
import com.appropel.xplanegps.R;
import com.appropel.xplanegps.thread.UdpReceiverThread;
import com.appropel.xplanegps.utility.NetworkUtility;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

/**
 * Activity which displays the instructions.
 */
public final class InstructionActivity extends RoboActivity implements TabConstants
{
    /** View holding server code text. */
    @InjectView(R.id.ip_text_view)
    private TextView ipInstructionsView;

    /** {@inheritDoc} */
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instructions);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        final String port = PreferenceManager.getDefaultSharedPreferences(this).getString(
                "port", String.valueOf(UdpReceiverThread.DEFAULT_PORT));
        ipInstructionsView.setText(
                String.format(getString(R.string.ip_instructions), NetworkUtility.getLocalIpAddress(), port));

        // Store current tab.
        final SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        sharedPreferences.edit().putString(TAB_TAG_KEY, INSTRUCTIONS_TAB_TAG).apply();
    }
}
