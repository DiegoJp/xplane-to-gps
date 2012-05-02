package com.appropel.xplanegps.guice;

import com.appropel.xplanegps.thread.UdpReceiverThread;
import roboguice.application.RoboApplication;

/**
 * Main application.
 */
public final class MainApplication extends RoboApplication
{
    /** Reference to background thread which processes packets. */
    private UdpReceiverThread udpReceiverThread;

    /**
     * Constructs a new <code>MainApplication</code>.
     */
    public MainApplication()
    {
        udpReceiverThread = new UdpReceiverThread(this);
        new Thread(udpReceiverThread).start();
    }

    /**
     * Returns the single instance of the UDP receiver thread.
     * @return thread instance.
     */
    public UdpReceiverThread getUdpReceiverThread()
    {
        return udpReceiverThread;
    }
}
