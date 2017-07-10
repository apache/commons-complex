/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.numbers.complex;

import org.apache.commons.numbers.complex.Complex;
import org.apache.commons.numbers.complex.ComplexUtils;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 */
public class ComplexUtilsTest {

    private final double inf = Double.POSITIVE_INFINITY;
    private final double negInf = Double.NEGATIVE_INFINITY;
    private final double nan = Double.NaN;
    private final double pi = Math.PI;

    private final Complex negInfInf = new Complex(negInf, inf);
    private final Complex infNegInf = new Complex(inf, negInf);
    private final Complex infInf = new Complex(inf, inf);
    private final Complex negInfNegInf = new Complex(negInf, negInf);
    private final Complex infNaN = new Complex(inf, nan);

    private static Complex c[]; // complex array with real values even and imag
    // values odd
    private static Complex cr[]; // complex array with real values consecutive
    private static Complex ci[]; // complex array with imag values consecutive
    private static double d[]; // real array with consecutive vals
    private static double di[]; // real array with consecutive vals,
    // 'interleaved' length
    private static float f[]; // real array with consecutive vals
    private static float fi[]; // real array with consec vals, interleaved
    // length
    private static double sr[]; // real component of split array, evens
    private static double si[]; // imag component of split array, odds
    private static float sfr[]; // real component of split array, float, evens
    private static float sfi[]; // imag component of split array, float, odds
    static Complex ans1, ans2; // answers to single value extraction methods
    static Complex[] ansArrayc1r, ansArrayc1i, ansArrayc2r, ansArrayc2i, ansArrayc3, ansArrayc4; // answers
    // to
    // range
    // extraction
    // methods
    static double[] ansArrayd1r, ansArrayd2r, ansArrayd1i, ansArrayd2i, ansArraydi1, ansArraydi2;
    static float[] ansArrayf1r, ansArrayf2r, ansArrayf1i, ansArrayf2i, ansArrayfi1, ansArrayfi2;
    static String msg; // error message for AssertEquals
    static Complex[][] c2d, cr2d, ci2d; // for 2d methods
    static Complex[][][] c3d, cr3d, ci3d; // for 3d methods
    static double[][] d2d, di2d, sr2d, si2d;
    static double[][][] d3d, di3d, sr3d, si3d;
    static float[][] f2d, fi2d, sfr2d, sfi2d;
    static float[][][] f3d, fi3d, sfr3d, sfi3d;

