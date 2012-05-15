package com.appropel.xplanegps.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TabHost;
import com.appropel.xplanegps.R;
import com.appropel.xplanegps.guice.MainApplication;
import com.google.inject.Inject;
import roboguice.activity.RoboTabActivity;

/**
 * Main activity of the application.
 */
public final class MainActivity extends RoboTabActivity
{
    /** Log tag. */
    private static final String TAG = MainActivity.class.getName();

    /** Main application. */
    @Inject
    private MainApplication mainApplication;

    /** {@inheritDoc} */
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Ensure that mock locations are enabled.
        try
        {
            int enabled = Settings.Secure.getInt(getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION);
            if (enabled == 1)
            {
                mainApplication.startUdpReceiverThread();
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.mock_location_warning)
                    .setPositiveButton(getText(R.string.go_to_settings),
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(final DialogInterface dialog, final int id)
                                {
                                    startActivity(
                                        new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
                                    finish();
                                }
                            });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
        catch (Settings.SettingNotFoundException e)
        {
            Log.e(TAG, "Error checking device settings", e);
        }

        // Set up tabs.
        Resources res = getResources();
        TabHost tabHost = getTabHost();
        TabHost.TabSpec spec;

        // Data tab
        spec = tabHost.newTabSpec("data")
                .setIndicator(getString(R.string.data), res.getDrawable(R.drawable.ic_tab_plane))
                .setContent(new Intent(this, DataActivity.class));
        tabHost.addTab(spec);

        // Instructions tab
        spec = tabHost.newTabSpec("instructions")
                .setIndicator(getString(R.string.instructions), res.getDrawable(R.drawable.ic_tab_gear))
                .setContent(new Intent(this, InstructionActivity.class));
        tabHost.addTab(spec);
    }
}
