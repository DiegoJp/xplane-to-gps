package com.appropel.xplanegps.view.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import com.appropel.xplane.udp.Becn;
import com.appropel.xplane.udp.PacketUtil;
import com.appropel.xplane.udp.UdpUtil;
import com.appropel.xplanegps.R;
import com.appropel.xplanegps.common.util.Expressions;
import com.appropel.xplanegps.dagger.DaggerWrapper;
import com.appropel.xplanegps.model.Preferences;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Activity for user preferences.
 */
public final class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener
{
    /** Key for shared pref. */
    public static final String PREF_VALUE = "settings";

    /** X-Plane version. */
    private ListPreference xplaneVersion;

    /** Broadcast subnet checkbox. */
    private CheckBoxPreference broadcastSubnet;

    /** Simulator IP address. */
    private EditTextPreference simulatorAddress;

    /** Reception port. */
    private EditTextPreference port;

    /** Port forward address. */
    private EditTextPreference forwardAddress;

    /** Event bus. */
    @Inject
    EventBus eventBus;

    /** Preferences. */
    @Inject
    Preferences preferences;

    /** UDP utilities. */
    @Inject
    UdpUtil udpUtil;

    /** Socket used to listen for X-Plane beacon. */
    private MulticastSocket socket;

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        addPreferencesFromResource(R.xml.settings);

        DaggerWrapper.INSTANCE.getDaggerComponent().inject(this);

        // Find preferences the hard way.
        xplaneVersion = (ListPreference) findPreference("xplane_version");
        broadcastSubnet = (CheckBoxPreference) findPreference("broadcast_subnet");
        simulatorAddress = (EditTextPreference) findPreference("sim_address");
        port = (EditTextPreference) findPreference("port");
        forwardAddress = (EditTextPreference) findPreference("forward_address");
    }

    @Override
    public void onStart()
    {
        super.onStart();
        eventBus.register(this);
        socket = udpUtil.joinMulticastGroup(UdpUtil.XPLANE_BEACON_ADDRESS, UdpUtil.XPLANE_BEACON_PORT);
        if (socket != null)     // Might be null if an error occurred opening the socket.
        {
            new Thread(new BeaconReceiver(socket, eventBus)).start();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        updatePreferenceSummary();
    }

    @Override
    public void onPause()
    {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onStop()
    {
        IOUtils.closeQuietly(socket);
        eventBus.unregister(this);
        super.onStop();
    }

    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String shared)
    {
        validate();
        updatePreferenceSummary();
    }

    /**
     * Handles an X-Plane beacon event.
     * @param event event
     */
    public void onEventMainThread(final XPlaneBeaconEvent event)
    {
        if (event.getAddress().equals(preferences.getSimulatorAddress()))
        {
            return; // Already set to this address
        }

        final String version = String.valueOf(event.getBecn().getVersionNumber() / 10000);
        final String message = getActivity().getResources().getString(R.string.beacon,
                version,
                event.getBecn().getComputerName(),
                event.getAddress());
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which)
                    {
                        preferences.setSimulatorAddress(event.getAddress());
                        preferences.setBroadcastSubnet(false);
                        preferences.setXplaneVersion(version);
                        updatePreferenceSummary();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which)
                    {
                        dialog.dismiss();
                    }
                })
                .show();
    }


    /**
     * Update displayed value of preferences.
     */
    void updatePreferenceSummary()
    {
        xplaneVersion.setSummary(preferences.getXplaneVersion());
        broadcastSubnet.setEnabled(preferences.isAutoconfigure());
        broadcastSubnet.setChecked(preferences.isBroadcastSubnet());
        simulatorAddress.setSummary(preferences.getSimulatorAddress());
        simulatorAddress.setEnabled(preferences.isAutoconfigure() && !preferences.isBroadcastSubnet());
        port.setSummary(preferences.getReceivePort());
        forwardAddress.setSummary(preferences.getForwardAddress());
        forwardAddress.setEnabled(preferences.isUdpForward());
    }

    /**
     * Validates the preference values and shows an alert dialog if there is a problem.
     */
    private void validate()
    {
        if (!Expressions.isValidIpAddress(preferences.getSimulatorAddress()))
        {
            showAlertDialog(getString(R.string.sim_invalid));
            preferences.setSimulatorAddress(getString(R.string.localhost));
        }
        
        final int port = Integer.parseInt(preferences.getReceivePort());
        if (port < 1024)
        {
            showAlertDialog(getString(R.string.port_gt));
            preferences.setReceivePort(getString(R.string.default_port));
        }
        else if (port > 65535)
        {
            showAlertDialog(getString(R.string.port_lt));
            preferences.setReceivePort(getString(R.string.default_port));
        }

        if (!Expressions.isValidIpAddress(preferences.getForwardAddress()))
        {
            showAlertDialog(getString(R.string.forward_invalid));
            preferences.setForwardAddress(getString(R.string.localhost));
        }
    }

    /**
     * Show dialog alerting user to validation failure.
     * @param message message.
     */
    private void showAlertDialog(final String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                {
                    public void onClick(final DialogInterface dialogInterface, final int which)
                    {
                        dialogInterface.dismiss();
                    }
                });
        builder.show();
    }

    /**
     * Runnable thread to receive X-Plane multicast beacon packets.
     */
    private static class BeaconReceiver implements Runnable
    {
        /** Buffer. */
        private final byte[] buffer = new byte[1024];

        /** Socket to receive on. */
        private final MulticastSocket socket;

        /** Event bus. */
        private final EventBus eventBus;

        /**
         * Constructs a new {@code BeaconReceiver}.
         * @param socket multicast socket
         * @param eventBus event bus
         */
        public BeaconReceiver(final MulticastSocket socket, final EventBus eventBus)
        {
            this.socket = socket;
            this.eventBus = eventBus;
        }

        @Override
        public void run()
        {
            final DatagramPacket recv = new DatagramPacket(buffer, buffer.length);
            try
            {
                socket.receive(recv);
                final Becn becn = (Becn) PacketUtil.decode(buffer, buffer.length);  // NOPMD: future use
                eventBus.post(new XPlaneBeaconEvent(recv.getAddress().getHostAddress(), becn));
            }
            catch (IOException e)   // NOPMD
            {
                // Ignore, could be interruption.
            }
        }
    }

    /**
     * Event broadcast when an X-Plane beacon message is received.
     */
    private static final class XPlaneBeaconEvent
    {
        /** Internet address the packet came from. */
        private final String address;

        /** BECN message. */
        private final Becn becn;

        /**
         * Constructs a new {@code XPlaneBeaconEvent}.
         * @param address address that the BECN came from
         * @param becn BECN message
         */
        public XPlaneBeaconEvent(final String address, final Becn becn)
        {
            this.address = address;
            this.becn = becn;
        }

        public String getAddress()
        {
            return address;
        }

        public Becn getBecn()
        {
            return becn;
        }
    }
}
