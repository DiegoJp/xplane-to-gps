package com.appropel.xplanegps.controller;

import com.appropel.xplane.udp.Dsel;
import com.appropel.xplane.udp.Iset;
import com.appropel.xplane.udp.PacketUtil;
import com.appropel.xplane.udp.UdpUtil;
import com.appropel.xplanegps.common.util.XPlaneVersion;
import com.appropel.xplanegps.common.util.XPlaneVersionUtil;
import com.appropel.xplanegps.model.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

/**
 * Thread which listens for UDP data from X-Plane.
 */
public final class UdpReceiverThread implements Runnable
{
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(UdpReceiverThread.class);

    /** Preferences. */
    final Preferences preferences;

    /** Flag to indicate if this thread is running. */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /** UDP utility. */
    private final UdpUtil udpUtil = new UdpUtil();

    /**
     * Constructs a new {@code UdpReceiverThread}.
     * @param preferences preferences.
     */
    public UdpReceiverThread(final Preferences preferences)
    {
        this.preferences = preferences;
    }

    @Override
    public void run()
    {
        LOGGER.info("Starting UdpReceiverThread.");
        running.set(true);

        final boolean forwardUdp = preferences.isUdpForward();

        // Determine receive port.
        int port;
        try
        {
            port = Integer.valueOf(preferences.getReceivePort());
        }
        catch (NumberFormatException ex)
        {
            port = UdpUtil.XPLANE_UDP_PORT;
        }

        final XPlaneVersion xplaneVersion = XPlaneVersionUtil.getXPlaneVersion(preferences.getXplaneVersion());

        // Send out datagrams to auto-configure X-Plane.
        if (preferences.isAutoconfigure())
        {
            // Send out a DSEL to select the appropriate data indexes.
            Dsel dsel = xplaneVersion.getDsel();
            final String simulatorAddress = preferences.getSimulatorAddress();
            if (preferences.isBroadcastSubnet())
            {
                udpUtil.sendDatagramToSubnet(PacketUtil.encode(dsel), UdpUtil.XPLANE_UDP_PORT);
            }
            else
            {
                udpUtil.sendDatagram(PacketUtil.encode(dsel), simulatorAddress, UdpUtil.XPLANE_UDP_PORT);
            }

            // Send out an ISET to point X-Plane to the proper data receiver.
            final InetAddress inetAddress = udpUtil.getSiteLocalAddress();
            if (inetAddress != null)
            {
                final Iset iset = xplaneVersion.getIset(inetAddress.getHostAddress(), String.valueOf(port));
                if (preferences.isBroadcastSubnet())
                {
                    udpUtil.sendDatagramToSubnet(PacketUtil.encode(iset), UdpUtil.XPLANE_UDP_PORT);
                }
                else
                {
                    udpUtil.sendDatagram(PacketUtil.encode(iset), simulatorAddress, UdpUtil.XPLANE_UDP_PORT);
                }
            }
        }

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
