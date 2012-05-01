package com.appropel.xplanegps.utility;

import android.util.Log;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class NetworkUtility
{
    /** Log tag. */
    private static final String TAG = NetworkUtility.class.getName();

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
                if ("eth0".equals(networkInterface.getName()))    // TODO: Is this fragile?
                {
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
        }
        catch (Exception e)
        {
            Log.e(TAG, "Exception determining local IP address", e);
        }
        return "127.0.0.1";     // TODO: What to return here?
    }
}
