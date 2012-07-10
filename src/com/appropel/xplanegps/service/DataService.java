package com.appropel.xplanegps.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import com.appropel.xplanegps.R;
import com.appropel.xplanegps.activity.MainActivity;
import com.appropel.xplanegps.guice.MainApplication;
import com.appropel.xplanegps.thread.UdpReceiverThread;
import com.google.inject.Inject;
import roboguice.service.RoboService;

/**
 * Data service.
 */
public final class DataService extends RoboService
{
    /** Notification identifier. */
    private static final int NOTIFICATION_ID = 1;

    /** Main application. */
    @Inject
    private MainApplication mainApplication;

    /** Reference to background thread which processes packets. */
    private UdpReceiverThread udpReceiverThread;

    @Override
    public IBinder onBind(final Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId)
    {
        udpReceiverThread = new UdpReceiverThread(mainApplication);
        new Thread(udpReceiverThread).start();

        // Create notification.
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(
                R.drawable.ic_menu_plane, getText(R.string.notification), System.currentTimeMillis());
        Context context = getApplicationContext();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(
                context, getText(R.string.app_name), getText(R.string.notification), contentIntent);
        notificationManager.notify(NOTIFICATION_ID, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        udpReceiverThread.stop();
        udpReceiverThread = null;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
