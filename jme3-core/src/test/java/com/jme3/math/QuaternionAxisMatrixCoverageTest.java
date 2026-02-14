package com.jme3.math;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * More structural (white-box) coverage tests for Quaternion: - toAngleAxis(...) : covers sqrLength==0 vs
 * general, and axisStore null/non-null - fromRotationMatrix(...) : covers trace>0 and the 3 "largest
 * diagonal" branches
 */
public class QuaternionAxisMatrixCoverageTest {

    private static final float EPS = 1e-5f;


    // toAngleAxis(Vector3f)

    @Test
    public void toAngleAxis_identity_axisStoreNonNull_hitsZeroSqrLengthBranchAndSetsAxis() {
        Quaternion q = new Quaternion(0f, 0f, 0f, 1f); // identity
        Vector3f axis = new Vector3f();

        float angle = q.toAngleAxis(axis);

        assertEquals(0f, angle, EPS);
        assertEquals(1f, axis.x, EPS);
        assertEquals(0f, axis.y, EPS);
        assertEquals(0f, axis.z, EPS);
    }

    @Test
    public void toAngleAxis_identity_axisStoreNull_hitsZeroSqrLengthBranchNoNpe() {
        Quaternion q = new Quaternion(0f, 0f, 0f, 1f); // identity

        float angle = q.toAngleAxis(null);

        assertEquals(0f, angle, EPS);
    }

    @Test
    public void toAngleAxis_generalCase_axisStoreNonNull_returnsAngleAndNormalizedAxis() {
        // 90 degrees about Z: axis=(0,0,1), angle=PI/2
        // quaternion: (x,y,z,w) = (0,0,sin(pi/4),cos(pi/4))
        float s = FastMath.sin(FastMath.QUARTER_PI);
        float c = FastMath.cos(FastMath.QUARTER_PI);
        Quaternion q = new Quaternion(0f, 0f, s, c);

        Vector3f axis = new Vector3f();
        float angle = q.toAngleAxis(axis);

        assertEquals(FastMath.HALF_PI, angle, 1e-4f);
        assertEquals(0f, axis.x, 1e-4f);
        assertEquals(0f, axis.y, 1e-4f);
        assertEquals(1f, axis.z, 1e-4f);
    }

    @Test
    public void toAngleAxis_generalCase_axisStoreNull_returnsAngleNoNpe() {
        float s = FastMath.sin(FastMath.QUARTER_PI);
        float c = FastMath.cos(FastMath.QUARTER_PI);
        Quaternion q = new Quaternion(0f, 0f, s, c);

        float angle = q.toAngleAxis(null);

        assertEquals(FastMath.HALF_PI, angle, 1e-4f);
    }


    // fromRotationMatrix(...)
    // These 4 tests are designed to hit the classic branch structure:
    // if (trace > 0) { ... }
    // else if (m00 is largest) { ... }
    // else if (m11 is largest) { ... }
    // else { m22 is largest ... }

    @Test
    public void fromRotationMatrix_identity_hitsTracePositiveBranch() {
        Matrix3f I = new Matrix3f(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f);

        Quaternion q = new Quaternion().fromRotationMatrix(I);

        assertEquals(0f, q.getX(), EPS);
        assertEquals(0f, q.getY(), EPS);
        assertEquals(0f, q.getZ(), EPS);
        assertEquals(1f, q.getW(), EPS);
    }

    @Test
    public void fromRotationMatrix_rot180AboutX_hitsM00LargestBranch() {
        // Rotation 180° about X => diag(1, -1, -1)
        Matrix3f Rx180 = new Matrix3f(1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, -1f);

        Quaternion q = new Quaternion().fromRotationMatrix(Rx180);

        // Expected quaternion for 180° about X is (1,0,0,0) up to sign
        assertQuaternionEqualsUpToSign(new Quaternion(1f, 0f, 0f, 0f), q, 1e-4f);
    }

    @Test
    public void fromRotationMatrix_rot180AboutY_hitsM11LargestBranch() {
        // Rotation 180° about Y => diag(-1, 1, -1)
        Matrix3f Ry180 = new Matrix3f(-1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, -1f);

        Quaternion q = new Quaternion().fromRotationMatrix(Ry180);

        // Expected quaternion for 180° about Y is (0,1,0,0) up to sign
        assertQuaternionEqualsUpToSign(new Quaternion(0f, 1f, 0f, 0f), q, 1e-4f);
    }

    @Test
    public void fromRotationMatrix_rot180AboutZ_hitsM22LargestBranch() {
        // Rotation 180° about Z => diag(-1, -1, 1)
        Matrix3f Rz180 = new Matrix3f(-1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 1f);

        Quaternion q = new Quaternion().fromRotationMatrix(Rz180);

        // Expected quaternion for 180° about Z is (0,0,1,0) up to sign
        assertQuaternionEqualsUpToSign(new Quaternion(0f, 0f, 1f, 0f), q, 1e-4f);
    }

    // Helpers
    private static void assertQuaternionEqualsUpToSign(Quaternion expected, Quaternion actual, float eps) {
        // q and -q represent the same rotation, so accept either.
        boolean direct = Math.abs(expected.getX() - actual.getX()) <= eps
                && Math.abs(expected.getY() - actual.getY()) <= eps
                && Math.abs(expected.getZ() - actual.getZ()) <= eps
                && Math.abs(expected.getW() - actual.getW()) <= eps;

        boolean neg = Math.abs(expected.getX() + actual.getX()) <= eps
                && Math.abs(expected.getY() + actual.getY()) <= eps
                && Math.abs(expected.getZ() + actual.getZ()) <= eps
                && Math.abs(expected.getW() + actual.getW()) <= eps;

        assertTrue("Quaternion mismatch (even up to sign). expected=" + expected + " actual=" + actual,
                direct || neg);
    }
}
