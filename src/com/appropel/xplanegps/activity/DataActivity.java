package com.appropel.xplanegps.activity;

import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import com.appropel.xplanegps.guice.MainApplication;
import com.appropel.xplanegps.thread.UdpReceiverThread;
import com.example.R;
import com.google.inject.Inject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

/**
 * Activity which displays the data stream coming from X-Plane.
 */
public final class DataActivity extends RoboActivity  implements PropertyChangeListener
{
    /** Main application. */
    @Inject
    private MainApplication mainApplication;

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

    /** {@inheritDoc} */
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        mainApplication.getUdpReceiverThread().addPropertyChangeListener(
                UdpReceiverThread.LOCATION_PROPERTY, this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        mainApplication.getUdpReceiverThread().removePropertyChangeListener(
                UdpReceiverThread.LOCATION_PROPERTY, this);
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

    /** {@inheritDoc} */
    public void propertyChange(final PropertyChangeEvent propertyChangeEvent)
    {
        final Location location = (Location) propertyChangeEvent.getNewValue();
        updateData(location);
    }
}
