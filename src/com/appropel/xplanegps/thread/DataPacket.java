package com.appropel.xplanegps.thread;

import java.nio.ByteBuffer;

/**
 * Encapsulates an X-Plane data packet.
 */
public final class DataPacket
{
    /** Length of data structure. */
    public static final int LENGTH = 36;

    /** index into the list of variables you can output from the Data Output screen in X-Plane. */
    private int index;

    /** the up to 8 numbers you see in the data output screen associated with that selection. */
    private float[] values = new float[8];

    /**
     * Constructs a new <code>DataPacket</code>.
     * @param buffer ByteBuffer to pull values from.
     * @param start start index in the buffer.
     */
    public DataPacket(final ByteBuffer buffer, final int start)
    {
        int i = start;
        index = buffer.getInt(i);
        for (int v = 0; v < 8; ++v)
        {
            i += 4;
            values[v] = buffer.getFloat(i);
        }
    }

    /**
     * Returns the index of this packet.
     * @return index.
     */
    public int getIndex()
    {
        return index;
    }

    /**
     * Returns the values.
     * @return values.
     */
    public float[] getValues()
    {
        return values;
    }
}
