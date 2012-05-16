package com.appropel.xplanegps.service;

import android.content.Intent;
import android.os.IBinder;
import com.appropel.xplanegps.guice.MainApplication;
import com.appropel.xplanegps.thread.UdpReceiverThread;
import com.google.inject.Inject;
import roboguice.service.RoboService;

/**
 * Data service.
 */
public final class DataService extends RoboService
{
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
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        udpReceiverThread.stop();
        udpReceiverThread = null;
    }
}
