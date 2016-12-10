package com.appropel.xplanegps.common.util;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for Expressions.
 */
public class ExpressionsTest
{
    @Test
    public void testIpAddress()
    {
        assertTrue(Expressions.isValidIpAddress("127.0.0.1"));
        assertTrue(Expressions.isValidIpAddress("10.0.0.22"));
        assertTrue(Expressions.isValidIpAddress("255.255.255.255"));
        assertFalse(Expressions.isValidIpAddress("256.255.255.255"));
        assertFalse(Expressions.isValidIpAddress("255.255.255.256"));
        assertFalse(Expressions.isValidIpAddress("a.b.c.d"));
    }
}
