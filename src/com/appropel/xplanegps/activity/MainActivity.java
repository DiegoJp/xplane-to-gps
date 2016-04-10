package com.appropel.xplanegps.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;
import com.appropel.xplanegps.R;
import com.appropel.xplanegps.guice.MainApplication;
import com.appropel.xplanegps.utility.SettingsUtility;
import com.google.inject.Inject;
import roboguice.activity.RoboTabActivity;

/**
 * Main activity of the application.
 */
public final class MainActivity extends RoboTabActivity implements TabConstants
{
    /** Log tag. */
    private static final String TAG = MainActivity.class.getName();

    /** Main application. */
    @Inject
    private MainApplication mainApplication;

    /** Alert dialog to warn about mock locations. */
    private AlertDialog alertDialog;

    /** {@inheritDoc} */
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Set up tabs.
        Resources res = getResources();
        TabHost tabHost = getTabHost();
        TabHost.TabSpec spec;

        // Settings tab
        spec = tabHost.newTabSpec(SETTINGS_TAB_TAG)
                .setIndicator(getString(R.string.settings), res.getDrawable(R.drawable.ic_tab_tick))
                .setContent(new Intent(this, SettingsActivity.class));
        tabHost.addTab(spec);

        // Data tab
        spec = tabHost.newTabSpec(DATA_TAB_TAG)
                .setIndicator(getString(R.string.data), res.getDrawable(R.drawable.ic_tab_plane))
                .setContent(new Intent(this, DataActivity.class));
        tabHost.addTab(spec);

        // Instructions tab
        spec = tabHost.newTabSpec(INSTRUCTIONS_TAB_TAG)
                .setIndicator(getString(R.string.instructions), res.getDrawable(R.drawable.ic_tab_gear))
                .setContent(new Intent(this, InstructionActivity.class));
        tabHost.addTab(spec);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // Ensure that mock locations are enabled. If not, pop up an eternal dialog.
        try
        {
            if (!SettingsUtility.isMockLocationEnabled(this))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.mock_location_warning).setCancelable(true);
                alertDialog = builder.create();
                alertDialog.show();
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "Error checking device settings", e);
        }

        // Restore app to the previous tab seen, using Settings as the default.
        final SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        final String tabTag = sharedPreferences.getString(TabConstants.TAB_TAG_KEY, SETTINGS_TAB_TAG);
        Log.i(TAG, "Previous tab was: " + tabTag);
        getTabHost().setCurrentTabByTag(tabTag);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if (alertDialog != null)
        {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

}
