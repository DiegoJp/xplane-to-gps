package com.appropel.xplanegps.common.util;

import com.appropel.xplane.udp.Dsel;
import com.appropel.xplane.udp.Iset;
import com.appropel.xplane.udp.PacketBase;

/**
 * X-Plane version 9.
 */
public final class XPlane9 implements XPlaneVersion
{
    @Override
    public PacketBase getDsel()
    {
        return new Dsel(new int[] {3, 18, 20});
    }

    @Override
    public PacketBase getIset(final String host, final String port)
    {
        return new Iset(60, host, port);
    }
}
