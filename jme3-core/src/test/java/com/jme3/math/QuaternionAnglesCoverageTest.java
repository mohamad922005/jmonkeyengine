package com.jme3.math;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Structural (white-box) coverage tests for Quaternion angle conversions.
 *
 * Goal: exercise major branches in:
 *  - Quaternion#fromAngles(float[]) argument validation
 *  - Quaternion#toAngles(float[]) singularity branches (north pole / south pole / general case)
 *  - Quaternion#fromRotationMatrix(...) scaling-compensation branches
 *
 */
public class QuaternionAnglesCoverageTest {

    private static final float EPS = 1e-5f;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void fromAngles_arrayLengthNot3_throwsIllegalArgumentException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Angles array must have three elements");

        Quaternion q = new Quaternion();
        q.fromAngles(new float[] { 0f, 0f }); // invalid length (2)
    }

    @Test
    public void toAngles_nullInput_allocatesArrayOfLength3() {
        Quaternion q = new Quaternion(); // identity
        float[] angles = q.toAngles(null);

        assertNotNull(angles);
        assertEquals(3, angles.length);
        // identity should produce approximately zeros
        assertEquals(0f, angles[0], EPS);
        assertEquals(0f, angles[1], EPS);
        assertEquals(0f, angles[2], EPS);
    }

    @Test
    public void toAngles_arrayLengthNot3_throwsIllegalArgumentException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Angles array must have three elements");

        Quaternion q = new Quaternion();
        q.toAngles(new float[] { 0f, 0f, 0f, 0f }); // invalid length (4)
    }

    @Test
    public void toAngles_northPoleSingularity_branchIsCovered() {
        // Choose components so test = x*y + z*w = 0.5 and unit = 1.0
        // This satisfies: test > 0.499 * unit
        Quaternion q = new Quaternion(0.5f, 0.5f, 0.5f, 0.5f);

        float[] angles = q.toAngles(new float[3]);

        // Branch sets:
        //  angles[1] = 2 * atan2(x, w)
        //  angles[2] = HALF_PI
        //  angles[0] = 0
        assertEquals(0f, angles[0], 1e-4f);
        assertEquals(FastMath.HALF_PI, angles[2], 1e-4f);
        assertEquals(2f * FastMath.atan2(0.5f, 0.5f), angles[1], 1e-4f);
    }

    @Test
    public void toAngles_southPoleSingularity_branchIsCovered() {
        // Choose components so test = x*y + z*w = -0.5 and unit = 1.0
        // This satisfies: test < -0.499 * unit
        Quaternion q = new Quaternion(0.5f, -0.5f, -0.5f, 0.5f);

        float[] angles = q.toAngles(new float[3]);

        // Branch sets:
        //  angles[1] = -2 * atan2(x, w)
        //  angles[2] = -HALF_PI
        //  angles[0] = 0
        assertEquals(0f, angles[0], 1e-4f);
        assertEquals(-FastMath.HALF_PI, angles[2], 1e-4f);
        assertEquals(-2f * FastMath.atan2(0.5f, 0.5f), angles[1], 1e-4f);
    }

    @Test
    public void toAngles_generalCase_branchIsCovered() {
        // Identity falls into the general case (test == 0).
        Quaternion q = new Quaternion(0f, 0f, 0f, 1f);

        float[] angles = q.toAngles(new float[3]);

        assertEquals(0f, angles[0], EPS);
        assertEquals(0f, angles[1], EPS);
        assertEquals(0f, angles[2], EPS);
    }

    @Test
    public void fromRotationMatrix_positiveScalingIsCompensated() {
        Matrix3f scaled = new Matrix3f(2f, 0f, 0f, 0f, 3f, 0f, 0f, 0f, 4f);

        Quaternion q = new Quaternion();
        q.fromRotationMatrix(scaled);

        // Approximate identity quaternion (0,0,0,1)
        assertEquals(0f, q.getX(), EPS);
        assertEquals(0f, q.getY(), EPS);
        assertEquals(0f, q.getZ(), EPS);
        assertEquals(1f, q.getW(), EPS);
    }

}
