package com.appropel.xplanegps.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.appropel.xplanegps.R;
import com.appropel.xplanegps.guice.MainApplication;
import com.appropel.xplanegps.service.DataService;
import com.appropel.xplanegps.thread.UdpReceiverThread;
import com.google.inject.Inject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

/**
 * Activity which displays the data stream coming from X-Plane.
 */
public final class DataActivity extends RoboActivity implements TabConstants
{
    /** Time format. */
    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

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

    /** View holding fix time. */
    @InjectView(R.id.time_view)
    private TextView timeView;

    /** Button to activate service. */
    @InjectView(R.id.active_button)
    private CompoundButton activeButton;

    /** {@inheritDoc} */
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data);

        final Intent dataServiceIntent = new Intent(this, DataService.class);
        activeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(final CompoundButton compoundButton, final boolean b)
            {
                if (activeButton.isChecked())
                {
                    DataActivity.this.startService(dataServiceIntent);
                }
                else
                {
                    DataActivity.this.stopService(dataServiceIntent);
                }
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        try
        {
            int enabled = Settings.Secure.getInt(getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION);
            activeButton.setEnabled(enabled == 1);
        }
        catch (final Exception ex)
        {
            // Ignore.
        }
        activeButton.setChecked(DataService.isRunning());
        mainApplication.getEventBus().register(this);

        // Store current tab.
        final SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        sharedPreferences.edit().putString(TAB_TAG_KEY, DATA_TAB_TAG).apply();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        mainApplication.getEventBus().unregister(this);
    }

    /**
     * Updates the onscreen information from the given location.
     * @param location location.
     */
    public void onEventMainThread(final Location location)
    {
        latitudeView.setText(Location.convert(location.getLatitude(), Location.FORMAT_SECONDS));
        longitudeView.setText(Location.convert(location.getLongitude(), Location.FORMAT_SECONDS));
        altitudeView.setText(String.format("%.0f", location.getAltitude() / UdpReceiverThread.FEET_TO_METERS));
        headingView.setText(String.format("%03.0f", location.getBearing()));
        groundspeedView.setText(String.format("%.0f", location.getSpeed() / UdpReceiverThread.KNOTS_TO_M_S));
        timeView.setText(TIME_FORMAT.format(new Date(location.getTime())));
    }
}