    private static void setArrays() { // initial setup method
        c = new Complex[10];
        cr = new Complex[10];
        ci = new Complex[10];
        d = new double[10];
        f = new float[10];
        di = new double[20];
        fi = new float[20];
        sr = new double[10];
        si = new double[10];
        sfr = new float[10];
        sfi = new float[10];
        c2d = new Complex[10][10];
        cr2d = new Complex[10][10];
        ci2d = new Complex[10][10];
        c3d = new Complex[10][10][10];
        cr3d = new Complex[10][10][10];
        ci3d = new Complex[10][10][10];
        d2d = new double[10][10];
        d3d = new double[10][10][10];
        f2d = new float[10][10];
        f3d = new float[10][10][10];
        sr2d = new double[10][10];
        sr3d = new double[10][10][10];
        si2d = new double[10][10];
        si3d = new double[10][10][10];
        sfr2d = new float[10][10];
        sfr3d = new float[10][10][10];
        sfi2d = new float[10][10];
        sfi3d = new float[10][10][10];
        di2d = new double[10][20];
        di3d = new double[10][10][20];
        fi2d = new float[10][20];
        fi3d = new float[10][10][20];
        for (int i = 0; i < 20; i += 2) {
            d[i / 2] = i / 2;
            f[i / 2] = i / 2;
            di[i] = i;
            di[i + 1] = i + 1;
            fi[i] = i;
            fi[i + 1] = i + 1;
            c[i / 2] = new Complex(i, i + 1);
            cr[i / 2] = new Complex(i / 2);
            ci[i / 2] = new Complex(0, i / 2);
            sr[i / 2] = i;
            si[i / 2] = i + 1;
            sfr[i / 2] = i;
            sfi[i / 2] = i + 1;
        }
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 20; j += 2) {
                d2d[i][j / 2] = 10 * i + j / 2;
                f2d[i][j / 2] = 10 * i + j / 2;
                sr2d[i][j / 2] = 10 * i + j;
                si2d[i][j / 2] = 10 * i + j + 1;
                sfr2d[i][j / 2] = 10 * i + j;
                sfi2d[i][j / 2] = 10 * i + j + 1;
                di2d[i][j] = 10 * i + j;
                di2d[i][j + 1] = 10 * i + j + 1;
                fi2d[i][j] = 10 * i + j;
                fi2d[i][j + 1] = 10 * i + j + 1;
                c2d[i][j / 2] = new Complex(10 * i + j, 10 * i + j + 1);
                cr2d[i][j / 2] = new Complex(10 * i + j / 2);
                ci2d[i][j / 2] = new Complex(0, 10 * i + j / 2);
            }
        }
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 20; k += 2) {
                    d3d[i][j][k / 2] = 100 * i + 10 * j + k / 2;
                    f3d[i][j][k / 2] = 100 * i + 10 * j + k / 2;
                    sr3d[i][j][k / 2] = 100 * i + 10 * j + k;
                    si3d[i][j][k / 2] = 100 * i + 10 * j + k + 1;
                    sfr3d[i][j][k / 2] = 100 * i + 10 * j + k;
                    sfi3d[i][j][k / 2] = 100 * i + 10 * j + k + 1;
                    di3d[i][j][k] = 100 * i + 10 * j + k;
                    di3d[i][j][k + 1] = 100 * i + 10 * j + k + 1;
                    fi3d[i][j][k] = 100 * i + 10 * j + k;
                    fi3d[i][j][k + 1] = 100 * i + 10 * j + k + 1;
                    c3d[i][j][k / 2] = new Complex(100 * i + 10 * j + k, 100 * i + 10 * j + k + 1);
                    cr3d[i][j][k / 2] = new Complex(100 * i + 10 * j + k / 2);
                    ci3d[i][j][k / 2] = new Complex(0, 100 * i + 10 * j + k / 2);
                }
            }
        }
        ansArrayc1r = new Complex[]{new Complex(3), new Complex(4), new Complex(5), new Complex(6), new Complex(7)};
        ansArrayc2r = new Complex[]{new Complex(3), new Complex(5), new Complex(7)};
        ansArrayc1i = new Complex[]{new Complex(0, 3), new Complex(0, 4), new Complex(0, 5), new Complex(0, 6),
                new Complex(0, 7)};
        ansArrayc2i = new Complex[]{new Complex(0, 3), new Complex(0, 5), new Complex(0, 7)};
        ansArrayc3 = new Complex[]{new Complex(6, 7), new Complex(8, 9), new Complex(10, 11), new Complex(12, 13),
                new Complex(14, 15)};
        ansArrayc4 = new Complex[]{new Complex(6, 7), new Complex(10, 11), new Complex(14, 15)};
        ansArrayd1r = new double[]{6, 8, 10, 12, 14};
        ansArrayd1i = new double[]{7, 9, 11, 13, 15};
        ansArrayd2r = new double[]{6, 10, 14};
        ansArrayd2i = new double[]{7, 11, 15};
        ansArrayf1r = new float[]{6, 8, 10, 12, 14};
        ansArrayf1i = new float[]{7, 9, 11, 13, 15};
        ansArrayf2r = new float[]{6, 10, 14};
        ansArrayf2i = new float[]{7, 11, 15};
        ansArraydi1 = new double[]{6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
        ansArrayfi1 = new float[]{6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
        ansArraydi2 = new double[]{6, 7, 10, 11, 14, 15};
        ansArrayfi2 = new float[]{6, 7, 10, 11, 14, 15};
        msg = "";
    }

    @Test
    public void testPolar2Complex() {
        TestUtils.assertEquals(Complex.ONE, ComplexUtils.polar2Complex(1, 0), 10e-12);
        TestUtils.assertEquals(Complex.ZERO, ComplexUtils.polar2Complex(0, 1), 10e-12);
        TestUtils.assertEquals(Complex.ZERO, ComplexUtils.polar2Complex(0, -1), 10e-12);
        TestUtils.assertEquals(Complex.I, ComplexUtils.polar2Complex(1, pi / 2), 10e-12);
        TestUtils.assertEquals(Complex.I.negate(), ComplexUtils.polar2Complex(1, -pi / 2), 10e-12);
        double r = 0;
        for (int i = 0; i < 5; i++) {
            r += i;
            double theta = 0;
            for (int j = 0; j < 20; j++) {
                theta += pi / 6;
                TestUtils.assertEquals(altPolar(r, theta), ComplexUtils.polar2Complex(r, theta), 10e-12);
            }
            theta = -2 * pi;
            for (int j = 0; j < 20; j++) {
                theta -= pi / 6;
                TestUtils.assertEquals(altPolar(r, theta), ComplexUtils.polar2Complex(r, theta), 10e-12);
            }
        }
    }

    protected Complex altPolar(double r, double theta) {
        return Complex.I.multiply(new Complex(theta, 0)).exp().multiply(new Complex(r, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPolar2ComplexIllegalModulus() {
        ComplexUtils.polar2Complex(-1, 0);
    }

    @Test
    public void testPolar2ComplexNaN() {
        TestUtils.assertSame(Complex.NaN, ComplexUtils.polar2Complex(nan, 1));
        TestUtils.assertSame(Complex.NaN, ComplexUtils.polar2Complex(1, nan));
        TestUtils.assertSame(Complex.NaN, ComplexUtils.polar2Complex(nan, nan));
    }

    @Test
    public void testPolar2ComplexInf() {
        TestUtils.assertSame(Complex.NaN, ComplexUtils.polar2Complex(1, inf));
        TestUtils.assertSame(Complex.NaN, ComplexUtils.polar2Complex(1, negInf));
        TestUtils.assertSame(Complex.NaN, ComplexUtils.polar2Complex(inf, inf));
        TestUtils.assertSame(Complex.NaN, ComplexUtils.polar2Complex(inf, negInf));
        TestUtils.assertSame(infInf, ComplexUtils.polar2Complex(inf, pi / 4));
        TestUtils.assertSame(infNaN, ComplexUtils.polar2Complex(inf, 0));
        TestUtils.assertSame(infNegInf, ComplexUtils.polar2Complex(inf, -pi / 4));
        TestUtils.assertSame(negInfInf, ComplexUtils.polar2Complex(inf, 3 * pi / 4));
        TestUtils.assertSame(negInfNegInf, ComplexUtils.polar2Complex(inf, 5 * pi / 4));
    }

    @Test
    public void testCExtract() {
        final double[] real = new double[]{negInf, -123.45, 0, 1, 234.56, pi, inf};
        final Complex[] complex = ComplexUtils.real2Complex(real);

        for (int i = 0; i < real.length; i++) {
            assertEquals(real[i], complex[i].getReal(), 0d);
        }
    }

    // EXTRACTION METHODS

    @Test
    public void testExtractionMethods() {
        setArrays();
        // Extract complex from real double array, index 3
        TestUtils.assertSame(new Complex(3), ComplexUtils.extractComplexFromRealArray(d, 3));
        // Extract complex from real float array, index 3
        TestUtils.assertSame(new Complex(3), ComplexUtils.extractComplexFromRealArray(f, 3));
        // Extract real double from complex array, index 3
        TestUtils.assertSame(6, ComplexUtils.extractRealFromComplexArray(c, 3));
        // Extract real float from complex array, index 3
        TestUtils.assertSame(6, ComplexUtils.extractRealFloatFromComplexArray(c, 3));
        // Extract complex from interleaved double array, index 3
        TestUtils.assertSame(new Complex(6, 7), ComplexUtils.extractComplexFromInterleavedArray(d, 3));
        // Extract complex from interleaved float array, index 3
        TestUtils.assertSame(new Complex(6, 7), ComplexUtils.extractComplexFromInterleavedArray(f, 3));
        // Extract interleaved double from complex array, index 3
        TestUtils.assertEquals(msg, new double[]{6, 7}, ComplexUtils.extractInterleavedFromComplexArray(c, 3),
                Math.ulp(1));
        // Extract interleaved float from complex array, index 3
        TestUtils.assertEquals(msg, new double[]{6, 7}, ComplexUtils.extractInterleavedFromComplexArray(c, 3),
                Math.ulp(1));
        if (!msg.equals("")) {
            throw new RuntimeException(msg);
        }
    }
    // REAL <-> COMPLEX

    @Test
    public void testRealToComplex() {
        setArrays();
        // Real double to complex, range 3-7, increment 1, entered as ints
        // Real double to complex, whole array
        TestUtils.assertEquals(msg, cr, ComplexUtils.real2Complex(d), Math.ulp(1.0));
        // Real float to complex, whole array
        TestUtils.assertEquals(msg, cr, ComplexUtils.real2Complex(f), Math.ulp(1.0));
        // 2d
        for (int i = 0; i < 10; i++) {
            // Real double to complex, 2d
            TestUtils.assertEquals(msg, cr2d[i], ComplexUtils.real2Complex(d2d[i]), Math.ulp(1.0));
            // Real float to complex, 2d
            TestUtils.assertEquals(msg, cr2d[i], ComplexUtils.real2Complex(f2d[i]), Math.ulp(1.0));
        }
        // 3d
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                // Real double to complex, 3d
                TestUtils.assertEquals(msg, cr3d[i][j], ComplexUtils.real2Complex(d3d[i][j]), Math.ulp(1.0));
                // Real float to complex, 3d
                TestUtils.assertEquals(msg, cr3d[i][j], ComplexUtils.real2Complex(f3d[i][j]), Math.ulp(1.0));
            }
        }
        if (!msg.equals("")) {
            throw new RuntimeException(msg);
        }
    }

    @Test
    public void testComplexToReal() {
        setArrays();
        // Real complex to double, whole array
        TestUtils.assertEquals(msg, sr, ComplexUtils.complex2Real(c), Math.ulp(1.0));
        // Real complex to float, whole array
        TestUtils.assertEquals(msg, sfr, ComplexUtils.complex2RealFloat(c), Math.ulp(1.0f));
        // 2d
        for (int i = 0; i < 10; i++) {
            // Real complex to double, 2d
            TestUtils.assertEquals(msg, sr2d[i], ComplexUtils.complex2Real(c2d[i]), Math.ulp(1.0));
            // Real complex to float, 2d
            TestUtils.assertEquals(msg, sfr2d[i], ComplexUtils.complex2RealFloat(c2d[i]), Math.ulp(1.0f));
        }
        // 3d
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                // Real complex to double, 3d
                TestUtils.assertEquals(msg, sr3d[i][j], ComplexUtils.complex2Real(c3d[i][j]), Math.ulp(1.0));
                // Real complex to float, 3d
                TestUtils.assertEquals(msg, sfr3d[i][j], ComplexUtils.complex2RealFloat(c3d[i][j]), Math.ulp(1.0f));
            }
        }
        if (!msg.equals("")) {
            throw new RuntimeException(msg);
        }
    }

    // IMAGINARY <-> COMPLEX

    @Test
    public void testImaginaryToComplex() {
        setArrays();
        // Imaginary double to complex, whole array
        TestUtils.assertEquals(msg, ci, ComplexUtils.imaginary2Complex(d), Math.ulp(1.0));
        // Imaginary float to complex, whole array
        TestUtils.assertEquals(msg, ci, ComplexUtils.imaginary2Complex(f), Math.ulp(1.0));
        // 2d
        for (int i = 0; i < 10; i++) {
            // Imaginary double to complex, 2d
            TestUtils.assertEquals(msg, ci2d[i], ComplexUtils.imaginary2Complex(d2d[i]), Math.ulp(1.0));
            // Imaginary float to complex, 2d
            TestUtils.assertEquals(msg, ci2d[i], ComplexUtils.imaginary2Complex(f2d[i]), Math.ulp(1.0));
        }
        // 3d
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                // Imaginary double to complex, 3d
                TestUtils.assertEquals(msg, ci3d[i][j], ComplexUtils.imaginary2Complex(d3d[i][j]), Math.ulp(1.0));
                // Imaginary float to complex, 3d
                TestUtils.assertEquals(msg, ci3d[i][j], ComplexUtils.imaginary2Complex(f3d[i][j]), Math.ulp(1.0));
            }
        }
        if (!msg.equals("")) {
            throw new RuntimeException(msg);
        }
    }

    @Test
    public void testComplexToImaginary() {
        setArrays();
        // Imaginary complex to double, whole array
        TestUtils.assertEquals(msg, si, ComplexUtils.complex2Imaginary(c), Math.ulp(1.0));
        // Imaginary complex to float, whole array
        TestUtils.assertEquals(msg, sfi, ComplexUtils.complex2ImaginaryFloat(c), Math.ulp(1.0f));
        // 2d
        for (int i = 0; i < 10; i++) {
            // Imaginary complex to double, 2d
            TestUtils.assertEquals(msg, si2d[i], ComplexUtils.complex2Imaginary(c2d[i]), Math.ulp(1.0));
            // Imaginary complex to float, 2d
            TestUtils.assertEquals(msg, sfi2d[i], ComplexUtils.complex2ImaginaryFloat(c2d[i]), Math.ulp(1.0f));
        }
        // 3d
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                // Imaginary complex to double, 3d
                TestUtils.assertEquals(msg, si3d[i][j], ComplexUtils.complex2Imaginary(c3d[i][j]), Math.ulp(1.0));
                // Imaginary complex to float, 3d
                TestUtils.assertEquals(msg, sfi3d[i][j], ComplexUtils.complex2ImaginaryFloat(c3d[i][j]), Math.ulp(1.0f));
            }
        }
        if (!msg.equals("")) {
            throw new RuntimeException(msg);
        }
    }

    // INTERLEAVED <-> COMPLEX

    @Test
    public void testInterleavedToComplex() {
        setArrays();
        // Interleaved double to complex, whole array
        TestUtils.assertEquals(msg, c, ComplexUtils.interleaved2Complex(di), Math.ulp(1.0));
        // Interleaved float to complex, whole array
        TestUtils.assertEquals(msg, c, ComplexUtils.interleaved2Complex(fi), Math.ulp(1.0));
        // 2d
        for (int i = 0; i < 10; i++) {
            // Interleaved double to complex, 2d
            TestUtils.assertEquals(msg, c2d[i], ComplexUtils.interleaved2Complex(di2d[i]), Math.ulp(1.0));
            // Interleaved float to complex, 2d
            TestUtils.assertEquals(msg, c2d[i], ComplexUtils.interleaved2Complex(fi2d[i]), Math.ulp(1.0));
        }
        // 3d
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                // Interleaved double to complex, 3d
                TestUtils.assertEquals(msg, c3d[i][j], ComplexUtils.interleaved2Complex(di3d[i][j]), Math.ulp(1.0));
                // Interleaved float to complex, 3d
                TestUtils.assertEquals(msg, c3d[i][j], ComplexUtils.interleaved2Complex(fi3d[i][j]), Math.ulp(1.0));
            }
        }
        if (!msg.equals("")) {
            throw new RuntimeException(msg);
        }
    }

    @Test
    public void testComplexToInterleaved() {
        setArrays();
        TestUtils.assertEquals(msg, di, ComplexUtils.complex2Interleaved(c), Math.ulp(1.0));
        // Interleaved complex to float, whole array
        TestUtils.assertEquals(msg, fi, ComplexUtils.complex2InterleavedFloat(c), Math.ulp(1.0f));
        // 2d
        for (int i = 0; i < 10; i++) {
            // Interleaved complex to double, 2d
            TestUtils.assertEquals(msg, di2d[i], ComplexUtils.complex2Interleaved(c2d[i]), Math.ulp(1.0));
            // Interleaved complex to float, 2d
            TestUtils.assertEquals(msg, fi2d[i], ComplexUtils.complex2InterleavedFloat(c2d[i]), Math.ulp(1.0f));
        }
        // 3d
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                // Interleaved complex to double, 3d
                TestUtils.assertEquals(msg, di3d[i][j], ComplexUtils.complex2Interleaved(c3d[i][j]), Math.ulp(1.0));
                // Interleaved complex to float, 3d
                TestUtils.assertEquals(msg, fi3d[i][j], ComplexUtils.complex2InterleavedFloat(c3d[i][j]), Math.ulp(1.0f));
            }
        }
        if (!msg.equals("")) {
            throw new RuntimeException(msg);
        }
    }

    // SPLIT TO COMPLEX
    @Test
    public void testSplit2Complex() {
        setArrays();
        // Split double to complex, whole array
        TestUtils.assertEquals(msg, c, ComplexUtils.split2Complex(sr, si), Math.ulp(1.0));

        // 2d
        for (int i = 0; i < 10; i++) {
            // Split double to complex, 2d
            TestUtils.assertEquals(msg, c2d[i], ComplexUtils.split2Complex(sr2d[i], si2d[i]), Math.ulp(1.0));
        }
        // 3d
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                // Split double to complex, 3d
                TestUtils.assertEquals(msg, c3d[i][j], ComplexUtils.split2Complex(sr3d[i][j], si3d[i][j]), Math.ulp(1.0));
            }
        }
        if (!msg.equals("")) {
            throw new RuntimeException(msg);
        }
    }

    // INITIALIZATION METHODS

    @Test
    public void testInitialize() {
        Complex[] c = new Complex[10];
        ComplexUtils.initialize(c);
        for (Complex cc : c) {
            TestUtils.assertEquals(new Complex(0, 0), cc, Math.ulp(0));
        }
    }

    @Test
    public void testcomplex2InterleavedFloatTakingTwoAndTwoWithPositive() {

        Complex[][] complexArray = new Complex[0][9];

        try {
            ComplexUtils.complex2InterleavedFloat(complexArray, 2);
            fail("Expecting exception: IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Out of range: 2", e.getMessage());
            assertEquals(ComplexUtils.class.getName(), e.getStackTrace()[0].getClassName());
        }

    }

    @Test
    public void testcomplex2ImaginaryTakingComplexArrayArrayArrayThrowsNullPointerException() {

        Complex[][][] complexArray = new Complex[1][2][5];
        complexArray[0] = new Complex[7][1];

        try {
            ComplexUtils.complex2Imaginary(complexArray);
            fail("Expecting exception: NullPointerException");
        } catch (NullPointerException e) {
            assertEquals(ComplexUtils.class.getName(), e.getStackTrace()[0].getClassName());
        }

    }


    @Test
    public void testcomplex2InterleavedFloatTakingTwoAndTwoWithEmptyArrayAndNegative() {

        Complex[][][] complexArray = new Complex[0][7][3];

        try {
            ComplexUtils.complex2InterleavedFloat(complexArray, (-1));
            fail("Expecting exception: IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Out of range: -1", e.getMessage());
            assertEquals(ComplexUtils.class.getName(), e.getStackTrace()[0].getClassName());
        }

    }


    @Test
    public void testInterleavedThreeComplexTakingTwoAndTwoWithEmptyArray() {

        float[][][] floatArray = new float[0][5][8];

        try {
            ComplexUtils.interleaved2Complex(floatArray, 334);
            fail("Expecting exception: IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Out of range: 334", e.getMessage());
            assertEquals(ComplexUtils.class.getName(), e.getStackTrace()[0].getClassName());
        }

    }


    @Test
    public void testExtractComplexFromInterleavedArrayTakingTwoAndTwoThrowsArrayIndexOutOfBoundsException() {

        double[] doubleArray = new double[5];
        doubleArray[0] = 0.0;
        doubleArray[1] = 3553.05;

        try {
            ComplexUtils.extractComplexFromInterleavedArray(doubleArray, (-2145818362));
            fail("Expecting exception: ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            assertEquals("3330572", e.getMessage());
            assertEquals(ComplexUtils.class.getName(), e.getStackTrace()[0].getClassName());
        }

    }


    @Test
    public void testInterleavedThrowsIllegalArgumentException() {

        try {
            ComplexUtils.interleaved2Complex((float[][][]) null, (-2501));
            fail("Expecting exception: IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Out of range: -2501", e.getMessage());
            assertEquals(ComplexUtils.class.getName(), e.getStackTrace()[0].getClassName());
        }

    }


    @Test
    public void testPolarThreeComplexTakingThreeDoubleArraysThrowsIllegalArgumentException() {

        double[] doubleArray = new double[8];
        doubleArray[0] = 682.369;
        doubleArray[1] = 612.16397804415;
        doubleArray[2] = 1.0;
        doubleArray[3] = (-252.151672108609);
        doubleArray[4] = 0.0;
        doubleArray[5] = Double.POSITIVE_INFINITY;

        try {
            ComplexUtils.polar2Complex(doubleArray, doubleArray);
            fail("Expecting exception: IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Modulus is negative: -252.151672108609", e.getMessage());
            assertEquals(ComplexUtils.class.getName(), e.getStackTrace()[0].getClassName());
        }

    }

    @Test
    public void testcomplex2InterleavedFloatTakingTwoAndTwoWithNegative() {

        Complex[][] complexArray = new Complex[0][8];

        try {
            ComplexUtils.complex2InterleavedFloat(complexArray, (-3231));
            fail("Expecting exception: IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Out of range: -3231", e.getMessage());
            assertEquals(ComplexUtils.class.getName(), e.getStackTrace()[0].getClassName());
        }

    }

    @Test
    public void testInterleavedThreeComplexTakingTwoAndTwoWithEmptyArrayAndNegative() {

        float[][] floatArray = new float[0][3];

        try {
            ComplexUtils.interleaved2Complex(floatArray, (-4026));
            fail("Expecting exception: IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Out of range: -4026", e.getMessage());
            assertEquals(ComplexUtils.class.getName(), e.getStackTrace()[0].getClassName());
        }

    }

    @Test
    public void testInterleavedThreeComplexTakingTwoAndTwoWithNullAndPositive() {

        try {
            ComplexUtils.interleaved2Complex((float[][]) null, 2146551443);
            fail("Expecting exception: IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Out of range: 2146551443", e.getMessage());
            assertEquals(ComplexUtils.class.getName(), e.getStackTrace()[0].getClassName());
        }

    }


    @Test
    public void testcomplex2ImaginaryTakingComplexArrayArray() {

        double[] doubleArray = new double[4];
        doubleArray[1] = 2005.05225;
        doubleArray[3] = 2005.05225;
        ComplexUtils.real2Complex(doubleArray);
        Complex[][][] complexArray = new Complex[1][2][5];
        Complex[][] complexArrayTwo = new Complex[0][1];
        complexArray[0] = complexArrayTwo;
        ComplexUtils.complex2Imaginary(complexArray);
        ComplexUtils.initialize(complexArray);

        try {
            ComplexUtils.complex2Interleaved(complexArray, (-2984));
            fail("Expecting exception: IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Out of range: -2984", e.getMessage());
            assertEquals(ComplexUtils.class.getName(), e.getStackTrace()[0].getClassName());
        }

    }


    @Test  //I consider this to be a defect.
    public void testAbsThrowsNullPointerException() {

        float[] floatArray = new float[0];
        Complex[] complexArray = ComplexUtils.interleaved2Complex(floatArray);
        Complex[][] complexArrayTwo = new Complex[5][8];
        complexArrayTwo[1] = complexArray;
        complexArrayTwo[2] = complexArray;
        complexArrayTwo[0] = complexArray;
        ComplexUtils.complex2Interleaved(complexArrayTwo);

        try {
            ComplexUtils.abs(complexArrayTwo[4]);
            fail("Expecting exception: NullPointerException");
        } catch (NullPointerException e) {
            assertEquals(ComplexUtils.class.getName(), e.getStackTrace()[0].getClassName());
        }

    }

}
