package com.appropel.xplanegps.activity;

import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import com.appropel.xplanegps.thread.UdpReceiverThread;
import com.appropel.xplanegps.utility.NetworkUtility;
import com.example.R;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

/**
 * Main activity of the application.
 */
public final class MainActivity extends RoboActivity
{
    /** View holding server code text. */
    @InjectView(R.id.ip_text_view)
    private TextView ipInstructionsView;

    /** View holding latitude. */
    @InjectView(R.id.latitude_view)
    private TextView latitudeView;

    /** View holding longitude. */
    @InjectView(R.id.longitude_view)
    private TextView longitudeView;

    /** View holding altitude. */
    @InjectView(R.id.altitude_view)
    private TextView altitudeView;

    /** View holding heading. */
    @InjectView(R.id.heading_view)
    private TextView headingView;

    /** View holding groundspeed. */
    @InjectView(R.id.groundspeed_view)
    private TextView groundspeedView;

    /** Reference to background thread which processes packets. */
    private UdpReceiverThread udpReceiverThread;

    /** {@inheritDoc} */
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ipInstructionsView.setText(
                String.format(getString(R.string.ip_instructions), NetworkUtility.getLocalIpAddress()));
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if (udpReceiverThread == null)
        {
            udpReceiverThread = new UdpReceiverThread(this);
            new Thread(udpReceiverThread).start();
            udpReceiverThread.addPropertyChangeListener(
                UdpReceiverThread.LOCATION_PROPERTY, new PropertyChangeListener()
                {
                    public void propertyChange(final PropertyChangeEvent propertyChangeEvent)
                    {
                        final Location location = (Location) propertyChangeEvent.getNewValue();
                        updateData(location);
                    }
                });
        }
    }

    /**
     * Updates the onscreen information from the given location.
     * @param location location.
     */
    public void updateData(final Location location)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                latitudeView.setText(Location.convert(location.getLatitude(), Location.FORMAT_SECONDS));
                longitudeView.setText(Location.convert(location.getLongitude(), Location.FORMAT_SECONDS));
                altitudeView.setText(String.format("%.0f", location.getAltitude()));
                headingView.setText(String.format("%03.0f", location.getBearing()));
                groundspeedView.setText(String.format("%.0f", location.getSpeed() / UdpReceiverThread.KNOTS_TO_M_S));
            }
        });
    }
}
