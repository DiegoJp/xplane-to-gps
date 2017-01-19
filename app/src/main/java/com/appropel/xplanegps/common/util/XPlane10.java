package com.appropel.xplanegps.common.util;

import com.appropel.xplane.udp.Dsel;
import com.appropel.xplane.udp.Iset;
import com.appropel.xplane.udp.PacketBase;

/**
 * X-Plane version 10.
 */
public final class XPlane10 implements XPlaneVersion
{
    @Override
    public PacketBase getDsel()
    {
        return new Dsel(new int[] {3, 17, 20});
    }

    @Override
    public PacketBase getIset(final String host, final String port)
    {
        return new Iset(64, host, port);
    }
}
