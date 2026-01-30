package com.jme3.math;

import org.junit.Test;
import static org.junit.Assert.*;

public class Vector3fNormalizePartitionTest {

    private static final float EPS = 1e-5f;

    @Test
    public void testZeroVectorNormalization() {
        Vector3f v = new Vector3f(0f, 0f, 0f);
        Vector3f result = v.normalize();

        assertEquals(0f, result.x, EPS);
        assertEquals(0f, result.y, EPS);
        assertEquals(0f, result.z, EPS);
        assertNotSame("normalize() should return a new object", v, result);
    }

    @Test
    public void testNormalVectorNormalization() {
        Vector3f v = new Vector3f(3f, 4f, 0f);
        Vector3f result = v.normalize();

        assertEquals(1f, result.length(), EPS);
        assertEquals(0.6f, result.x, EPS);
        assertEquals(0.8f, result.y, EPS);
        assertEquals(0f, result.z, EPS);

        // Original should remain unchanged
        assertEquals(3f, v.x, EPS);
        assertEquals(4f, v.y, EPS);
    }

    @Test
    public void testLargeVectorNormalization() {
        Vector3f v = new Vector3f(1e6f, 1e6f, 1e6f);
        Vector3f result = v.normalize();

        assertEquals(1f, result.length(), EPS);
        assertFalse(Float.isNaN(result.x));
        assertFalse(Float.isInfinite(result.x));
    }

    @Test
    public void testSmallVectorNormalization() {
        Vector3f v = new Vector3f(1e-6f, 1e-6f, 1e-6f);
        Vector3f result = v.normalize();

        assertEquals(1f, result.length(), EPS);
        assertFalse(Float.isNaN(result.x));
        assertFalse(Float.isInfinite(result.x));
    }
}
