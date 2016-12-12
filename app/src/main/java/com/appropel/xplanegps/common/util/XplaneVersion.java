package com.appropel.xplanegps.common.util;

import com.appropel.xplane.udp.Dsel;
import com.appropel.xplane.udp.Iset;

/**
 * Interface to the various X-Plane versions.
 */
public interface XPlaneVersion
{
    /**
     * Returns a DSEL packet that configures X-Plane for the proper indexes.
     * @return DSEL packet.
     */
    Dsel getDsel();

    /**
     * Returns an ISET packet that configures X-Plane to output data to the proper location.
     * @param host host to send data to.
     * @param port port to send data to.
     * @return ISET packet.
     */
    Iset getIset(String host, String port);
}
