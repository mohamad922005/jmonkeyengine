package com.jme3.math;

import org.junit.Test;
import static org.junit.Assert.*;

public class Vector3fNormalizePartitionTest {

    // P1 – Zero vector
    @Test
    public void testNormalizeZeroVector() {
        Vector3f v = new Vector3f(0, 0, 0);
        Vector3f result = v.normalize();
        assertEquals(0f, result.length(), 0f);
    }

    // P2 – Normal vector
    @Test
    public void testNormalizeRegularVector() {
        Vector3f v = new Vector3f(3, 4, 0);
        Vector3f result = v.normalize();
        assertEquals(1.0f, result.length(), 1e-4f);
    }

    // P3 – Large vector
    @Test
    public void testNormalizeLargeVector() {
        Vector3f v = new Vector3f(1000000f, 1000000f, 1000000f);
        Vector3f result = v.normalize();
        assertEquals(1.0f, result.length(), 1e-3f);
    }

    // P4 – Small vector
    @Test
    public void testNormalizeSmallVector() {
        Vector3f v = new Vector3f(0.00001f, 0, 0);
        Vector3f result = v.normalize();
        assertEquals(1.0f, result.length(), 1e-3f);
    }
}
