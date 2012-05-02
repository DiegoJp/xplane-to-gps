package com.appropel.xplanegps.thread;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Thread which receives UDP packets from X-Plane and translates them into Locations.
 */
public final class UdpReceiverThread implements Runnable
{
    /** Log tag. */
    private static final String TAG = UdpReceiverThread.class.getName();

    /** Conversion factor from knots to m/s. */
    public static final float KNOTS_TO_M_S = 0.514444444f;

    /** Name of location property for events. */
    public static final String LOCATION_PROPERTY = "location";

    /** Data buffer for packet reception. */
    private byte[] data = new byte[1024];

    /** Context for locating application resources. */
    private Context context;

    /** Property change support. */
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**
     * Constructs a new <code>UdpReceiverThread</code>.
     * @param context application text.
     */
    public UdpReceiverThread(final Context context)
    {
        this.context = context;
    }

    /** {@inheritDoc} */
    public void run()
    {
        try
        {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            String mockLocationProvider = LocationManager.GPS_PROVIDER;
            locationManager.addTestProvider(mockLocationProvider, false, false,
                    false, false, true, true, true, 0, 5);
            locationManager.setTestProviderEnabled(mockLocationProvider, true);

            DatagramSocket socket = new DatagramSocket(49000);
            DatagramPacket packet = new DatagramPacket(data, data.length);
            for (;;)
            {
                socket.receive(packet);
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
                pcs.firePropertyChange(LOCATION_PROPERTY, null, location);
            }
        }
        catch (Exception ex)
        {
            Log.e(TAG, "Exception", ex);
        }
    }

    /**
     * Adds a property change listener.
     * @param propertyName property name.
     * @param pcl listener.
     */
    public void addPropertyChangeListener(final String propertyName, final PropertyChangeListener pcl)
    {
        pcs.addPropertyChangeListener(propertyName, pcl);
    }
}
