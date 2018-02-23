package com.appropel.xplanegps.common.util;

import com.appropel.xplane.udp.Dsel;
import com.appropel.xplane.udp.Ise4;
import com.appropel.xplane.udp.PacketBase;

/**
 * X-Plane version 11.
 */
public final class XPlane11 implements XPlaneVersion
{
    @Override
    public PacketBase getDsel()
    {
        return new Dsel(new int[] {3, 17, 20});
    }

    @Override
    public PacketBase getIset(final String host, final String port)
    {
        return new Ise4(64, host, port);
    }

    @Override
    public int getHeadingIndex()
    {
        return 17;
    }
}
