package com.appropel.xplanegps.controller;

import com.appropel.xplane.udp.Data;
import com.appropel.xplane.udp.Dsel;
import com.appropel.xplane.udp.Iset;
import com.appropel.xplane.udp.PacketBase;
import com.appropel.xplane.udp.PacketUtil;
import com.appropel.xplane.udp.UdpUtil;
import com.appropel.xplanegps.common.event.DataEvent;
import com.appropel.xplanegps.common.util.XPlaneVersion;
import com.appropel.xplanegps.common.util.XPlaneVersionUtil;
import com.appropel.xplanegps.model.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import de.greenrobot.event.EventBus;

/**
 * Thread which listens for UDP data from X-Plane.
 */
public final class UdpReceiverThread implements Runnable    // NOPMD: complexity
{
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(UdpReceiverThread.class);

    /** Preferences. */
    final Preferences preferences;

    /** Event bus. */
    final EventBus eventBus;

    /** Flag to indicate if this thread is running. */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /** UDP utility. */
    private final UdpUtil udpUtil = new UdpUtil();

    /** Data buffer for packet reception. */
    private final byte[] data = new byte[256];

    /**
     * Constructs a new {@code UdpReceiverThread}.
     * @param preferences preferences.
     */
    public UdpReceiverThread(final Preferences preferences, final EventBus eventBus)
    {
        this.preferences = preferences;
        this.eventBus = eventBus;
    }

    @Override
    public void run()   // NOPMD - long method
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

        LOGGER.info("Receiver thread is listening on port {}", port);
        DatagramPacket packet = new DatagramPacket(data, data.length);
        try (final DatagramSocket socket = new DatagramSocket(port))
        {
            socket.setSoTimeout(100);   // Receive will timeout every 1/10 sec

            while (running.get())
            {
                try
                {
                    socket.receive(packet);
                }
                catch (final InterruptedIOException iioex)
                {
                    continue;   // No packet was received so continue loop
                }

                // If forwarding is active, send the packet back out.
                if (forwardUdp)
                {
                    final String forwardAddress = preferences.getForwardAddress();
                    if (forwardAddress != null && forwardAddress.length() > 0)
                    {
                        final DatagramPacket outPacket = new DatagramPacket(
                                packet.getData(),
                                packet.getLength(),
                                InetAddress.getByName(forwardAddress),
                                UdpUtil.XPLANE_UDP_PORT
                        );
                        socket.send(outPacket);
                    }
                }

                // Decode the packet and look for DATA packets.
                final PacketBase packetBase = PacketUtil.decode(data, packet.getLength());
                if (packetBase instanceof Data)
                {
                    eventBus.post(new DataEvent((Data) packetBase));
                }
            }
        }
        catch (Exception ex)
        {
            LOGGER.error("Exception receiving UDP data", ex);
            // TODO: can we alert the user? Send an event to pop a toast?
        }
        LOGGER.info("Stopping UdpReceiverThread.");
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
