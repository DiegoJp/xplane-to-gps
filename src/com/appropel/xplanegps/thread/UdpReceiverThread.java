package com.appropel.xplanegps.thread;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.preference.PreferenceManager;
import android.util.Log;
import com.appropel.xplane.udp.Data;
import com.appropel.xplane.udp.Dsel;
import com.appropel.xplane.udp.Iset;
import com.appropel.xplane.udp.UdpUtil;
import com.appropel.xplanegps.guice.MainApplication;
import java.io.InterruptedIOException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.codehaus.preon.Codecs;

/**
 * Thread which receives UDP packets from X-Plane and translates them into Locations.
 */
public final class UdpReceiverThread implements Runnable
{
    /** Default reception port. */
    public static final int DEFAULT_PORT = 49000;

    /** Conversion factor from knots to m/s. */
    public static final float KNOTS_TO_M_S = 0.514444444f;

    /** Conversion factor from feet to meters. */
    public static final float FEET_TO_METERS = 0.3048f;

    /** Packet header. */
    public static final String PACKET_HEADER = "DATA";

    /** Log tag. */
    private static final String TAG = UdpReceiverThread.class.getName();

    /** EasyVFR magic number. */
    private static final float EASY_VFR = 1234.0f;

    /** Data buffer for packet reception. */
    private byte[] data = new byte[1024];

    /** Context for locating application resources. */
    private MainApplication mainApplication;

    /** Flag to indicate if this thread is running. */
    private AtomicBoolean running = new AtomicBoolean(true);

    /**
     * Constructs a new <code>UdpReceiverThread</code>.
     * @param mainApplication application.
     */
    public UdpReceiverThread(final MainApplication mainApplication)
    {
        this.mainApplication = mainApplication;
    }

    /** {@inheritDoc} */
    public void run()
    {
        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(mainApplication);
        final boolean forwardUdp = sharedPreferences.getBoolean("enable_udp_forward", false);

        Log.i(TAG, "Starting UdpReceiverThread.");
        try
        {
            LocationManager locationManager =
                    (LocationManager) mainApplication.getSystemService(Context.LOCATION_SERVICE);

            locationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, false,
                    false, false, true, true, true, 0, Criteria.ACCURACY_FINE);
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);

            int port;
            try
            {
                port = Integer.valueOf(sharedPreferences.getString("port", String.valueOf(DEFAULT_PORT)));
            }
            catch (NumberFormatException ex)
            {
                port = DEFAULT_PORT;
            }

            // Send out datagrams to auto-configure X-Plane.
            if (sharedPreferences.getBoolean("autoconfigure", false))
            {
                try
                {
                    Dsel dsel;
                    switch(Integer.valueOf(sharedPreferences.getString("xplane_version", "10")))
                    {
                        case 9:
                            dsel = new Dsel(new int[] {3, 18, 20});
                            break;
                        case 10:
                        default:
                            dsel = new Dsel(new int[] {3, 17, 20});
                            break;
                    }

                    final String simulatorAddress = sharedPreferences.getString("sim_address", "127.0.0.1");
                    final boolean broadcast = sharedPreferences.getBoolean("broadcast_subnet", false);

                    final byte[] dselData = Codecs.encode(dsel, Dsel.CODEC);
                    if (broadcast)
                    {
                        UdpUtil.INSTANCE.sendDatagramToSubnet(dselData, UdpUtil.XPLANE_UDP_PORT);
                    }
                    else
                    {
                        UdpUtil.INSTANCE.sendDatagram(
                                dselData, InetAddress.getByName(simulatorAddress), UdpUtil.XPLANE_UDP_PORT);
                    }

                    final InetAddress inetAddress = UdpUtil.INSTANCE.getSiteLocalAddress();
                    if (inetAddress != null)
                    {
                        Iset iset;
                        switch(Integer.valueOf(sharedPreferences.getString("xplane_version", "10")))
                        {
                            case 9:
                                iset = new Iset(
                                    Iset.INDEX_DATA_RECEIVER_IP_9, inetAddress.getHostAddress(), String.valueOf(port));
                                break;
                            case 10:
                            default:
                                iset = new Iset(
                                    Iset.INDEX_DATA_RECEIVER_IP_10, inetAddress.getHostAddress(), String.valueOf(port));
                                break;
                        }
                        final byte[] isetData = Codecs.encode(iset, Iset.CODEC);

                        if (broadcast)
                        {
                            UdpUtil.INSTANCE.sendDatagramToSubnet(isetData, UdpUtil.XPLANE_UDP_PORT);
                        }
                        else
                        {
                            UdpUtil.INSTANCE.sendDatagram(
                                    isetData, InetAddress.getByName(simulatorAddress), UdpUtil.XPLANE_UDP_PORT);
                        }
                    }
                }
                catch (final Exception ex)
                {
                    Log.e(TAG, "Exception sending configuration datagrams", ex);
                }
            }

