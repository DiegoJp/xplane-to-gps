package com.appropel.xplanegps.view.util;

import android.content.Intent;

/**
 * Provides global intents.
 */
public interface IntentProvider
{
    Intent getActivityIntent();

    Intent getServiceIntent();
}
