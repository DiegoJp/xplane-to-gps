package com.appropel.xplanegps.view.util;

import android.location.Location;
import android.location.LocationManager;

import com.appropel.xplane.udp.Data;
import com.appropel.xplanegps.common.util.LocationUtil;
import com.appropel.xplanegps.model.Preferences;

import java.lang.reflect.Method;

import de.greenrobot.event.EventBus;

/**
 * Utility for converting raw data into Android Location format.
 */
public final class LocationUtilImpl implements LocationUtil
{
    /** Conversion factor from knots to m/s. */
    public static final float KNOTS_TO_M_S = 0.514444444f;

    /** Conversion factor from feet to meters. */
    public static final float FEET_TO_METERS = 0.3048f;

    /** Name of mock location provider. */
    public static final String MOCK_PROVIDER_NAME = "XPlane";

    /** EasyVFR magic number. */
    private static final float EASY_VFR = 1234.0f;

    /** Preferences. */
    private final Preferences preferences;

    /** Event bus. */
    private final EventBus eventBus;

    /**
     * Constructs a new {@code LocationUtilImpl}.
     * @param preferences preferences.
     * @param eventBus event bus.
     */
    public LocationUtilImpl(final Preferences preferences, final EventBus eventBus)
    {
        this.preferences = preferences;
        this.eventBus = eventBus;
    }

    @Override
    public void broadcastLocation(final Data data)
    {
        // Transfer data values into a Location object.
        Location location = new Location(LocationManager.GPS_PROVIDER);

        for (Data.Chunk chunk : data.getChunks())
        {
            switch (chunk.getIndex())
            {
                case 3:     // speeds
                    location.setSpeed(chunk.getData()[3] * KNOTS_TO_M_S);
                    break;
                case 17:    // pitch, roll, headings (X-Plane 10)
                case 18:    // pitch, roll, headings (X-Plane 9)
                    location.setBearing(chunk.getData()[2]);
                    break;
                case 20:    // lat, lon, altitude
                    location.setLatitude(chunk.getData()[0]);
                    location.setLongitude(chunk.getData()[1]);
                    location.setAltitude(chunk.getData()[2] * FEET_TO_METERS);
                    break;
                default:
                    break;
            }
        }

        // Set the time in the location.
        location.setTime(System.currentTimeMillis());

        // Set accuracy.
        location.setAccuracy(preferences.isEasyVfr() ? EASY_VFR : 1.0f);

        try
        {
            Method locationJellyBeanFixMethod = Location.class.getMethod("makeComplete");
            if (locationJellyBeanFixMethod != null)
            {
                locationJellyBeanFixMethod.invoke(location);
            }
        }
        catch (final Exception ex)  // NOPMD - we want to ignore this.
        {
            // Do nothing if method doesn't exist.
        }

        eventBus.post(location);
    }
}
