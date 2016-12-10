package com.appropel.xplanegps.view.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.appropel.xplanegps.R;
import com.appropel.xplanegps.controller.UdpReceiverThread;
import com.appropel.xplanegps.dagger.DaggerWrapper;
import com.appropel.xplanegps.view.util.IntentProvider;

import javax.inject.Inject;

/**
 * Data service.
 */
public final class DataService extends Service
{
    /** Notification identifier. */
    private static final int NOTIFICATION_ID = 1;

    /** Reference to background thread which processes packets. */
    @Inject
    UdpReceiverThread udpReceiverThread;

    /** Intent provider. */
    @Inject
    IntentProvider intentProvider;

    @Override
    public void onCreate()
    {
        super.onCreate();
        DaggerWrapper.INSTANCE.getDaggerComponent().inject(this);
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
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        udpReceiverThread.stop();
        stopForeground(true);
        super.onDestroy();
    }
}
