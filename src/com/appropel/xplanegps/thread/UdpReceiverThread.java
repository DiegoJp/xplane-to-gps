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
    /** Log tag. */
    private static final String TAG = UdpReceiverThread.class.getName();

    /** Conversion factor from knots to m/s. */
    public static final float KNOTS_TO_M_S = 0.514444444f;

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
                    socket.receive(packet);
                }
                catch (final InterruptedIOException iioex)
                {
                    continue;   // No packet was received so continue loop
                }

                // Verify that this is a valid packet from X-Plane by examining the first 5 bytes.
                if (data[0] != 0x44 || data[1] != 0x41 || data[2] != 0x54 || data[3] != 0x41 || data[4] != 0x40)
                {
                    continue;
                }

                // Extract data packets from buffer.
                ByteBuffer buffer = ByteBuffer.wrap(data);
                buffer.order(ByteOrder.LITTLE_ENDIAN);

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
                        case 17:    // pitch, roll, headings
                            location.setBearing(dataPacket.getValues()[2]);
                            break;
                        case 20:    // lat, lon, altitude
                            location.setLatitude(dataPacket.getValues()[0]);
                            location.setLongitude(dataPacket.getValues()[1]);
                            location.setAltitude(dataPacket.getValues()[2]);
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