            Log.i(TAG, String.format("Receiver thread is listening on port %d", port));
            DatagramSocket socket = new DatagramSocket(port);
            socket.setSoTimeout(100);   // Receive will timeout every 1/10 sec
            DatagramPacket packet = new DatagramPacket(data, data.length);
            for (; running.get();)
            {
                try
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
                        try
                        {
                            final String forwardAddress = sharedPreferences.getString("forward_address", "");
                            if (forwardAddress != null && forwardAddress.length() > 0)
                            {
                                final DatagramPacket outPacket = new DatagramPacket(
                                        packet.getData(),
                                        packet.getLength(),
                                        InetAddress.getByName(forwardAddress),
                                        DEFAULT_PORT
                                );
                                socket.send(outPacket);
                            }
                        }
                        catch (Exception ex)
                        {
                            Log.d(TAG, "Exception forwarding UDP packet", ex);
                        }
                    }

                    // Decode packet using Preon.
                    final List<Data> messages = new ArrayList<Data>();
                    int index = 0;
                    for (;;)
                    {
                        try
                        {
                            final Data dataMsg = Codecs.decode(
                                    Data.CODEC, Arrays.copyOfRange(data, index, data.length));
                            messages.add(dataMsg);
                            index += 36;
                        }
                        catch (Exception ex)
                        {
                            break;
                        }
                    }

                    // Transfer data values into a Location object.
                    Location location = new Location(LocationManager.GPS_PROVIDER);
                    for (Data dataMsg : messages)
                    {
                        switch (dataMsg.getIndex())
                        {
                            case 3:     // speeds
                                location.setSpeed(dataMsg.getData()[3] * KNOTS_TO_M_S);
                                break;
                            case 17:    // pitch, roll, headings (X-Plane 10)
                            case 18:    // pitch, roll, headings (X-Plane 9)
                                location.setBearing(dataMsg.getData()[2]);
                                break;
                            case 20:    // lat, lon, altitude
                                location.setLatitude(dataMsg.getData()[0]);
                                location.setLongitude(dataMsg.getData()[1]);
                                location.setAltitude(dataMsg.getData()[2] * FEET_TO_METERS);
                                break;
                            default:
                                break;
                        }
                    }

                    // Set the time in the location.
                    location.setTime(System.currentTimeMillis());

                    // Set accuracy.
                    final boolean easyVfr = sharedPreferences.getBoolean("easy_vfr", false);
                    location.setAccuracy(easyVfr ? EASY_VFR : 1.0f);

                    try
                    {
                        Method locationJellyBeanFixMethod = Location.class.getMethod("makeComplete");
                        if (locationJellyBeanFixMethod != null)
                        {
                            locationJellyBeanFixMethod.invoke(location);
                        }
                    }
                    catch (final Exception ex)
                    {
                        // Do nothing if method doesn't exist.
                    }

                    locationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER,
                            LocationProvider.AVAILABLE,
                            null, System.currentTimeMillis());
                    locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location);
                    mainApplication.getEventBus().post(location);
                }
                catch (Exception e)
                {
                    Log.w(TAG, "Exception in receiver loop, continuing", e);
                }
            }
            socket.close();
        }
        catch (Exception ex)
        {
            Log.e(TAG, "Exception", ex);
        }
        Log.i(TAG, "Stopping UdpReceiverThread.");
    }

    /**
     * Shuts down this thread.
     */
    public void stop()
    {
        running.set(false);
    }
}
