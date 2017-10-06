package com.appropel.xplanegps.dagger;

import android.content.Context;

import com.appropel.xplane.udp.UdpUtil;
import com.appropel.xplane.udp.UdpUtilImpl;
import com.appropel.xplanegps.common.util.LocationUtil;
import com.appropel.xplanegps.model.Preferences;
import com.appropel.xplanegps.view.application.DefaultIntentProvider;
import com.appropel.xplanegps.view.util.IntentProvider;
import com.appropel.xplanegps.view.util.LocationUtilImpl;

import net.orange_box.storebox.StoreBox;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.greenrobot.event.EventBus;

/**
 * Dagger Module.
 */
@Module
@Singleton
public final class DaggerModule
{
    /** Android Context. */
    private final Context context;

    /**
     * Constructs a new {@code DaggerModule}.
     * @param context Android Context.
     */
    public DaggerModule(final Context context)
    {
        this.context = context;
    }

    /**
     * Provides the current Android Context.
     * @return context.
     */
    @Provides
    Context provideContext()
    {
        return context;
    }

    @Provides
    @Singleton
    Preferences providePreferences()
    {
        return StoreBox.create(context, Preferences.class);
    }

    @Provides
    @Singleton
    EventBus provideEventBus()
    {
        return new EventBus();
    }

    @Provides
    @Singleton
    LocationUtil provideLocationUtil(final Preferences preferences, final EventBus eventBus)
    {
        return new LocationUtilImpl(preferences, eventBus);
    }

    @Provides
    @Singleton
    UdpUtil provideUdpUtil()
    {
        return new UdpUtilImpl();
    }

    @Provides
    @Singleton
    IntentProvider provideIntentProvider()
    {
        return new DefaultIntentProvider(context);
    }
}
