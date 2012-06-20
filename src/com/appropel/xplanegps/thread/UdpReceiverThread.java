package com.appropel.xplanegps.thread;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import com.appropel.xplanegps.guice.MainApplication;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
    /** Conversion factor from knots to m/s. */
    public static final float KNOTS_TO_M_S = 0.514444444f;

    /** Conversion factor from feet to meters. */
    public static final float FEET_TO_METERS = 0.3048f;

    /** Packet header. */
    public static final String PACKET_HEADER = "DATA";

    /** Log tag. */
    private static final String TAG = UdpReceiverThread.class.getName();

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
        Log.i(TAG, "Starting UdpReceiverThread.");
        try
        {
            LocationManager locationManager =
                    (LocationManager) mainApplication.getSystemService(Context.LOCATION_SERVICE);

            String mockLocationProvider = LocationManager.GPS_PROVIDER;
            locationManager.addTestProvider(mockLocationProvider, false, false,
                    false, false, true, true, true, 0, 5);
            locationManager.setTestProviderEnabled(mockLocationProvider, true);

            DatagramSocket socket = new DatagramSocket(49000);
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

                    // Extract data packets from buffer.
                    ByteBuffer buffer = ByteBuffer.wrap(data);
                    buffer.order(ByteOrder.LITTLE_ENDIAN);

                    // Verify that this is a valid packet from X-Plane by examining the first 4 bytes.
                    if (!PACKET_HEADER.equals(new String(buffer.array(), 0, 4)))
                    {
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
                    Location location = new Location(mockLocationProvider);
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

                    // set the time in the location. If the time on this location
                    // matches the time on the one in the previous set call, it will be
                    // ignored
                    location.setTime(System.currentTimeMillis());
                    locationManager.setTestProviderLocation(mockLocationProvider, location);
                    mainApplication.setLocation(location);
                }
                catch (Exception e)
                {
                    Log.w("Exception in receiver loop, continuing", e);
                }
            }
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
