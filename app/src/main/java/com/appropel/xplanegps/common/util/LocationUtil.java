package com.appropel.xplanegps.common.util;

import com.appropel.xplane.udp.Data;

/**
 * Location utility.
 */
public interface LocationUtil
{
    /**
     * Broadcasts a new Location to the system based upon the given DATA packet.
     * @param data DATA packet.
     */
    void broadcastLocation(Data data);
}
