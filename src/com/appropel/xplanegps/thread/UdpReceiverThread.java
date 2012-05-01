package com.appropel.xplanegps.thread;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Thread which receives UDP packets from X-Plane and translates them into Locations.
 */
public final class UdpReceiverThread implements Runnable
{
    /** Log tag. */
    private static final String TAG = UdpReceiverThread.class.getName();

    /** Conversion factor from knots to m/s. */
    private static final float KNOTS_TO_M_S = 0.514444444f;

    /** Data buffer for packet reception. */
    private byte[] data = new byte[1024];

    /** Context for locating application resources. */
    private Context context;

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
                ByteBuffer buffer = ByteBuffer.wrap(data);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                float speed = buffer.getFloat(21) * KNOTS_TO_M_S;
                float bearing = buffer.getFloat(53);
                float latitude = buffer.getFloat(81);
                float longitude = buffer.getFloat(85);
                float altitude = buffer.getFloat(89);

                Location location = new Location(mockLocationProvider);
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                location.setAltitude(altitude);
                location.setBearing(bearing);
                location.setSpeed(speed);

                // set the time in the location. If the time on this location
                // matches the time on the one in the previous set call, it will be
                // ignored
                location.setTime(System.currentTimeMillis());

                locationManager.setTestProviderLocation(mockLocationProvider, location);
            }
        }
        catch (Exception ex)
        {
            Log.e(TAG, "Exception", ex);
        }
    }
}
