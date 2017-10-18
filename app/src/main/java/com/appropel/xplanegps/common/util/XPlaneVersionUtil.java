package com.appropel.xplanegps.common.util;

/**
 * Utility for obtaining the X-Plane version.
 */
public final class XPlaneVersionUtil
{
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
    public static XPlaneVersion getXPlaneVersion(final int version)
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
    public static XPlaneVersion getXPlaneVersion(final String version)
    {
        return getXPlaneVersion(Integer.parseInt(version));
    }
}
