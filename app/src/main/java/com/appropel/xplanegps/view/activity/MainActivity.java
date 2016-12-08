package com.appropel.xplanegps.view.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;

import com.appropel.xplanegps.R;

/**
 * Main activity of the application.
 */
public final class MainActivity extends Activity
{
    /** Log tag. */
    private static final String TAG = MainActivity.class.getName();

    /** Alert dialog to warn about mock locations. */
    private AlertDialog alertDialog;

    /** {@inheritDoc} */
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

//        // Set up tabs.
//        Resources res = getResources();
//        TabHost tabHost = getTabHost();
//        TabHost.TabSpec spec;
//
//        // Settings tab
//        spec = tabHost.newTabSpec(SETTINGS_TAB_TAG)
//                .setIndicator(getString(R.string.settings), res.getDrawable(R.drawable.ic_tab_tick))
//                .setContent(new Intent(this, SettingsActivity.class));
//        tabHost.addTab(spec);
//
//        // Data tab
//        spec = tabHost.newTabSpec(DATA_TAB_TAG)
//                .setIndicator(getString(R.string.data), res.getDrawable(R.drawable.ic_tab_plane))
//                .setContent(new Intent(this, DataActivity.class));
//        tabHost.addTab(spec);
//
//        // Instructions tab
//        spec = tabHost.newTabSpec(INSTRUCTIONS_TAB_TAG)
//                .setIndicator(getString(R.string.instructions), res.getDrawable(R.drawable.ic_tab_gear))
//                .setContent(new Intent(this, InstructionActivity.class));
//        tabHost.addTab(spec);
    }
/*
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
*/
}
