package com.appropel.xplanegps.view.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.appropel.xplanegps.R;
import com.appropel.xplanegps.controller.UdpReceiverThread;
import com.appropel.xplanegps.dagger.DaggerWrapper;
import com.appropel.xplanegps.view.util.IntentProvider;
import com.appropel.xplanegps.view.util.SettingsUtility;

import java.text.DateFormat;
import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Activity which displays the data stream coming from X-Plane.
 */
public final class DataFragment extends Fragment
{
    /** Key for shared pref. */
    public static final String PREF_VALUE = "data";

    /** Time format. */
    private final DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.LONG);

    /** View holding latitude. */
    @BindView(R.id.latitude_view)
    TextView latitudeView;

    /** View holding longitude. */
    @BindView(R.id.longitude_view)
    TextView longitudeView;

    /** View holding altitude. */
    @BindView(R.id.altitude_view)
    TextView altitudeView;

    /** View holding heading. */
    @BindView(R.id.heading_view)
    TextView headingView;

    /** View holding groundspeed. */
    @BindView(R.id.groundspeed_view)
    TextView groundspeedView;

    /** View holding fix time. */
    @BindView(R.id.time_view)
    TextView timeView;

    /** Button to activate service. */
    @BindView(R.id.active_button)
    CompoundButton activeButton;

    /** Reference to background thread which processes packets. */
    @Inject
    UdpReceiverThread udpReceiverThread;

    /** Intent provider. */
    @Inject
    IntentProvider intentProvider;

    /** Used by ButterKnife. */
    private Unbinder unbinder;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
        final View view = inflater.inflate(R.layout.data, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) // NOPMD
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        DaggerWrapper.INSTANCE.getDaggerComponent().inject(this);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        final Intent dataServiceIntent = intentProvider.getServiceIntent();
        activeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(final CompoundButton compoundButton, final boolean isChecked)
            {
                if (activeButton.isChecked())
                {
                    getActivity().startService(dataServiceIntent);
                }
                else
                {
                    getActivity().stopService(dataServiceIntent);
                }
            }
        });
    }

    @Override
    public void onStart()
    {
        super.onStart();
        activeButton.setEnabled(SettingsUtility.isMockLocationEnabled(getActivity()));
        activeButton.setChecked(udpReceiverThread.isRunning());
//        mainApplication.getEventBus().register(this);

    }

    @Override
    public void onStop() // NOPMD
    {
        super.onStop();
//        mainApplication.getEventBus().unregister(this);
    }

    @Override
    public void onDestroyView()
    {
        unbinder.unbind();
        super.onDestroyView();
    }

    /**
     * Updates the onscreen information from the given location.
     * @param location location.
     */
    public void onEventMainThread(final Location location)
    {
        latitudeView.setText(Location.convert(location.getLatitude(), Location.FORMAT_SECONDS));
        longitudeView.setText(Location.convert(location.getLongitude(), Location.FORMAT_SECONDS));
//        altitudeView.setText(String.format("%.0f", location.getAltitude() / UdpReceiverThread.FEET_TO_METERS));
        headingView.setText(String.format("%03.0f", location.getBearing()));
//        groundspeedView.setText(String.format("%.0f", location.getSpeed() / UdpReceiverThread.KNOTS_TO_M_S));
        timeView.setText(timeFormat.format(new Date(location.getTime())));
    }
}
