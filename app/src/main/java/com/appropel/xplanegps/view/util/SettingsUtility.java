package com.appropel.xplanegps.view.util;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

/**
 * Utility methods for querying settings.
 */
public final class SettingsUtility
{
    private SettingsUtility()
    {
        // Utility.
    }

    /**
     * Returns true if mock locations are enabled. On Android M, this means that our app is selected as
     * the mock location provider.
     * @param context Android Context.
     * @return true if enabled.
     */
    public static boolean isMockLocationEnabled(final Context context)
    {
        final SettingsChecker settingsChecker =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? new MarshmallowSettingsChecker()
                        : new OldSettingsChecker();
        try
        {
            return settingsChecker.isMockLocationEnabled(context);
        }
        catch (final Exception ex)
        {
            return false;
        }
    }

    /**
     * Interface to an object that can check if mock locations are enabled.
     */
    private interface SettingsChecker
    {
        /**
         * Returns true if Mock Locations are enabled.
         * @return true if enabled.
         */
        boolean isMockLocationEnabled(Context context) throws Exception;    // NOPMD
    }

    /**
     * Implementation of a settings checker for Marshmallow and above. In these versions the user must
     * manually select the mock location app.
     */
    private static class MarshmallowSettingsChecker implements SettingsChecker
    {
        @Override
        public boolean isMockLocationEnabled(final Context context)
        {
            AppOpsManager opsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            return opsManager.checkOp(AppOpsManager.OPSTR_MOCK_LOCATION, android.os.Process.myUid(),
                    context.getPackageName()) == AppOpsManager.MODE_ALLOWED;
        }
    }

    /**
     * Implementation of a legacy settings checker. In these versions mock locations must be enabled
     * in the developer settings.
     */
    private static class OldSettingsChecker implements SettingsChecker
    {
        @Override
        public boolean isMockLocationEnabled(final Context context) throws Settings.SettingNotFoundException
        {
            return Settings.Secure.getInt(context.getContentResolver(), "mock_location") == 1;
        }
    }
}
