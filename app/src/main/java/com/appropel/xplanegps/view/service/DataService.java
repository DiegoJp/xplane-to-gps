package com.appropel.xplanegps.view.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.IBinder;
import android.widget.Toast;

import com.appropel.xplane.udp.Data;
import com.appropel.xplane.udp.PacketBase;
import com.appropel.xplane.udp.PacketUtil;
import com.appropel.xplane.udp.UdpCallbackReceiver;
import com.appropel.xplane.udp.UdpUtil;
import com.appropel.xplanegps.R;
import com.appropel.xplanegps.common.util.LocationUtil;
import com.appropel.xplanegps.common.util.XPlaneVersion;
import com.appropel.xplanegps.common.util.XPlaneVersionUtil;
import com.appropel.xplanegps.dagger.DaggerWrapper;
import com.appropel.xplanegps.model.Preferences;
import com.appropel.xplanegps.view.util.IntentProvider;
import com.appropel.xplanegps.view.util.SettingsUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Data service.
 */
public final class DataService extends Service  // NOPMD
{
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(DataService.class);

    /**
     * Notification identifier.
     */
    private static final int NOTIFICATION_ID = 1;

    /**
     * Intent provider.
     */
    @Inject
    IntentProvider intentProvider;

    /** Preferences. */
    @Inject
    Preferences preferences;

    /** Event bus. */
    @Inject
    EventBus eventBus;

    /** UDP utilities. */
    @Inject
    UdpUtil udpUtil;

    /** Location utility. */
    @Inject
    LocationUtil locationUtil;

    /**
     * Location manager.
     */
    private LocationManager locationManager;

    @Override
    public void onCreate()
    {
        super.onCreate();
        DaggerWrapper.INSTANCE.getDaggerComponent().inject(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public IBinder onBind(final Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId)  // NOPMD
    {
        LOGGER.info("onStartCommand - starting receiver thread");

        final UdpCallbackReceiver callbackReceiver = new UdpCallbackReceiver()
        {
            @Override
            public int getPort()
            {
                // Determine receive port.
                int port;
                try
                {
                    port = Integer.parseInt(preferences.getReceivePort());
                }
                catch (NumberFormatException ex)
                {
                    port = UdpUtil.XPLANE_UDP_PORT;
                }

                return port;
            }

            @Override
            public void initialize()
            {
                final XPlaneVersion xplaneVersion = XPlaneVersionUtil.getXPlaneVersion(preferences.getXplaneVersion());

                // Send out datagrams to auto-configure X-Plane.
                final String simulatorAddress = preferences.getSimulatorAddress();
                if (preferences.isAutoconfigure())
                {
                    // Send out a DSEL to select the appropriate data indexes.
                    PacketBase dsel = xplaneVersion.getDsel();
                    if (preferences.isBroadcastSubnet())
                    {
                        udpUtil.sendDatagramToSubnet(PacketUtil.encode(dsel), UdpUtil.XPLANE_UDP_PORT);
                    }
                    else
                    {
                        udpUtil.sendDatagram(PacketUtil.encode(dsel), simulatorAddress, UdpUtil.XPLANE_UDP_PORT);
                    }

                    // Send out an ISET to point X-Plane to the proper data receiver.
                    final InetAddress inetAddress = udpUtil.getSiteLocalAddress();
                    if (inetAddress != null)
                    {
                        final PacketBase iset =
                                xplaneVersion.getIset(inetAddress.getHostAddress(), String.valueOf(getPort()));
                        if (preferences.isBroadcastSubnet())
                        {
                            udpUtil.sendDatagramToSubnet(PacketUtil.encode(iset), UdpUtil.XPLANE_UDP_PORT);
                        }
                        else
                        {
                            udpUtil.sendDatagram(PacketUtil.encode(iset), simulatorAddress, UdpUtil.XPLANE_UDP_PORT);
                        }
                    }
                }
            }

            @Override
            public void handleRawPacket(final DatagramPacket packet)
            {
                // If forwarding is active, send the packet back out.
                if (preferences.isUdpForward())
                {
                    final String forwardAddress = preferences.getForwardAddress();
                    if (forwardAddress != null && forwardAddress.length() > 0)
                    {
                        try
                        {
                            final DatagramPacket outPacket = new DatagramPacket(
                                    packet.getData(),
                                    packet.getLength(),
                                    InetAddress.getByName(forwardAddress),
                                    UdpUtil.XPLANE_UDP_PORT
                            );
                            udpUtil.sendDatagram(
                                    outPacket.getData(), preferences.getSimulatorAddress(), UdpUtil.XPLANE_UDP_PORT);
                        }
                        catch (final UnknownHostException ex)   // NOPMD: intentional.
                        {
                            // Ignore this.
                        }
                    }
                }

                // Decode the packet and look for DATA packets.
                final PacketBase packetBase = PacketUtil.decode(packet.getData(), packet.getLength());
                if (packetBase instanceof Data)
                {
                    locationUtil.broadcastLocation((Data) packetBase);
                }
            }

            @Override
            public void handlePacket(final PacketBase packet)
            {
                // Packets are handled in the raw above.
            }
        };
        udpUtil.startReceiverThread(callbackReceiver);

        if (!eventBus.isRegistered(this))
        {
            eventBus.register(this);
        }

        // Create notification.
        Intent notificationIntent = intentProvider.getActivityIntent();
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        final Notification notification = new Notification.Builder(this)
                .setTicker(getText(R.string.notification))
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.notification))
                .setSmallIcon(R.drawable.ic_menu_plane)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .build();

        startForeground(NOTIFICATION_ID, notification);

        try
        {
            if (SettingsUtil.isMockLocationEnabled(getApplicationContext()))
            {
                locationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, false,
                        false, false, true, true, true, 0, Criteria.ACCURACY_FINE);
                locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
            }
        }
        catch (SecurityException ex)
        {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        udpUtil.close();
        stopForeground(true);

        if (eventBus.isRegistered(this))
        {
            eventBus.unregister(this);
        }

        try
        {
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
        }
        catch (Exception ex)    // NOPMD: intentional.
        {
            // Ignore this.
        }

        super.onDestroy();
    }

    /**
     * Updates the mock location from the given location.
     *
     * @param location location.
     */
    public void onEventMainThread(final Location location)
    {
        locationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER,
                LocationProvider.AVAILABLE,
                null, System.currentTimeMillis());
        locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location);
    }
}
