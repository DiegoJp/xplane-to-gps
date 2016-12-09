package com.appropel.xplanegps.view.fragment;

import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.appropel.xplanegps.R;
import com.appropel.xplanegps.view.util.SettingsUtility;

import java.text.DateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Activity which displays the data stream coming from X-Plane.
 */
public final class DataFragment extends Fragment
{
    /** Time format. */
    private final DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.LONG);

    /** Main application. */
//    @Inject
//    private MainApplication mainApplication;

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

//        final Intent dataServiceIntent = new Intent(this, DataService.class);
//        activeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
//        {
//            public void onCheckedChanged(final CompoundButton compoundButton, final boolean b)
//            {
//                if (activeButton.isChecked())
//                {
//                    DataFragment.this.startService(dataServiceIntent);
//                }
//                else
//                {
//                    DataFragment.this.stopService(dataServiceIntent);
//                }
//            }
//        });
    }

    @Override
    public void onStart()
    {
        super.onStart();
        activeButton.setEnabled(SettingsUtility.isMockLocationEnabled(getActivity()));
//        activeButton.setChecked(DataService.isRunning());
//        mainApplication.getEventBus().register(this);

        // Store current tab.
//        final SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
//        sharedPreferences.edit().putString(TAB_TAG_KEY, DATA_TAB_TAG).apply();
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
