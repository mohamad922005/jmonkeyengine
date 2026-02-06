package com.jme3.math;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * SWE 261P - Project Part 2 (FSM / Functional Model Coverage)
 *
 * Model: Finite-state functional model for Vector3f.normalize()
 * Coverage target: transition coverage of Start->Clone, Start->Scale, Start->Scale*
 */
public class Vector3fNormalizeFsmTest {

    private static final float EPS = 1e-5f;

    // --- Start -> Clone (lenSq == 0) ---
    @Test
    public void testZeroVector_returnsClone() {
        Vector3f v = new Vector3f(0f, 0f, 0f);
        Vector3f r = v.normalize();

        assertNotSame("normalize() should return a new instance", v, r);
        assertEquals(0f, r.x, EPS);
        assertEquals(0f, r.y, EPS);
        assertEquals(0f, r.z, EPS);

        // Current instance is unaffected
        assertEquals(0f, v.x, EPS);
        assertEquals(0f, v.y, EPS);
        assertEquals(0f, v.z, EPS);
    }

    // --- Start -> Clone (lenSq == 1) ---
    @Test
    public void testUnitVector_returnsClone() {
        Vector3f v = new Vector3f(1f, 0f, 0f);
        Vector3f r = v.normalize();

        assertNotSame("normalize() should return a new instance", v, r);
        assertEquals(1f, r.x, EPS);
        assertEquals(0f, r.y, EPS);
        assertEquals(0f, r.z, EPS);
        assertEquals(1f, r.length(), EPS);

        // Current instance is unaffected
        assertEquals(1f, v.x, EPS);
        assertEquals(0f, v.y, EPS);
        assertEquals(0f, v.z, EPS);
    }

    // --- Start -> Scale (finite lenSq != 0,1) ---
    @Test
    public void testNonUnitFiniteVector_scalesToUnit() {
        Vector3f v = new Vector3f(3f, 4f, 0f); // len = 5
        Vector3f r = v.normalize();

        assertNotSame("normalize() should return a new instance", v, r);

        // Unit length
        assertEquals(1f, r.length(), EPS);

        // Expected normalized components
        assertEquals(0.6f, r.x, EPS);
        assertEquals(0.8f, r.y, EPS);
        assertEquals(0f, r.z, EPS);

        // Current instance is unaffected
        assertEquals(3f, v.x, EPS);
        assertEquals(4f, v.y, EPS);
        assertEquals(0f, v.z, EPS);
    }

    // --- Start -> Scale (finite but very small) ---
    @Test
    public void testVerySmallFiniteVector_scalesToUnit() {
        Vector3f v = new Vector3f(1e-18f, 0f, 0f);
        Vector3f result = v.normalize();

        assertEquals(1f, result.length(), EPS);
        assertEquals(1f, result.x, EPS);
        assertEquals(0f, result.y, EPS);
        assertEquals(0f, result.z, EPS);

        // normalize() returns a new Vector3f object in the scaling branch
        assertNotSame(v, result);
    }

    // --- Start -> Scale* (lenSq is NaN) ---
    @Test
    public void testNaNInput_propagatesNaN() {
        Vector3f v = new Vector3f(Float.NaN, 1f, 0f);
        Vector3f r = v.normalize();

        assertNotSame("normalize() should return a new instance", v, r);
        assertTrue("NaN in input should yield NaN in output", Float.isNaN(r.x) || Float.isNaN(r.y) || Float.isNaN(r.z));
    }

    // --- Start -> Scale* (lenSq is +Inf) ---
    @Test
    public void testInfinityInput_producesNaNComponent() {
        Vector3f v = new Vector3f(Float.POSITIVE_INFINITY, 0f, 0f);
        Vector3f r = v.normalize();

        assertNotSame("normalize() should return a new instance", v, r);

        // With IEEE-754 floats, Inf*0 yields NaN, so we expect at least one NaN component.
        assertTrue("Infinity input should yield NaN component(s)", Float.isNaN(r.x) || Float.isNaN(r.y) || Float.isNaN(r.z));
    }
}
