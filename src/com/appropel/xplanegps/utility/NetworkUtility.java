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

    /**
     * No public constructor.
     */
    private NetworkUtility()
    {
    }

    /**
     * Returns the IP address of this device, if able to determine.
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
                Enumeration<InetAddress> addressEnumeration = networkInterface.getInetAddresses();
                while (addressEnumeration.hasMoreElements())
                {
                    InetAddress address = addressEnumeration.nextElement();
                    if (address instanceof Inet4Address)    // TODO: How to deal with IPv6?
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
