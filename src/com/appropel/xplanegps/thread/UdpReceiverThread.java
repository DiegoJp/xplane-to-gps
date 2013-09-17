package com.appropel.xplanegps.thread;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.preference.PreferenceManager;
import android.util.Log;
import com.appropel.xplanegps.guice.MainApplication;
import java.io.InterruptedIOException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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

                    // Extract data packets from buffer.
                    ByteBuffer buffer = ByteBuffer.wrap(data);
                    buffer.order(ByteOrder.LITTLE_ENDIAN);

                    // Verify that this is a valid packet from X-Plane by examining the first 4 bytes.
                    if (!PACKET_HEADER.equals(new String(buffer.array(), 0, 4)))
                    {
                        Log.d(TAG, "Received an unknown packet!");
                        continue;
                    }

                    int index = 5;
                    List<DataPacket> dataPackets = new ArrayList<DataPacket>();
                    while (index + DataPacket.LENGTH <= packet.getLength())
                    {
                        DataPacket dataPacket = new DataPacket(buffer, index);
                        dataPackets.add(dataPacket);
                        index += DataPacket.LENGTH;
                    }

                    // Transfer data values into a Location object.
                    Location location = new Location(LocationManager.GPS_PROVIDER);
                    for (DataPacket dataPacket : dataPackets)
                    {
                        switch (dataPacket.getIndex())
                        {
                            case 3:     // speeds
                                location.setSpeed(dataPacket.getValues()[3] * KNOTS_TO_M_S);
                                break;
                            case 17:    // pitch, roll, headings (X-Plane 10)
                            case 18:    // pitch, roll, headings (X-Plane 9)
                                location.setBearing(dataPacket.getValues()[2]);
                                break;
                            case 20:    // lat, lon, altitude
                                location.setLatitude(dataPacket.getValues()[0]);
                                location.setLongitude(dataPacket.getValues()[1]);
                                location.setAltitude(dataPacket.getValues()[2] * FEET_TO_METERS);
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
                    mainApplication.setLocation(location);
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
