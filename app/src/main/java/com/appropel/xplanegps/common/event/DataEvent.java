package com.appropel.xplanegps.common.event;

import com.appropel.xplane.udp.Data;

/**
 * Event broadcast when a new data packet is received.
 */
public final class DataEvent
{
    /** Data packet.*/
    private final Data data;

    /**
     * Constructs a new {@code DataEvent}.
     * @param data data.
     */
    public DataEvent(final Data data)
    {
        this.data = data;
    }

    public Data getData()
    {
        return data;
    }
}
