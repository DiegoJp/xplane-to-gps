package com.appropel.xplanegps.common.util;

import java.util.regex.Pattern;

/**
 * Regular expressions.
 */
public final class Expressions
{
    /** IP address. */
    public static final String IP_ADDRESS =
            "^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])"
                    + "\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)"
                    + "\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)"
                    + "\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$";

    /** IP address pattern. */
    public static final Pattern IP_ADDRESS_PATTERN = Pattern.compile(IP_ADDRESS);

    private Expressions()
    {
        // Utility class.
    }
}
