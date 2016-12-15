package com.appropel.xplanegps.view.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.IBinder;
import android.widget.Toast;

import com.appropel.xplanegps.R;
import com.appropel.xplanegps.controller.UdpReceiverThread;
import com.appropel.xplanegps.dagger.DaggerWrapper;
import com.appropel.xplanegps.model.Preferences;
import com.appropel.xplanegps.view.util.IntentProvider;
import com.appropel.xplanegps.view.util.LocationUtilImpl;
import com.appropel.xplanegps.view.util.SettingsUtil;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Data service.
 */
public final class DataService extends Service
{
    /**
     * Notification identifier.
     */
    private static final int NOTIFICATION_ID = 1;

    /**
     * Reference to background thread which processes packets.
     */
    @Inject
    UdpReceiverThread udpReceiverThread;

    /**
     * Intent provider.
     */
    @Inject
    IntentProvider intentProvider;

    /** Preferences. */
    @Inject
    Preferences preferences;

    /** Event bus. */
    @Inject
    EventBus eventBus;

    /**
     * Location manager.
     */
    private LocationManager locationManager;

    @Override
    public void onCreate()
    {
        super.onCreate();
        DaggerWrapper.INSTANCE.getDaggerComponent().inject(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public IBinder onBind(final Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId)
    {
        new Thread(udpReceiverThread).start();

        if (!eventBus.isRegistered(this))
        {
            eventBus.register(this);
        }

        // Create notification.
        Intent notificationIntent = intentProvider.getActivityIntent();
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        final Notification notification = new Notification.Builder(this)
                .setTicker(getText(R.string.notification))
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.notification))
                .setSmallIcon(R.drawable.ic_menu_plane)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .build();

        startForeground(NOTIFICATION_ID, notification);

        try
        {
            if (SettingsUtil.isMockLocationEnabled(getApplicationContext())
                    && locationManager.getProvider(LocationUtilImpl.MOCK_PROVIDER_NAME) == null)
            {
                locationManager.addTestProvider(LocationUtilImpl.MOCK_PROVIDER_NAME, false, false,
                        false, false, true, true, true, Criteria.POWER_LOW, Criteria.ACCURACY_FINE);
                locationManager.setTestProviderEnabled(LocationUtilImpl.MOCK_PROVIDER_NAME, true);
            }
        }
        catch (SecurityException ex)
        {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        udpReceiverThread.stop();
        stopForeground(true);

        if (eventBus.isRegistered(this))
        {
            eventBus.unregister(this);
        }

        if (locationManager.getProvider(LocationUtilImpl.MOCK_PROVIDER_NAME) != null)
        {
            locationManager.removeTestProvider(LocationUtilImpl.MOCK_PROVIDER_NAME);
        }

        super.onDestroy();
    }

    /**
     * Updates the mock location from the given location.
     *
     * @param location location.
     */
    public void onEventMainThread(final Location location)
    {
        locationManager.setTestProviderStatus(LocationUtilImpl.MOCK_PROVIDER_NAME,
                LocationProvider.AVAILABLE,
                null, System.currentTimeMillis());
        locationManager.setTestProviderLocation(LocationUtilImpl.MOCK_PROVIDER_NAME, location);
    }
}
