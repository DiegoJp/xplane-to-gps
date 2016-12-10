package com.appropel.xplanegps.view.application;

import android.content.Context;
import android.content.Intent;

import com.appropel.xplanegps.view.activity.MainActivity;
import com.appropel.xplanegps.view.service.DataService;

/**
 * Supplies various intents to avoid cyclic dependencies.
 */
public final class DefaultIntentProvider implements com.appropel.xplanegps.view.util.IntentProvider
{
    /** Android Context. */
    private final Context context;

    public DefaultIntentProvider(final Context context)
    {
        this.context = context;
    }

    @Override
    public Intent getActivityIntent()
    {
        return new Intent(context, MainActivity.class);
    }

    @Override
    public Intent getServiceIntent()
    {
        return new Intent(context, DataService.class);
    }
}
