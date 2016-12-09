package com.appropel.xplanegps.dagger;

import android.content.Context;

/**
 * Holds the Dagger component needed for injecting view components.
 */
public enum DaggerWrapper
{
    /** Singleton instance. */
    INSTANCE;

    /** Component. */
    private DaggerComponent daggerComponent;

    public DaggerComponent getDaggerComponent()
    {
        return daggerComponent;
    }

    /**
     * Initializes Dagger.
     * @param context Android Context.
     */
    public void init(final Context context)
    {
        final DaggerModule module = new DaggerModule(context);
        daggerComponent = DaggerDaggerComponent.builder()
                .daggerModule(module)
                .build();
    }
}
