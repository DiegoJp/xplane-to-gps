package com.appropel.xplanegps.view.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;

import com.appropel.xplanegps.R;
import com.appropel.xplanegps.view.util.SettingsUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main activity of the application.
 */
public final class MainActivity extends Activity
{
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MainActivity.class);

    /** Alert dialog to warn about mock locations. */
    private AlertDialog alertDialog;

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
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
                builder.setMessage(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? R.string.mock_location_app_warning : R.string.mock_location_warning)
                        .setCancelable(true);
                alertDialog = builder.create();
                alertDialog.show();
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Error checking device settings", e);
        }

        // Restore app to the previous tab seen, using Settings as the default.
//        final SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
//        final String tabTag = sharedPreferences.getString(TabConstants.TAB_TAG_KEY, SETTINGS_TAB_TAG);
//        Log.i(TAG, "Previous tab was: " + tabTag);
//        getTabHost().setCurrentTabByTag(tabTag);
    }

    @Override
    protected void onStop()
    {
        if (alertDialog != null)
        {
            alertDialog.dismiss();
            alertDialog = null;
        }
        super.onStop();
    }
}
