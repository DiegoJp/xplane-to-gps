package com.appropel.xplanegps.activity;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import com.example.R;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends Activity
{
    private static final String TAG = MainActivity.class.getName();
    private byte[] data = new byte[1024];
    private static final float KNOTS_TO_M_S = 0.514444444f;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        try
        {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            String mocLocationProvider = LocationManager.GPS_PROVIDER;
            locationManager.addTestProvider(mocLocationProvider, false, false,
                    false, false, true, true, true, 0, 5);
            locationManager.setTestProviderEnabled(mocLocationProvider, true);
//            locationManager.requestLocationUpdates(mocLocationProvider, 0, 0, this);

            DatagramSocket socket = new DatagramSocket(49000);
            DatagramPacket packet = new DatagramPacket(data, data.length);
            for (;;)
            {
                socket.receive(packet);
                ByteBuffer buffer = ByteBuffer.wrap(data);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                float speed = buffer.getFloat(21) * KNOTS_TO_M_S;
                float bearing = buffer.getFloat(45);
                float latitude = buffer.getFloat(81);
                float longitude = buffer.getFloat(85);
                float altitude = buffer.getFloat(89);

//                StringBuffer sb = new StringBuffer();
//                for (int i = 0; i < 64; ++i)
//                {
//                    sb.append(String.format("%02x ", data[i]));
//                }
//                Log.i(TAG, sb.toString());

                Location location = new Location(mocLocationProvider);
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                location.setAltitude(altitude);
                location.setBearing(bearing);
                location.setSpeed(speed);

//                Log.e(LOG_TAG, location.toString());

                // set the time in the location. If the time on this location
                // matches the time on the one in the previous set call, it will be
                // ignored
                location.setTime(System.currentTimeMillis());

                locationManager.setTestProviderLocation(mocLocationProvider,
                        location);
            }
        }
        catch (Exception ex)
        {
            Log.e(TAG, "Exception", ex);
        }
    }
}
