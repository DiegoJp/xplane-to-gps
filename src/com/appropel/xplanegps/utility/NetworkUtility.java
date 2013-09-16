package com.appropel.xplanegps.utility;

import android.util.Log;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Useful utilities.
 */
public final class NetworkUtility
{
    /** Log tag. */
    private static final String TAG = NetworkUtility.class.getName();

    /** Loopback address. */
    private static final String LOOPBACK_SUBNET = "127";

    /** Wireless network device name. */
    private static final String WIRELESS_DEVICE = "wlan";

    /**
     * No public constructor.
     */
    private NetworkUtility()
    {
    }

    /**
     * Returns the IP address of this device, if able to determine. The address returned will be for the wireless
     * adapter.
     * @return String form of IP address.
     */
    public static String getLocalIpAddress()
    {
        try
        {
            Enumeration<NetworkInterface> interfaceEnumeration = NetworkInterface.getNetworkInterfaces();
            while (interfaceEnumeration.hasMoreElements())
            {
                NetworkInterface networkInterface = interfaceEnumeration.nextElement();
                if (networkInterface.getName().indexOf(WIRELESS_DEVICE) < 0)
                {
                    continue;   // Looking specifically for wireless device
                }
                Enumeration<InetAddress> addressEnumeration = networkInterface.getInetAddresses();
                while (addressEnumeration.hasMoreElements())
                {
                    InetAddress address = addressEnumeration.nextElement();
                    if (address instanceof Inet4Address && !address.getHostAddress().startsWith(LOOPBACK_SUBNET))
                        // TODO: How to deal with IPv6?
                    {
                        return address.getHostAddress();
                    }
                }
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "Exception determining local IP address", e);
        }
        return "UNKNOWN";     // TODO: What to return here?
    }
}
