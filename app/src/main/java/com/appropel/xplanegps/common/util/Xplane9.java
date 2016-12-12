package com.appropel.xplanegps.common.util;

import com.appropel.xplane.udp.Dsel;
import com.appropel.xplane.udp.Iset;

/**
 * X-Plane version 9.
 */
public final class XPlane9 implements XPlaneVersion
{
    @Override
    public Dsel getDsel()
    {
        return new Dsel(new int[] {3, 18, 20});
    }

    @Override
    public Iset getIset(final String host, final String port)
    {
        return new Iset(60, host, port);
    }
}
