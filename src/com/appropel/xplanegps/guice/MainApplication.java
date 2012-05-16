package com.appropel.xplanegps.guice;

import android.location.Location;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import roboguice.application.RoboApplication;

/**
 * Main application.
 */
public final class MainApplication extends RoboApplication
{
    /** Name of location property for events. */
    public static final String LOCATION_PROPERTY = "location";

    /** Property change support. */
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**
     * Sets the current location.
     * @param location location.
     */
    public void setLocation(final Location location)
    {
        pcs.firePropertyChange(LOCATION_PROPERTY, null, location);
    }

    /**
     * Adds a property change listener.
     * @param propertyName property name.
     * @param pcl listener.
     */
    public void addPropertyChangeListener(final String propertyName, final PropertyChangeListener pcl)
    {
        pcs.addPropertyChangeListener(propertyName, pcl);
    }

    /**
     * Removes a property change listener.
     * @param propertyName property name.
     * @param pcl listener.
     */
    public void removePropertyChangeListener(final String propertyName, final PropertyChangeListener pcl)
    {
        pcs.removePropertyChangeListener(propertyName, pcl);
    }
}
