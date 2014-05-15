package com.appropel.xplanegps.guice;

import de.greenrobot.event.EventBus;
import roboguice.application.RoboApplication;

/**
 * Main application.
 */
public final class MainApplication extends RoboApplication
{
    /** Event bus. */
    private final EventBus eventBus = new EventBus();

    /**
     * Returns the application event bus.
     * @return event bus.
     */
    public EventBus getEventBus()
    {
        return eventBus;
    }
}
