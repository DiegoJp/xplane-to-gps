package com.appropel.xplanegps.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for obtaining the X-Plane version.
 */
public final class XPlaneVersionUtil
{
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(XPlaneVersionUtil.class);

    /** Version 9. */
    private static final XPlaneVersion VERSION_9 = new XPlane9();

    /** Version 10. */
    private static final XPlaneVersion VERSION_10 = new XPlane10();

    /** Version 11. */
    private static final XPlaneVersion VERSION_11 = new XPlane11();

    private XPlaneVersionUtil()
    {
        // Utility class.
    }

    /**
     * Returns the appropriate version.
     * @param version version number - 9, 10, or 11.
     * @return X-Plane version.
     */
    public static XPlaneVersion getXPlaneVersion(int version)
    {
        switch (version)
        {
            case 9:
                return VERSION_9;
            case 10:
                return VERSION_10;
            case 11:
                return VERSION_11;
            default:
                throw new IllegalArgumentException("Unknown X-Plane version " + version);
        }
    }

    /**
     * Returns the appropriate version.
     * @param version version number - 9, 10, or 11.
     * @return X-Plane version.
     */
    public static XPlaneVersion getXPlaneVersion(String version)
    {
        return getXPlaneVersion(Integer.valueOf(version));
    }
}
