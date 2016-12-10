package com.appropel.xplanegps.controller;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread which listens for UDP data from X-Plane.
 */
public final class UdpReceiverThread implements Runnable
{
    /** Flag to indicate if this thread is running. */
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void run()
    {
        running.set(true);

        while (running.get())
        {   // NOPMD
            // Empty.
        }
    }

    /**
     * Shuts down this thread.
     */
    public void stop()
    {
        running.set(false);
    }

    public boolean isRunning()
    {
        return running.get();
    }
}
