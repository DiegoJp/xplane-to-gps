package com.appropel.xplanegps.common.util;

import com.appropel.xplane.udp.Dsel;
import com.appropel.xplane.udp.Iset;

/**
 * X-Plane version 11.
 */
public final class XPlane11 implements XPlaneVersion
{
    @Override
    public Dsel getDsel()
    {
        return new Dsel(new int[] {3, 17, 20});
    }

    @Override
    public Iset getIset(final String host, final String port)
    {
        return new Iset(42, host, port);    // TODO: this is not the right index
    }
}
