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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.numbers.core.Precision;

/**
 * Cartesian representation of a complex number, i.e. a number which has both a
 * real and imaginary part.
 *
 * <p>This class is immutable. All arithmetic will create a new instance for the
 * result.</p>
 *
 * <p>Arithmetic in this class conforms to the C99 standard for complex numbers
 * defined in ISO/IEC 9899, Annex G. All methods have been named using the equivalent
 * method in ISO C99. The behaviour for special cases is listed as defined in C99.</p>
 *
 * <p>For functions \( f \) which obey the conjugate equality \( conj(f(z)) = f(conj(z)) \),
 * the specifications for the upper half-plane imply the specifications for the lower
 * half-plane.</p>
 *
 * <p>For functions that are either odd, \( f(z) = -f(-z) \), or even, \( f(z) =  f(-z) \)
 * the specifications for the first quadrant imply the specifications for the other three
 * quadrants.</p>
 *
 * @see <a href="http://www.open-std.org/JTC1/SC22/WG14/www/standards">
 *    ISO/IEC 9899 - Programming languages - C</a>
 */
public final class Complex implements Serializable  {
    /**
     * A complex number representing \( i \), the square root of \( -1 \).
     *
     * <p>\( (0 + i 1) \).
     */
    public static final Complex I = new Complex(0, 1);
    /**
     * A complex number representing one.
     *
     * <p>\( (1 + i 0) \).
     */
    public static final Complex ONE = new Complex(1, 0);
    /**
     * A complex number representing zero.
     *
     * <p>\( (0 + i 0) \).
     */
    public static final Complex ZERO = new Complex(0, 0);

    /** A complex number representing {@code NaN + i NaN}. */
    private static final Complex NAN = new Complex(Double.NaN, Double.NaN);
    /** &pi;/2. */
    private static final double PI_OVER_2 = 0.5 * Math.PI;
    /** &pi;/4. */
    private static final double PI_OVER_4 = 0.25 * Math.PI;
    /** Mask an integer number to even by discarding the lowest bit. */
    private static final int MASK_INT_TO_EVEN = ~0x1;
    /** Natural logarithm of 2 (ln(2)). */
    private static final double LN_2 = Math.log(2);
    /** Base 10 logarithm of 10 divided by 2 (log10(e)/2). */
    private static final double LOG_10E_O_2 = Math.log10(Math.E) / 2;
    /** Base 10 logarithm of 2 (log10(2)). */
    private static final double LOG10_2 = Math.log10(2);
    /** {@code 1/2}. */
    private static final double HALF = 0.5;
    /** {@code sqrt(2)}. */
    private static final double ROOT2 = Math.sqrt(2);
    /** The number of bits of precision of the mantissa of a {@code double} + 1: {@code 54}. */
    private static final double PRECISION_1 = 54;
    /** The bit representation of {@code -0.0}. */
    private static final long NEGATIVE_ZERO_LONG_BITS = Double.doubleToLongBits(-0.0);

    /**
     * Crossover point to switch computation for asin/acos factor A.
     * This has been updated from the 1.5 value used by Hull et al to 10
     * as used in boost::math::complex.
     * @see <a href="https://svn.boost.org/trac/boost/ticket/7290">Boost ticket 7290</a>
     */
    private static final double A_CROSSOVER = 10;
    /** Crossover point to switch computation for asin/acos factor B. */
    private static final double B_CROSSOVER = 0.6471;
    /**
     * The safe maximum double value {@code x} to avoid loss of precision in asin/acos.
     * Equal to sqrt(M) / 8 in Hull, et al (1997) with M the largest normalised floating-point value.
     */
    private static final double SAFE_MAX = Math.sqrt(Double.MAX_VALUE) / 8;
    /**
     * The safe minimum double value {@code x} to avoid loss of precision/underflow in asin/acos.
     * Equal to sqrt(u) * 4 in Hull, et al (1997) with u the smallest normalised floating-point value.
     */
    private static final double SAFE_MIN = Math.sqrt(Double.MIN_NORMAL) * 4;
    /**
     * The safe maximum double value {@code x} to avoid loss of precision in atanh.
     * Equal to sqrt(M) / 2 with M the largest normalised floating-point value.
     */
    private static final double SAFE_UPPER = Math.sqrt(Double.MAX_VALUE) / 2;
    /**
     * The safe minimum double value {@code x} to avoid loss of precision/underflow in atanh.
     * Equal to sqrt(u) * 2 with u the smallest normalised floating-point value.
     */
    private static final double SAFE_LOWER = Math.sqrt(Double.MIN_NORMAL) * 2;

    /** Serializable version identifier. */
    private static final long serialVersionUID = 20180201L;

    /**
     * The size of the buffer for {@link #toString()}.
     *
     * <p>The longest double will require a sign, a maximum of 17 digits, the decimal place
     * and the exponent, e.g. for max value this is 24 chars: -1.7976931348623157e+308.
     * Set the buffer size to twice this and round up to a power of 2 thus
     * allowing for formatting characters. The size is 64.
     */
    private static final int TO_STRING_SIZE = 64;
    /** The minimum number of characters in the format. This is 5, e.g. {@code "(0,0)"}. */
    private static final int FORMAT_MIN_LEN = 5;
    /** {@link #toString() String representation}. */
    private static final char FORMAT_START = '(';
    /** {@link #toString() String representation}. */
    private static final char FORMAT_END = ')';
    /** {@link #toString() String representation}. */
    private static final char FORMAT_SEP = ',';
    /** The minimum number of characters before the separator. This is 2, e.g. {@code "(0"}. */
    private static final int BEFORE_SEP = 2;

    /** The imaginary part. */
    private final double imaginary;
    /** The real part. */
    private final double real;

    /**
     * Define a constructor for a Complex.
     * This is used in functions that implement trigonomic identities.
     */
    @FunctionalInterface
    private interface ComplexConstructor {
        /**
         * Create a complex number given the real and imaginary parts.
         *
         * @param real Real part.
         * @param imaginary Imaginary part.
         * @return {@code Complex} object.
         */
        Complex create(double real, double imaginary);
    }

    /**
     * Define a unary operation on a double.
     * This is used in the log() and log10() functions.
     */
    @FunctionalInterface
    private interface UnaryOperation {
        /**
         * Apply an operation to a value.
         *
         * @param value The value.
         * @return The result.
         */
        double apply(double value);
    }

    /**
     * Private default constructor.
     *
     * @param real Real part.
     * @param imaginary Imaginary part.
     */
    private Complex(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }

    /**
     * Create a complex number given the real and imaginary parts.
     *
     * @param real Real part.
     * @param imaginary Imaginary part.
     * @return {@code Complex} number.
     */
    public static Complex ofCartesian(double real, double imaginary) {
        return new Complex(real, imaginary);
    }

    /**
     * Creates a complex number from its polar representation using modulus {@code rho} (\( \rho \))
     * and phase angle {@code theta} (\( \theta \)).
     *
     * \[ x = \rho \cos(\theta) \\
     *    y = \rho \sin(\theta) \]
     *
     * <p>Requires that {@code rho} is non-negative and non-NaN and {@code theta} is finite;
     * otherwise returns a complex with NaN real and imaginary parts. A value of {@code -0.0} is
     * considered negative and an invalid modulus.
     *
     * <p>A non-NaN complex number constructed using this method will satisfy the following
     * to within floating-point error when {@code theta} is in the range
     * \( -\pi\ \lt \theta \leq \pi \):</p>
     * <pre>
     *  Complex.ofPolar(rho, theta).abs() == rho
     *  Complex.ofPolar(rho, theta).arg() == theta </pre>
     *
     * <p>If {@code rho} is infinite then the resulting parts may be infinite or NaN
     * following the rules for double arithmetic, for example:</p>
     *
     * <ul>
     * <li>{@code ofPolar(}\( -0.0 \){@code , }\( 0 \){@code ) = }\( \text{NaN} + i \text{NaN} \)
     * <li>{@code ofPolar(}\( 0.0 \){@code , }\( 0 \){@code ) = }\( 0 + i 0 \)
     * <li>{@code ofPolar(}\( 1 \){@code , }\( 0 \){@code ) = }\( 1 + i 0 \)
     * <li>{@code ofPolar(}\( 1 \){@code , }\( \pi \){@code ) = }\( -1 + i \sin(\pi) \)
     * <li>{@code ofPolar(}\( \infty \){@code , }\( \pi \){@code ) = }\( -\infty + i \infty \)
     * <li>{@code ofPolar(}\( \infty \){@code , }\( 0 \){@code ) = }\( -\infty + i \text{NaN} \)
     * <li>{@code ofPolar(}\( \infty \){@code , }\( -\frac{\pi}{4} \){@code ) = }\( \infty - i \infty \)
     * <li>{@code ofPolar(}\( \infty \){@code , }\( 5\frac{\pi}{4} \){@code ) = }\( -\infty - i \infty \)
     * </ul>
     *
     * <p>This method is the functional equivalent of the C++ method {@code std::polar}.
     *
     * @param rho The modulus of the complex number.
     * @param theta The argument of the complex number.
     * @return {@code Complex} number.
     * @see <a href="http://mathworld.wolfram.com/PolarCoordinates.html">Polar Coordinates</a>
     */
    public static Complex ofPolar(double rho, double theta) {
        // Require finite theta and non-negative, non-nan rho
        if (!Double.isFinite(theta) || negative(rho) || Double.isNaN(rho)) {
            return NAN;
        }
        final double x = rho * Math.cos(theta);
        final double y = rho * Math.sin(theta);
        return new Complex(x, y);
    }

    /**
     * Create a complex cis number. This is also known as the complex exponential:
     *
     * \[ \text{cis}(x) = e^{ix} = \cos(x) + i \sin(x) \]
     *
     * @param x {@code double} to build the cis number.
     * @return {@code Complex} cis number.
     * @see <a href="http://mathworld.wolfram.com/Cis.html">Cis</a>
     */
    public static Complex ofCis(double x) {
        return new Complex(Math.cos(x), Math.sin(x));
    }

    /**
     * Returns a {@code Complex} instance representing the specified string {@code s}.
     *
     * <p>If {@code s} is {@code null}, then a {@code NullPointerException} is thrown.
     *
     * <p>The string must be in a format compatible with that produced by
     * {@link #toString() Complex.toString()}.
     * The format expects a start and end parentheses surrounding two numeric parts split
     * by a separator. Leading and trailing spaces are allowed around each numeric part.
     * Each numeric part is parsed using {@link Double#parseDouble(String)}. The parts
     * are interpreted as the real and imaginary parts of the complex number.
     *
     * <p>Examples of valid strings and the equivalent {@code Complex} are shown below:
     *
     * <pre>
     * "(0,0)"             = Complex.ofCartesian(0, 0)
     * "(0.0,0.0)"         = Complex.ofCartesian(0, 0)
     * "(-0.0, 0.0)"       = Complex.ofCartesian(-0.0, 0)
     * "(-1.23, 4.56)"     = Complex.ofCartesian(-123, 4.56)
     * "(1e300,-1.1e-2)"   = Complex.ofCartesian(1e300, -1.1e-2)
     * </pre>
     *
     * @param s String representation.
     * @return {@code Complex} number.
     * @throws NullPointerException if the string is null.
     * @throws NumberFormatException if the string does not contain a parsable complex number.
     * @see Double#parseDouble(String)
     * @see #toString()
     */
    public static Complex parse(String s) {
        final int len = s.length();
        if (len < FORMAT_MIN_LEN) {
            throw parsingException("Expected format",
                FORMAT_START + "real" + FORMAT_SEP + "imaginary" + FORMAT_END, null);
        }

        // Confirm start: '('
        if (s.charAt(0) != FORMAT_START) {
            throw parsingException("Expected start", FORMAT_START, null);
        }

        // Confirm end: ')'
        if (s.charAt(len - 1) != FORMAT_END) {
            throw parsingException("Expected end", FORMAT_END, null);
        }

        // Confirm separator ',' is between at least 2 characters from
        // either end: "(x,x)"
        // Count back from the end ignoring the last 2 characters.
        final int sep = s.lastIndexOf(FORMAT_SEP, len - 3);
        if (sep < BEFORE_SEP) {
            throw parsingException("Expected separator between two numbers", FORMAT_SEP, null);
        }

        // Should be no more separators
        if (s.indexOf(FORMAT_SEP, sep + 1) != -1) {
            throw parsingException("Incorrect number of parts, expected only 2 using separator",
                FORMAT_SEP, null);
        }

        // Try to parse the parts

        final String rePart = s.substring(1, sep);
        final double re;
        try {
            re = Double.parseDouble(rePart);
        } catch (final NumberFormatException ex) {
            throw parsingException("Could not parse real part", rePart, ex);
        }

        final String imPart = s.substring(sep + 1, len - 1);
        final double im;
        try {
            im = Double.parseDouble(imPart);
        } catch (final NumberFormatException ex) {
            throw parsingException("Could not parse imaginary part", imPart, ex);
        }

        return ofCartesian(re, im);
    }

    /**
     * Returns {@code true} if either the real <em>or</em> imaginary component of the complex number is NaN
     * <em>and</em> the complex number is not infinite.
     *
     * <p>Note that in contrast to {@link Double#isNaN()}:
     * <ul>
     *   <li>There is more than one complex number that can return {@code true}.
     *   <li>Different representations of NaN can be distinguished by the
     *       {@link #equals(Object) Complex.equals(Object)} method.
     * </ul>
     *
     * @return {@code true} if this instance contains NaN and no infinite parts.
     * @see Double#isNaN(double)
     * @see #isInfinite()
     * @see #equals(Object) Complex.equals(Object)
     */
    public boolean isNaN() {
        if (Double.isNaN(real) || Double.isNaN(imaginary)) {
            return !isInfinite();
        }
        return false;
    }

    /**
     * Returns {@code true} if either real or imaginary component of the complex number is infinite.
     *
     * <p>Note: A complex or imaginary value with at least one infinite part is regarded
     * as an infinity (even if its other part is a NaN).</p>
     *
     * @return {@code true} if this instance contains an infinite value.
     * @see Double#isInfinite(double)
     */
    public boolean isInfinite() {
        return Double.isInfinite(real) || Double.isInfinite(imaginary);
    }

    /**
     * Returns {@code true} if both real and imaginary component of the complex number are finite.
     *
     * @return {@code true} if this instance contains finite values.
     * @see Double#isFinite(double)
     */
    public boolean isFinite() {
        return Double.isFinite(real) && Double.isFinite(imaginary);
    }

    /**
     * Returns projection of this complex number onto the Riemann sphere.
     *
     * <p>\( z \) projects to \( z \), except that all complex infinities (even those
     * with one infinite part and one NaN part) project to positive infinity on the real axis.
     *
     * If \( z \) has an infinite part, then {@code z.proj()} shall be equivalent to:</p>
     * <pre>
     *   return Complex.ofCartesian(Double.POSITIVE_INFINITY, Math.copySign(0.0, z.imag());
     * </pre>
     *
     * @return \( z \) projected onto the Riemann sphere.
     * @see #isInfinite()
     * @see <a href="http://pubs.opengroup.org/onlinepubs/9699919799/functions/cproj.html">
     * IEEE and ISO C standards: cproj</a>
     */
    public Complex proj() {
        if (isInfinite()) {
            return new Complex(Double.POSITIVE_INFINITY, Math.copySign(0.0, imaginary));
        }
        return this;
    }

    /**
     * Returns the absolute value of this complex number. This is also called complex norm, modulus,
     * or magnitude.
     *
     * <p>\[ \text{abs}(x + i y) = \sqrt{(x^2 + y^2)} \]
     *
     * <p>If either component is infinite then the result is positive infinity. If either
     * component is NaN and this is not {@link #isInfinite() infinite} then the result is NaN.
     *
     * <p>This code follows the
     * <a href="http://www.iso-9899.info/wiki/The_Standard">ISO C Standard</a>, Annex G,
     * in calculating the returned value using the {@code hypot(x, y)} method for complex
     * \( x + i y \).
     *
     * @return The absolute value.
     * @see #isInfinite()
     * @see #isNaN()
     * @see Math#hypot(double, double)
     * @see <a href="http://mathworld.wolfram.com/ComplexModulus.html">Complex modulus</a>
     */
    public double abs() {
        // Delegate
        return Math.hypot(real, imaginary);
    }

    /**
     * Returns the squared norm value of this complex number. This is also called the absolute
     * square.
     *
     * <p>\[ \text{norm}(x + i y) = x^2 + y^2 \]
     *
     * <p>If either component is infinite then the result is positive infinity. If either
     * component is NaN and this is not {@link #isInfinite() infinite} then the result is NaN.
     *
     * <p>This method will return the square of {@link #abs()}. It can be used as a faster
     * alternative for ranking by magnitude although overflow to infinity will create equal
     * ranking for values that may be still distinguished by {@code abs()}.
     *
     * @return The square norm value.
     * @see #isInfinite()
     * @see #isNaN()
     * @see #abs()
     * @see <a href="http://mathworld.wolfram.com/AbsoluteSquare.html">Absolute square</a>
     */
    public double norm() {
        if (isInfinite()) {
            return Double.POSITIVE_INFINITY;
        }
        return real * real + imaginary * imaginary;
    }

    /**
     * Returns a {@code Complex} whose value is {@code (this + addend)}.
     * Implements the formula:
     *
     * <p>\[ (a + i b) + (c + i d) = (a + c) + i (b + d) \]
     *
     * @param  addend Value to be added to this complex number.
     * @return {@code this + addend}.
     * @see <a href="http://mathworld.wolfram.com/ComplexAddition.html">Complex Addition</a>
     */
    public Complex add(Complex addend) {
        return new Complex(real + addend.real,
                           imaginary + addend.imaginary);
    }

    /**
     * Returns a {@code Complex} whose value is {@code (this + addend)},
     * with {@code addend} interpreted as a real number.
     * Implements the formula:
     *
     * <p>\[ (a + i b) + c = (a + c) + i b \]
     *
     * <p>This method is included for compatibility with ISO C99 which defines arithmetic between
     * real-only and complex numbers.</p>
     *
     * <p>Note: This method preserves the sign of the imaginary component \( b \) if it is {@code -0.0}.
     * The sign would be lost if adding \( (c + i 0) \) using
     * {@link #add(Complex) add(Complex.ofCartesian(addend, 0))} since
     * {@code -0.0 + 0.0 = 0.0}.
     *
     * @param addend Value to be added to this complex number.
     * @return {@code this + addend}.
     * @see #add(Complex)
     * @see #ofCartesian(double, double)
     */
    public Complex add(double addend) {
        return new Complex(real + addend, imaginary);
    }

    /**
     * Returns a {@code Complex} whose value is {@code (this + addend)},
     * with {@code addend} interpreted as an imaginary number.
     * Implements the formula:
     *
     * <p>\[ (a + i b) + i d = a + i (b + d) \]
     *
     * <p>This method is included for compatibility with ISO C99 which defines arithmetic between
     * imaginary-only and complex numbers.</p>
     *
     * <p>Note: This method preserves the sign of the real component \( a \) if it is {@code -0.0}.
     * The sign would be lost if adding \( (0 + i d) \) using
     * {@link #add(Complex) add(Complex.ofCartesian(0, addend))} since
     * {@code -0.0 + 0.0 = 0.0}.
     *
     * @param addend Value to be added to this complex number.
     * @return {@code this + addend}.
     * @see #add(Complex)
     * @see #ofCartesian(double, double)
     */
    public Complex addImaginary(double addend) {
        return new Complex(real, imaginary + addend);
    }

    /**
     * Returns the
     * <a href="http://mathworld.wolfram.com/ComplexConjugate.html">conjugate</a>
     * \( \overline{z} \) of this complex number \( z \).
     *
     * <p>\[ z           = x + i y \\
     *      \overline{z} = x - i y \]
     *
     * @return The conjugate (\( \overline{z} \)) of this complex number.
     */
    public Complex conj() {
        return new Complex(real, -imaginary);
    }

    /**
     * Returns a {@code Complex} whose value is {@code (this / divisor)}.
     * Implements the formula:
     *
     * <p>\[ \frac{a + i b}{c + i d} = \frac{(ac + bd) + i (bc - ad)}{c^2+d^2} \]
     *
     * <p>Re-calculates NaN result values to recover infinities as specified in C99 standard G.5.1.
     *
     * @param divisor Value by which this complex number is to be divided.
     * @return {@code this / divisor}.
     * @see <a href="http://mathworld.wolfram.com/ComplexDivision.html">Complex Division</a>
     */
    public Complex divide(Complex divisor) {
        return divide(real, imaginary, divisor.real, divisor.imaginary);
    }

    /**
     * Returns a {@code Complex} whose value is:
     * <pre>
     * <code>
     *   a + i b     (ac + bd) + i (bc - ad)
     *   -------  =  -----------------------
     *   c + i d            c<sup>2</sup> + d<sup>2</sup>
     * </code>
     * </pre>
     *
     * <p>Recalculates to recover infinities as specified in C99
     * standard G.5.1. Method is fully in accordance with
     * C++11 standards for complex numbers.</p>
     *
     * <p>Note: In the event of divide by zero this method produces the same result
     * as dividing by a real-only zero using {@link #divide(double)}.
     *
     * @param re1 Real component of first number.
     * @param im1 Imaginary component of first number.
     * @param re2 Real component of second number.
     * @param im2 Imaginary component of second number.
     * @return (a + i b) / (c + i d).
     * @see <a href="http://mathworld.wolfram.com/ComplexDivision.html">Complex Division</a>
     * @see #divide(double)
     */
    private static Complex divide(double re1, double im1, double re2, double im2) {
        double a = re1;
        double b = im1;
        double c = re2;
        double d = im2;
        int ilogbw = 0;
        // Get the exponent to scale the divisor.
        final int exponent = getMaxExponent(c, d);
        if (exponent <= Double.MAX_EXPONENT) {
            ilogbw = exponent;
            c = Math.scalb(c, -ilogbw);
            d = Math.scalb(d, -ilogbw);
        }
        final double denom = c * c + d * d;
        double x = Math.scalb((a * c + b * d) / denom, -ilogbw);
        double y = Math.scalb((b * c - a * d) / denom, -ilogbw);
        // Recover infinities and zeros that computed as NaN+iNaN
        // the only cases are nonzero/zero, infinite/finite, and finite/infinite, ...
        // --------------
        // Modification from the listing in ISO C99 G.5.1 (8):
        // Prevent overflow in (a * c + b * d) and (b * c - a * d).
        // It is only the sign that is important. not the magnitude.
        // --------------
        if (Double.isNaN(x) && Double.isNaN(y)) {
            if ((denom == 0.0) &&
                    (!Double.isNaN(a) || !Double.isNaN(b))) {
                // nonzero/zero
                // This case produces the same result as divide by a real-only zero
                // using divide(+/-0.0).
                x = Math.copySign(Double.POSITIVE_INFINITY, c) * a;
                y = Math.copySign(Double.POSITIVE_INFINITY, c) * b;
            } else if ((Double.isInfinite(a) || Double.isInfinite(b)) &&
                    Double.isFinite(c) && Double.isFinite(d)) {
                // infinite/finite
                a = boxInfinity(a);
                b = boxInfinity(b);
                x = Double.POSITIVE_INFINITY * computeACplusBD(a, b, c, d);
                y = Double.POSITIVE_INFINITY * computeBCminusAD(a, b, c, d);
            } else if ((Double.isInfinite(c) || Double.isInfinite(d)) &&
                    Double.isFinite(a) && Double.isFinite(b)) {
                // finite/infinite
                c = boxInfinity(c);
                d = boxInfinity(d);
                x = 0.0 * computeACplusBD(a, b, c, d);
                y = 0.0 * computeBCminusAD(a, b, c, d);
            }
        }
        return new Complex(x, y);
    }

    /**
     * Compute {@code a*c + b*d} without overflow.
     * It is assumed: either {@code a} an\( b \)b} or {@code c} and {@code d} are
     * either zero or one (i.e. a boxed infinity); and the sign of the result is important,
     * not the value.
     *
     * @param a the a
     * @param b the b
     * @param c the c
     * @param d the d
     * @return The result
     */
    private static double computeACplusBD(double a, double b, double c, double d) {
        final double ac = a * c;
        final double bd = b * d;
        final double result = ac + bd;
        return Double.isFinite(result) ?
            result :
            // Overflow. Just divide by 2 as it is the sign of the result that matters.
            ac * 0.5 + bd * 0.5;
    }

    /**
     * Compute {@code b*c - a*d} without overflow.
     * It is assumed: either {@code a} and {@code b} or {@code c} and {@code d} are
     * either zero or one (i.e. a boxed infinity); and the sign of the result is important,
     * not the value.
     *
     * @param a the a
     * @param b the b
     * @param c the c
     * @param d the d
     * @return The result
     */
    private static double computeBCminusAD(double a, double b, double c, double d) {
        final double bc = b * c;
        final double ad = a * d;
        final double result = bc - ad;
        return Double.isFinite(result) ?
            result :
            // Overflow. Just divide by 2 as it is the sign of the result that matters.
            bc * 0.5 - ad * 0.5;
    }

    /**
     * Returns a {@code Complex} whose value is {@code (this / divisor)},
     * with {@code divisor} interpreted as a real number.
     * Implements the formula:
     *
     * <p>\[ \frac{a + i b}{c} = \frac{a}{c} + i \frac{b}{c} \]
     *
     * <p>This method is included for compatibility with ISO C99 which defines arithmetic between
     * real-only and complex numbers.</p>
     *
     * <p>Note: This method should be preferred over using
     * {@link #divide(Complex) divide(Complex.ofCartesian(divisor, 0))}. Division
     * can generate signed zeros if {@code this} complex has zeros for the real
     * and/or imaginary component, or the divisor is infinity. The summation of signed zeros
     * in {@link #divide(Complex)} may create zeros in the result that differ in sign
     * from the equivalent call to divide by a real-only number.
     *
     * @param  divisor Value by which this complex number is to be divided.
     * @return {@code this / divisor}.
     * @see #divide(Complex)
     */
    public Complex divide(double divisor) {
        return new Complex(real / divisor, imaginary / divisor);
    }

    /**
     * Returns a {@code Complex} whose value is {@code (this / divisor)},
     * with {@code divisor} interpreted as an imaginary number.
     * Implements the formula:
     *
     * <p>\[ \frac{a + i b}{id} = \frac{b}{d} - i \frac{a}{d} \]
     *
     * <p>This method is included for compatibility with ISO C99 which defines arithmetic between
     * imaginary-only and complex numbers.</p>
     *
     * <p>Note: This method should be preferred over using
     * {@link #divide(Complex) divide(Complex.ofCartesian(0, divisor))}. Division
     * can generate signed zeros if {@code this} complex has zeros for the real
     * and/or imaginary component, or the divisor is infinity. The summation of signed zeros
     * in {@link #divide(Complex)} may create zeros in the result that differ in sign
     * from the equivalent call to divide by an imaginary-only number.
     *
     * <p>Warning: This method will generate a different result from
     * {@link #divide(Complex) divide(Complex.ofCartesian(0, divisor))} if the divisor is zero.
     * In this case the divide method using a zero-valued Complex will produce the same result
     * as dividing by a real-only zero. The output from dividing by imaginary zero will create
     * infinite and NaN values in the same component parts as the output from
     * {@code this.divide(Complex.ZERO).multiplyImaginary(1)}, however the sign
     * of some infinity values may be negated.
     *
     * @param  divisor Value by which this complex number is to be divided.
     * @return {@code this / divisor}.
     * @see #divide(Complex)
     * @see #divide(double)
     */
    public Complex divideImaginary(double divisor) {
        return new Complex(imaginary / divisor, -real / divisor);
    }

    /**
     * Test for equality with another object. If the other object is a {@code Complex} then a
     * comparison is made of the real and imaginary parts; otherwise {@code false} is returned.
     *
     * <p>If both the real and imaginary parts of two complex numbers
     * are exactly the same the two {@code Complex} objects are considered to be equal.
     * For this purpose, two {@code double} values are considered to be
     * the same if and only if the method {@link Double #doubleToLongBits(double)}
     * returns the identical {@code long} value when applied to each.
     *
     * <p>Note that in most cases, for two instances of class
     * {@code Complex}, {@code c1} and {@code c2}, the
     * value of {@code c1.equals(c2)} is {@code true} if and only if
     *
     * <pre>
     * {@code c1.getReal() == c2.getReal() && c1.getImaginary() == c2.getImaginary()}</pre>
     *
     * <p>also has the value {@code true}. However, there are exceptions:
     *
     * <ul>
     *  <li>
     *   Instances that contain {@code NaN} values in the same part
     *   are considered to be equal for that part, even though {@code Double.NaN==Double.NaN}
     *   has the value {@code false}.
     *  </li>
     *  <li>
     *   Instances that share a {@code NaN} value in one part
     *   but have different values in the other part are <em>not</em> considered equal.
     *  </li>
     *  <li>
     *   Instances that contain different representations of zero in the same part
     *   are <em>not</em> considered to be equal for that part, even though {@code -0.0==0.0}
     *   has the value {@code true}.
     *  </li>
     * </ul>
     *
     * <p>The behavior is the same as if the components of the two complex numbers were passed
     * to {@link java.util.Arrays#equals(double[], double[]) Arrays.equals(double[], double[])}:
     *
     * <pre>
     *  Arrays.equals(new double[]{c1.getReal(), c1.getImaginary()},
     *                new double[]{c2.getReal(), c2.getImaginary()}); </pre>
     *
     * @param other Object to test for equality with this instance.
     * @return {@code true} if the objects are equal, {@code false} if object
     * is {@code null}, not an instance of {@code Complex}, or not equal to
     * this instance.
     * @see java.lang.Double#doubleToLongBits(double)
     * @see java.util.Arrays#equals(double[], double[])
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof Complex) {
            final Complex c = (Complex) other;
            return equals(real, c.real) &&
                equals(imaginary, c.imaginary);
        }
        return false;
    }

    /**
     * Gets a hash code for the complex number.
     *
     * <p>The behavior is the same as if the components of the complex number were passed
     * to {@link java.util.Arrays#hashCode(double[]) Arrays.hashCode(double[])}:
     * <pre>
     *  {@code Arrays.hashCode(new double[] {getReal(), getImaginary()})}
     * </pre>
     *
     * @return A hash code value for this object.
     * @see java.util.Arrays#hashCode(double[]) Arrays.hashCode(double[])
     */
    @Override
    public int hashCode() {
        return 31 * (31 + Double.hashCode(real)) + Double.hashCode(imaginary);
    }

    /**
     * Gets the imaginary part.
     *
     * @return The imaginary part.
     */
    public double getImaginary() {
        return imaginary;
    }

    /**
     * Gets the imaginary part (C++ grammar).
     *
     * @return The imaginary part.
     * @see #getImaginary()
     */
    public double imag() {
        return getImaginary();
    }

    /**
     * Gets the real part.
     *
     * @return The real part.
     */
    public double getReal() {
        return real;
    }

     /**
     * Gets the real part (C++ grammar).
     *
     * @return The real part.
     * @see #getReal()
     */
    public double real() {
        return getReal();
    }

    /**
     * Returns a {@code Complex} whose value is {@code this * factor}.
     * Implements the formula:
     *
     * <p>\[ (a + i b)(c + i d) = (ac - bd) + i (ad + bc) \]
     *
     * <p>Recalculates to recover infinities as specified in C99 standard G.5.1.
     *
     * @param  factor Value to be multiplied by this complex number.
     * @return {@code this * factor}.
     * @see <a href="http://mathworld.wolfram.com/ComplexMultiplication.html">Complex Muliplication</a>
     */
    public Complex multiply(Complex factor) {
        return multiply(real, imaginary, factor.real, factor.imaginary);
    }

    /**
     * Returns a {@code Complex} whose value is:
     * <pre>
     *   (a + i b)(c + i d) = (ac - bd) + i (ad + bc)
     * </pre>
     *
     * <p>Recalculates to recover infinities as specified in C99 standard G.5.1.
     *
     * @param re1 Real component of first number.
     * @param im1 Imaginary component of first number.
     * @param re2 Real component of second number.
     * @param im2 Imaginary component of second number.
     * @return (a + b i)(c + d i).
     */
    private static Complex multiply(double re1, double im1, double re2, double im2) {
        double a = re1;
        double b = im1;
        double c = re2;
        double d = im2;
        final double ac = a * c;
        final double bd = b * d;
        final double ad = a * d;
        final double bc = b * c;
        double x = ac - bd;
        double y = ad + bc;

        // --------------
        // NaN can occur if:
        // - any of (a,b,c,d) are NaN (for NaN or Infinite complex numbers)
        // - a multiplication of infinity by zero (ac,bd,ad,bc).
        // - a subtraction of infinity from infinity (e.g. ac - bd)
        //   Note that (ac,bd,ad,bc) can be infinite due to overflow.
        //
        // Detect a NaN result and perform correction.
        //
        // Modification from the listing in ISO C99 G.5.1 (6)
        // Do not correct infinity multiplied by zero. This is left as NaN.
        // --------------

        if (Double.isNaN(x) && Double.isNaN(y)) {
            // Recover infinities that computed as NaN+iNaN ...
            boolean recalc = false;
            if ((Double.isInfinite(a) || Double.isInfinite(b)) &&
                isNotZero(c, d)) {
                // This complex is infinite.
                // "Box" the infinity and change NaNs in the other factor to 0.
                a = boxInfinity(a);
                b = boxInfinity(b);
                c = changeNaNtoZero(c);
                d = changeNaNtoZero(d);
                recalc = true;
            }
            // (c, d) may have been corrected so do not use factor.isInfinite().
            if ((Double.isInfinite(c) || Double.isInfinite(d)) &&
                isNotZero(a, b)) {
                // This other complex is infinite.
                // "Box" the infinity and change NaNs in the other factor to 0.
                c = boxInfinity(c);
                d = boxInfinity(d);
                a = changeNaNtoZero(a);
                b = changeNaNtoZero(b);
                recalc = true;
            }
            if (!recalc && (Double.isInfinite(ac) || Double.isInfinite(bd) ||
                            Double.isInfinite(ad) || Double.isInfinite(bc))) {
                // The result overflowed to infinity.
                // Recover infinities from overflow by changing NaNs to 0 ...
                a = changeNaNtoZero(a);
                b = changeNaNtoZero(b);
                c = changeNaNtoZero(c);
                d = changeNaNtoZero(d);
                recalc = true;
            }
            if (recalc) {
                x = Double.POSITIVE_INFINITY * (a * c - b * d);
                y = Double.POSITIVE_INFINITY * (a * d + b * c);
            }
        }
        return new Complex(x, y);
    }

    /**
     * Box values for the real or imaginary component of an infinite complex number.
     * Any infinite value will be returned as one. Non-infinite values will be returned as zero.
     * The sign is maintained.
     *
     * <pre>
     *  inf  =  1
     * -inf  = -1
     *  x    =  0
     * -x    = -0
     * </pre>
     *
     * @param component the component
     * @return The boxed value
     */
    private static double boxInfinity(double component) {
        return Math.copySign(Double.isInfinite(component) ? 1.0 : 0.0, component);
    }

    /**
     * Checks if the complex number is not zero.
     *
     * @param real the real component
     * @param imaginary the imaginary component
     * @return true if the complex is not zero
     */
    private static boolean isNotZero(double real, double imaginary) {
        // The use of equals is deliberate.
        // This method must distinguish NaN from zero thus ruling out:
        // (real != 0.0 || imaginary != 0.0)
        return !(real == 0.0 && imaginary == 0.0);
    }

    /**
     * Change NaN to zero preserving the sign; otherwise return the value.
     *
     * @param value the value
     * @return The new value
     */
    private static double changeNaNtoZero(double value) {
        return Double.isNaN(value) ? Math.copySign(0.0, value) : value;
    }

    /**
     * Returns a {@code Complex} whose value is {@code this * factor}, with {@code factor}
     * interpreted as a real number.
     * Implements the formula:
     *
     * <p>\[ (a + i b) c =  (ac) + i (bc) \]
     *
     * <p>This method is included for compatibility with ISO C99 which defines arithmetic between
     * real-only and complex numbers.</p>
     *
     * <p>Note: This method should be preferred over using
     * {@link #multiply(Complex) multiply(Complex.ofCartesian(factor, 0))}. Multiplication
     * can generate signed zeros if either {@code this} complex has zeros for the real
     * and/or imaginary component, or if the factor is zero. The summation of signed zeros
     * in {@link #multiply(Complex)} may create zeros in the result that differ in sign
     * from the equivalent call to multiply by a real-only number.
     *
     * @param  factor Value to be multiplied by this complex number.
     * @return {@code this * factor}.
     * @see #multiply(Complex)
     */
    public Complex multiply(double factor) {
        return new Complex(real * factor, imaginary * factor);
    }

    /**
     * Returns a {@code Complex} whose value is {@code this * factor}, with {@code factor}
     * interpreted as an imaginary number.
     * Implements the formula:
     *
     * <p>\[ (a + i b) id = (-bd) + i (ad) \]
     *
     * <p>This method can be used to compute the multiplication of this complex number \( z \)
     * by \( i \). This should be used in preference to
     * {@link #multiply(Complex) multiply(Complex.I)} with or without {@link #negate() negation}:</p>
     *
     * \[ iz = (-b + i a) \\
     *   -iz = (b - i a) \]
     *
     * <p>This method is included for compatibility with ISO C99 which defines arithmetic between
     * imaginary-only and complex numbers.</p>
     *
     * <p>Note: This method should be preferred over using
     * {@link #multiply(Complex) multiply(Complex.ofCartesian(0, factor))}. Multiplication
     * can generate signed zeros if either {@code this} complex has zeros for the real
     * and/or imaginary component, or if the factor is zero. The summation of signed zeros
     * in {@link #multiply(Complex)} may create zeros in the result that differ in sign
     * from the equivalent call to multiply by an imaginary-only number.
     *
     * @param  factor Value to be multiplied by this complex number.
     * @return {@code this * factor}.
     * @see #multiply(Complex)
     */
    public Complex multiplyImaginary(double factor) {
        return new Complex(-imaginary * factor, real * factor);
    }

    /**
     * Returns a {@code Complex} whose value is the negation of both the real and imaginary parts
     * of complex number \( z \).
     *
     * @return \( -z \).
     */
    public Complex negate() {
        return new Complex(-real, -imaginary);
    }

    /**
     * Returns a {@code Complex} whose value is {@code (this - subtrahend)}.
     * Implements the formula:
     *
     * <p>\[ (a + i b) - (c + i d) = (a - c) + i (b - d) \]
     *
     * @param  subtrahend Value to be subtracted from this complex number.
     * @return {@code this - subtrahend}.
     * @see <a href="http://mathworld.wolfram.com/ComplexSubtraction.html">Complex Subtraction</a>
     */
    public Complex subtract(Complex subtrahend) {
        return new Complex(real - subtrahend.real,
                           imaginary - subtrahend.imaginary);
    }

    /**
     * Returns a {@code Complex} whose value is {@code (this - subtrahend)},
     * with {@code subtrahend} interpreted as a real number.
     * Implements the formula:
     *
     * <p>\[ (a + i b) - c = (a - c) + i b \]
     *
     * <p>This method is included for compatibility with ISO C99 which defines arithmetic between
     * real-only and complex numbers.</p>
     *
     * @param  subtrahend Value to be subtracted from this complex number.
     * @return {@code this - subtrahend}.
     * @see #subtract(Complex)
     */
    public Complex subtract(double subtrahend) {
        return new Complex(real - subtrahend, imaginary);
    }

    /**
     * Returns a {@code Complex} whose value is {@code (this - subtrahend)},
     * with {@code subtrahend} interpreted as an imaginary number.
     * Implements the formula:
     *
     * <p>\[ (a + i b) - i d = a + i (b - d) \]
     *
     * <p>This method is included for compatibility with ISO C99 which defines arithmetic between
     * imaginary-only and complex numbers.</p>
     *
     * @param  subtrahend Value to be subtracted from this complex number.
     * @return {@code this - subtrahend}.
     * @see #subtract(Complex)
     */
    public Complex subtractImaginary(double subtrahend) {
        return new Complex(real, imaginary - subtrahend);
    }

    /**
     * Returns a {@code Complex} whose value is {@code (minuend - this)},
     * with {@code minuend} interpreted as a real number.
     * Implements the formula:
     * \[ c - (a + i b) = (c - a) - i b \]
     *
     * <p>This method is included for compatibility with ISO C99 which defines arithmetic between
     * real-only and complex numbers.</p>
     *
     * <p>Note: This method inverts the sign of the imaginary component \( b \) if it is {@code 0.0}.
     * The sign would not be inverted if subtracting from \( c + i 0 \) using
     * {@link #subtract(Complex) Complex.ofCartesian(minuend, 0).subtract(this))} since
     * {@code 0.0 - 0.0 = 0.0}.
     *
     * @param  minuend Value this complex number is to be subtracted from.
     * @return {@code minuend - this}.
     * @see #subtract(Complex)
     * @see #ofCartesian(double, double)
     */
    public Complex subtractFrom(double minuend) {
        return new Complex(minuend - real, -imaginary);
    }

    /**
     * Returns a {@code Complex} whose value is {@code (this - subtrahend)},
     * with {@code minuend} interpreted as an imaginary number.
     * Implements the formula:
     * \[ i d - (a + i b) = -a + i (d - b) \]
     *
     * <p>This method is included for compatibility with ISO C99 which defines arithmetic between
     * imaginary-only and complex numbers.</p>
     *
     * <p>Note: This method inverts the sign of the real component \( a \) if it is {@code 0.0}.
     * The sign would not be inverted if subtracting from \( 0 + i d \) using
     * {@link #subtract(Complex) Complex.ofCartesian(0, minuend).subtract(this))} since
     * {@code 0.0 - 0.0 = 0.0}.
     *
     * @param  minuend Value this complex number is to be subtracted from.
     * @return {@code this - subtrahend}.
     * @see #subtract(Complex)
     * @see #ofCartesian(double, double)
     */
    public Complex subtractFromImaginary(double minuend) {
        return new Complex(-real, minuend - imaginary);
    }

    /**
     * Returns the
     * <a href="http://mathworld.wolfram.com/InverseCosine.html">
     * inverse cosine</a> of this complex number.
     *
     * <p>\[ \cos^{-1}(z) = \frac{\pi}{2} + i \left(\ln{iz + \sqrt{1 - z^2}}\right) \]
     *
     * <p>The inverse cosine of \( z \) is in the range \( [0, \infty) \) along the real axis and
     * in the range \( [-\pi, \pi] \) along the imaginary axis. Special cases:
     *
     * <ul>
     * <li>{@code z.conj().acos() == z.acos().conj()}.
     * <li>If {@code z} is ±0 + i0, returns π/2 − i0.
     * <li>If {@code z} is ±0 + iNaN, returns π/2 + iNaN.
     * <li>If {@code z} is x + i∞ for finite x, returns π/2 − i∞.
     * <li>If {@code z} is x + iNaN, returns NaN + iNaN.
     * <li>If {@code z} is −∞ + iy for positive-signed finite y, returns π − i∞.
     * <li>If {@code z} is +∞ + iy for positive-signed finite y, returns +0 − i∞.
     * <li>If {@code z} is −∞ + i∞, returns 3π /4 − i∞.
     * <li>If {@code z} is +∞ + i∞, returns π /4 − i∞.
     * <li>If {@code z} is ±∞ + iNaN, returns NaN ± i∞ where the sign of the imaginary part of the result is unspecified.
     * <li>If {@code z} is NaN + iy for finite y, returns NaN + iNaN.
     * <li>If {@code z} is NaN + i∞, returns NaN − i∞.
     * <li>If {@code z} is NaN + iNaN, returns NaN + iNaN.
     * </ul>
     *
     * <p>This function is implemented using real \( x \) and imaginary \( y \) parts:
     *
     * <p>\[ \cos^{-1}(z) = \cos^{-1}(B) - i\ \text{sgn}(y) \ln\left(A + \sqrt{A^2-1}\right) \\
     *   A = \frac{1}{2} \left[ \sqrt{(x+1)^2+y^2} + \sqrt{(x-1)^2+y^2} \right] \\
     *   B = \frac{1}{2} \left[ \sqrt{(x+1)^2+y^2} - \sqrt{(x-1)^2+y^2} \right] \]
     *
     * <p>where \( \text{sgn}(y) \) is the sign function implemented using
     * {@link Math#copySign(double,double) copySign(1.0, y)}.
     *
     * <p>The implementation is based on the method described in:</p>
     * <blockquote>
     * T E Hull, Thomas F Fairgrieve and Ping Tak Peter Tang (1997)
     * Implementing the complex Arcsine and Arccosine Functions using Exception Handling.
     * ACM Transactions on Mathematical Software, Vol 23, No 3, pp 299-335.
     * </blockquote>
     *
     * <p>The code has been adapted from the <a href="https://www.boost.org/">Boost</a>
     * {@code c++} implementation {@code <boost/math/complex/acos.hpp>}. The function is well
     * defined over the entire complex number range, and produces accurate values even at the
     * extremes due to special handling of overflow and underflow conditions.</p>
     *
     * @return The inverse cosine of this complex number.
     * @see <a href="http://functions.wolfram.com/ElementaryFunctions/ArcCos/">ArcCos</a>
     */
    public Complex acos() {
        return acos(real, imaginary, Complex::ofCartesian);
    }

    /**
     * Returns the inverse cosine of the complex number.
     *
     * <p>This function exists to allow implementation of the identity
     * {@code acosh(z) = +-i acos(z)}.<p>
     *
     * @param real Real part.
     * @param imaginary Imaginary part.
     * @param constructor Constructor.
     * @return The inverse cosine of the complex number.
     */
    private static Complex acos(final double real, final double imaginary,
                                final ComplexConstructor constructor) {
        // Compute with positive values and determine sign at the end
        final double x = Math.abs(real);
        final double y = Math.abs(imaginary);
        // The result (without sign correction)
        double re;
        double im;

        // Handle C99 special cases
        if (isPosInfinite(x)) {
            if (isPosInfinite(y)) {
                re = PI_OVER_4;
                im = y;
            } else if (Double.isNaN(y)) {
                // sign of the imaginary part of the result is unspecified
                return constructor.create(imaginary, real);
            } else {
                re = 0;
                im = Double.POSITIVE_INFINITY;
            }
        } else if (Double.isNaN(x)) {
            if (isPosInfinite(y)) {
                return constructor.create(x, -imaginary);
            }
            // No-use of the input constructor
            return NAN;
        } else if (isPosInfinite(y)) {
            re = PI_OVER_2;
            im = y;
        } else if (Double.isNaN(y)) {
            return constructor.create(x == 0 ? PI_OVER_2 : y, y);
        } else {
            // Special case for real numbers:
            if (y == 0 && x <= 1) {
                return constructor.create(x == 0 ? PI_OVER_2 : Math.acos(real), -imaginary);
            }

            final double xp1 = x + 1;
            final double xm1 = x - 1;

            if (inRegion(x, y, SAFE_MIN, SAFE_MAX)) {
                final double yy = y * y;
                final double r = Math.sqrt(xp1 * xp1 + yy);
                final double s = Math.sqrt(xm1 * xm1 + yy);
                final double a = 0.5 * (r + s);
                final double b = x / a;

                if (b <= B_CROSSOVER) {
                    re = Math.acos(b);
                } else {
                    final double apx = a + x;
                    if (x <= 1) {
                        re = Math.atan(Math.sqrt(0.5 * apx * (yy / (r + xp1) + (s - xm1))) / x);
                    } else {
                        re = Math.atan((y * Math.sqrt(0.5 * (apx / (r + xp1) + apx / (s + xm1)))) / x);
                    }
                }

                if (a <= A_CROSSOVER) {
                    double am1;
                    if (x < 1) {
                        am1 = 0.5 * (yy / (r + xp1) + yy / (s - xm1));
                    } else {
                        am1 = 0.5 * (yy / (r + xp1) + (s + xm1));
                    }
                    im = Math.log1p(am1 + Math.sqrt(am1 * (a + 1)));
                } else {
                    im = Math.log(a + Math.sqrt(a * a - 1));
                }
            } else {
                // Hull et al: Exception handling code from figure 6
                if (y <= (Precision.EPSILON * Math.abs(xm1))) {
                    if (x < 1) {
                        re = Math.acos(x);
                        im = y / Math.sqrt(xp1 * (1 - x));
                    } else {
                        // This deviates from Hull et al's paper as per
                        // https://svn.boost.org/trac/boost/ticket/7290
                        if ((Double.MAX_VALUE / xp1) > xm1) {
                            // xp1 * xm1 won't overflow:
                            re = y / Math.sqrt(xm1 * xp1);
                            im = Math.log1p(xm1 + Math.sqrt(xp1 * xm1));
                        } else {
                            re = y / x;
                            im = LN_2 + Math.log(x);
                        }
                    }
                } else if (y <= SAFE_MIN) {
                    // Hull et al: Assume x == 1.
                    // True if:
                    // E^2 > 8*sqrt(u)
                    //
                    // E = Machine epsilon: (1 + epsilon) = 1
                    // u = Double.MIN_NORMAL
                    re = Math.sqrt(y);
                    im = Math.sqrt(y);
                } else if (Precision.EPSILON * y - 1 >= x) {
                    re = PI_OVER_2;
                    im = LN_2 + Math.log(y);
                } else if (x > 1) {
                    re = Math.atan(y / x);
                    final double xoy = x / y;
                    im = LN_2 + Math.log(y) + 0.5 * Math.log1p(xoy * xoy);
                } else {
                    re = PI_OVER_2;
                    final double a = Math.sqrt(1 + y * y);
                    im = 0.5 * Math.log1p(2 * y * (y + a));
                }
            }
        }

        return constructor.create(negative(real) ? Math.PI - re : re,
                                  negative(imaginary) ? im : -im);
    }

    /**
     * Returns the
     * <a href="http://mathworld.wolfram.com/InverseSine.html">
     * inverse sine</a> of this complex number.
     *
     * <p>\[ \sin^{-1}(z) = - i \left(\ln{iz + \sqrt{1 - z^2}}\right) \]
     *
     * <p>The inverse sine of \( z \) is unbounded along the imaginary axis and
     * in the range \( [-\pi, \pi] \) along the real axis. Special cases are handled
     * as if the operation is implemented using \( \sin^{-1}(z) = -i \sinh^{-1}(iz) \).
     *
     * <p>This is implemented using real \( x \) and imaginary \( y \) parts:
     *
     * <p>\[ \sin^{-1}(z) = \sin^{-1}(B) + i\ \text{sgn}(y)\ln \left(A + \sqrt{A^2-1} \right) \\
     *   A = \frac{1}{2} \left[ \sqrt{(x+1)^2+y^2} + \sqrt{(x-1)^2+y^2} \right] \\
     *   B = \frac{1}{2} \left[ \sqrt{(x+1)^2+y^2} - \sqrt{(x-1)^2+y^2} \right] \]
     *
     * <p>where \( \text{sgn}(y) \) is the sign function implemented using
     * {@link Math#copySign(double,double) copySign(1.0, y)}.
     *
     * <p>The implementation is based on the method described in:</p>
     * <blockquote>
     * T E Hull, Thomas F Fairgrieve and Ping Tak Peter Tang (1997)
     * Implementing the complex Arcsine and Arccosine Functions using Exception Handling.
     * ACM Transactions on Mathematical Software, Vol 23, No 3, pp 299-335.
     * </blockquote>
     *
     * <p>The code has been adapted from the <a href="https://www.boost.org/">Boost</a>
     * {@code c++} implementation {@code <boost/math/complex/asin.hpp>}. The function is well
     * defined over the entire complex number range, and produces accurate values even at the
     * extremes due to special handling of overflow and underflow conditions.</p>
     *
     * @return The inverse sine of this complex number.
     * @see <a href="http://functions.wolfram.com/ElementaryFunctions/ArcSin/">ArcSin</a>
     */
    public Complex asin() {
        return asin(real, imaginary, Complex::ofCartesian);
    }

    /**
     * Returns the inverse sine of the complex number.
     *
     * <p>This function exists to allow implementation of the identity
     * {@code asinh(z) = -i asin(iz)}.<p>
     *
     * <p>The code has been adapted from the <a href="https://www.boost.org/">Boost</a>
     * {@code c++} implementation {@code <boost/math/complex/asin.hpp>}.</p>
     *
     * @param real Real part.
     * @param imaginary Imaginary part.
     * @param constructor Constructor.
     * @return The inverse sine of this complex number.
     */
    private static Complex asin(final double real, final double imaginary,
                                final ComplexConstructor constructor) {
        // Compute with positive values and determine sign at the end
        final double x = Math.abs(real);
        final double y = Math.abs(imaginary);
        // The result (without sign correction)
        double re;
        double im;

        // Handle C99 special cases
        if (Double.isNaN(x)) {
            if (isPosInfinite(y)) {
                re = x;
                im = y;
            } else {
                // No-use of the input constructor
                return NAN;
            }
        } else if (Double.isNaN(y)) {
            if (x == 0) {
                re = 0;
                im = y;
            } else if (isPosInfinite(x)) {
                re = y;
                im = x;
            } else {
                // No-use of the input constructor
                return NAN;
            }
        } else if (isPosInfinite(x)) {
            re = isPosInfinite(y) ? PI_OVER_4 : PI_OVER_2;
            im = x;
        } else if (isPosInfinite(y)) {
            re = 0;
            im = y;
        } else {
            // Special case for real numbers:
            if (y == 0 && x <= 1) {
                return constructor.create(Math.asin(real), imaginary);
            }

            final double xp1 = x + 1;
            final double xm1 = x - 1;

            if (inRegion(x, y, SAFE_MIN, SAFE_MAX)) {
                final double yy = y * y;
                final double r = Math.sqrt(xp1 * xp1 + yy);
                final double s = Math.sqrt(xm1 * xm1 + yy);
                final double a = 0.5 * (r + s);
                final double b = x / a;

                if (b <= B_CROSSOVER) {
                    re = Math.asin(b);
                } else {
                    final double apx = a + x;
                    if (x <= 1) {
                        re = Math.atan(x / Math.sqrt(0.5 * apx * (yy / (r + xp1) + (s - xm1))));
                    } else {
                        re = Math.atan(x / (y * Math.sqrt(0.5 * (apx / (r + xp1) + apx / (s + xm1)))));
                    }
                }

                if (a <= A_CROSSOVER) {
                    double am1;
                    if (x < 1) {
                        am1 = 0.5 * (yy / (r + xp1) + yy / (s - xm1));
                    } else {
                        am1 = 0.5 * (yy / (r + xp1) + (s + xm1));
                    }
                    im = Math.log1p(am1 + Math.sqrt(am1 * (a + 1)));
                } else {
                    im = Math.log(a + Math.sqrt(a * a - 1));
                }
            } else {
                // Hull et al: Exception handling code from figure 4
                if (y <= (Precision.EPSILON * Math.abs(xm1))) {
                    if (x < 1) {
                        re = Math.asin(x);
                        im = y / Math.sqrt(xp1 * (1 - x));
                    } else {
                        re = PI_OVER_2;
                        if ((Double.MAX_VALUE / xp1) > xm1) {
                            // xp1 * xm1 won't overflow:
                            im = Math.log1p(xm1 + Math.sqrt(xp1 * xm1));
                        } else {
                            im = LN_2 + Math.log(x);
                        }
                    }
                } else if (y <= SAFE_MIN) {
                    // Hull et al: Assume x == 1.
                    // True if:
                    // E^2 > 8*sqrt(u)
                    //
                    // E = Machine epsilon: (1 + epsilon) = 1
                    // u = Double.MIN_NORMAL
                    re = PI_OVER_2 - Math.sqrt(y);
                    im = Math.sqrt(y);
                } else if (Precision.EPSILON * y - 1 >= x) {
                    // Possible underflow:
                    re = x / y;
                    im = LN_2 + Math.log(y);
                } else if (x > 1) {
                    re = Math.atan(x / y);
                    final double xoy = x / y;
                    im = LN_2 + Math.log(y) + 0.5 * Math.log1p(xoy * xoy);
                } else {
                    final double a = Math.sqrt(1 + y * y);
                    // Possible underflow:
                    re = x / a;
                    im = 0.5 * Math.log1p(2 * y * (y + a));
                }
            }
        }

        return constructor.create(changeSign(re, real),
                                  changeSign(im, imaginary));
    }

    /**
     * Returns the
     * <a href="http://mathworld.wolfram.com/InverseTangent.html">
     * inverse tangent</a> of this complex number.
     *
     * <p>\[ \tan^{-1}(z) = \frac{i}{2} \ln \left( \frac{i + z}{i - z} \right) \]
     *
     * <p>As per the C99 standard this function is computed using the trigonomic identity:
     * \[ \tan^{-1}(z) = -i \tanh^{-1}(iz) \]
     *
     * @return The inverse tangent of this complex number.
     * @see <a href="http://functions.wolfram.com/ElementaryFunctions/ArcTan/">ArcTan</a>
     */
    public Complex atan() {
        // Define in terms of atanh
        // atan(z) = -i atanh(iz)
        // Multiply this number by I, compute atanh, then multiply by back
        return atanh(-imaginary, real, Complex::multiplyNegativeI);
    }

    /**
     * Returns the
     * <a href="http://mathworld.wolfram.com/InverseHyperbolicSine.html">
     * inverse hyperbolic sine</a> of this complex number.
     *
     * <p>\[ \sinh^{-1}(z) = \ln \left(z + \sqrt{1 + z^2} \right) \]
     *
     * <p>The inverse hyperbolic sine of \( z \) is unbounded along the real axis and
     * in the range \( [-\pi, \pi] \) along the imaginary axis. Special cases:
     *
     * <ul>
     * <li>{@code z.conj().asinh() == z.asinh().conj()}.
     * <li>This is an odd function: \( \sinh^{-1}(z) = -\sinh^{-1}(-z) \).
     * <li>If {@code z} is +0 + i0, returns 0 + i0.
     * <li>If {@code z} is x + i∞ for positive-signed finite x, returns +∞ + iπ/2.
     * <li>If {@code z} is x + iNaN for finite x, returns NaN + iNaN.
     * <li>If {@code z} is +∞ + iy for positive-signed finite y, returns +∞ + i0.
     * <li>If {@code z} is +∞ + i∞, returns +∞ + iπ/4.
     * <li>If {@code z} is +∞ + iNaN, returns +∞ + iNaN.
     * <li>If {@code z} is NaN + i0, returns NaN + i0.
     * <li>If {@code z} is NaN + iy for finite nonzero y, returns NaN + iNaN.
     * <li>If {@code z} is NaN + i∞, returns ±∞ + iNaN (where the sign of the real part of the result is unspecified).
     * <li>If {@code z} is NaN + iNaN, returns NaN + iNaN.
     * </ul>
     *
     * <p>This function is computed using the trigonomic identity:
     *
     * <p>\[ \sinh^{-1}(z) = -i \sin^{-1}(iz) \]
     *
     * @return The inverse hyperbolic sine of this complex number.
     * @see <a href="http://functions.wolfram.com/ElementaryFunctions/ArcSinh/">ArcSinh</a>
     */
    public Complex asinh() {
        // Define in terms of asin
        // asinh(z) = -i asin(iz)
        // Note: This is the opposite to the identity defined in the C99 standard:
        // asin(z) = -i asinh(iz)
        // Multiply this number by I, compute asin, then multiply by back
        return asin(-imaginary, real, Complex::multiplyNegativeI);
    }

    /**
     * Returns the
     * <a href="http://mathworld.wolfram.com/InverseHyperbolicTangent.html">
     * inverse hyperbolic tangent</a> of this complex number.
     *
     * <p>\[ \tanh^{-1}(z) = \frac{1}{2} \ln \left( \frac{1 + z}{1 - z} \right) \]
     *
     * <p>The inverse hyperbolic tangent of \( z \) is unbounded along the real axis and
     * in the range \( [-\pi, \pi] \) along the imaginary axis. Special cases:
     *
     * <ul>
     * <li>{@code z.conj().atanh() == z.atanh().conj()}.
     * <li>This is an odd function: \( \tanh^{-1}(z) = -\tanh^{-1}(-z) \).
     * <li>If {@code z} is +0 + i0, returns +0 + i0.
     * <li>If {@code z} is +0 + iNaN, returns +0 + iNaN.
     * <li>If {@code z} is +1 + i0, returns +∞ + i0.
     * <li>If {@code z} is x + i∞ for finite positive-signed x, returns +0 + iπ /2.
     * <li>If {@code z} is x+iNaN for nonzero finite x, returns NaN+iNaN.
     * <li>If {@code z} is +∞ + iy for finite positive-signed y, returns +0 + iπ /2.
     * <li>If {@code z} is +∞ + i∞, returns +0 + iπ /2.
     * <li>If {@code z} is +∞ + iNaN, returns +0 + iNaN.
     * <li>If {@code z} is NaN+iy for finite y, returns NaN+iNaN.
     * <li>If {@code z} is NaN + i∞, returns ±0 + iπ /2 (where the sign of the real part of the result is unspecified).
     * <li>If {@code z} is NaN + iNaN, returns NaN + iNaN.
     * </ul>
     *
     * <p>This is implemented using real \( x \) and imaginary \( y \) parts:
     *
     * <p>\[ \tanh^{-1}(z) = \frac{1}{4} \ln \left(1 + \frac{4x}{(1-x)^2+y^2} \right) + \\
     *                     i \frac{1}{2} \left( \tan^{-1} \left(\frac{2y}{1-x^2-y^2} \right) + \frac{\pi}{2} \left(\text{sgn}(x^2+y^2-1)+1 \right) \text{sgn}(y) \right) \]
     *
     * <p>The imaginary part is computed using {@link Math#atan2(double, double)} to ensure the
     * correct quadrant is returned from \( \tan^{-1} \left(\frac{2y}{1-x^2-y^2} \right) \).
     *
     * <p>The code has been adapted from the <a href="https://www.boost.org/">Boost</a>
     * {@code c++} implementation {@code <boost/math/complex/atanh.hpp>}. The function is well
     * defined over the entire complex number range, and produces accurate values even at the
     * extremes due to special handling of overflow and underflow conditions.
     *
     * @return The inverse hyperbolic tangent of this complex number.
     * @see <a href="http://functions.wolfram.com/ElementaryFunctions/ArcTanh/">ArcTanh</a>
     */
    public Complex atanh() {
        return atanh(real, imaginary, Complex::ofCartesian);
    }

    /**
     * Returns the inverse hyperbolic tangent of this complex number.
     *
     * <p>This function exists to allow implementation of the identity
     * {@code atan(z) = -i atanh(iz)}.<p>
     *
     * @param real Real part.
     * @param imaginary Imaginary part.
     * @param constructor Constructor.
     * @return The inverse hyperbolic tangent of the complex number.
     */
    private static Complex atanh(final double real, final double imaginary,
                                 final ComplexConstructor constructor) {
        // Compute with positive values and determine sign at the end
        final double x = Math.abs(real);
        final double y = Math.abs(imaginary);
        // The result (without sign correction)
        double re;
        double im;

        // Handle C99 special cases
        if (Double.isNaN(x)) {
            if (isPosInfinite(y)) {
                // The sign of the real part of the result is unspecified
                return constructor.create(0, Math.copySign(PI_OVER_2, imaginary));
            }
            // No-use of the input constructor.
            // Optionally raises the ‘‘invalid’’ floating-point exception, for finite y.
            return NAN;
        } else if (Double.isNaN(y)) {
            if (isPosInfinite(x)) {
                return constructor.create(Math.copySign(0, real), Double.NaN);
            }
            if (x == 0) {
                return constructor.create(real, Double.NaN);
            }
            // No-use of the input constructor
            return NAN;
        } else {
            // x && y are finite or infinite.

            // Check the safe region.
            // The lower and upper bounds have been copied from boost::math::atanh.
            // They are different from the safe region for asin and acos.
            // x >= SAFE_UPPER: (1-x) == -x
            // x <= SAFE_LOWER: 1 - x^2 = 1

            if (inRegion(x, y, SAFE_LOWER, SAFE_UPPER)) {
                // Normal computation within a safe region.

                // minus x plus 1: (-x+1)
                final double mxp1 = 1 - x;
                final double yy = y * y;
                // The definition of real component is:
                // real = log( ((x+1)^2+y^2) / ((1-x)^2+y^2) ) / 4
                // This simplifies by adding 1 and subtracting 1 as a fraction:
                //      = log(1 + ((x+1)^2+y^2) / ((1-x)^2+y^2) - ((1-x)^2+y^2)/((1-x)^2+y^2) ) / 4
                //
                // real(atanh(z)) == log(1 + 4*x / ((1-x)^2+y^2)) / 4
                // imag(atanh(z)) == tan^-1 (2y, (1-x)(1+x) - y^2) / 2
                // The division is done at the end of the function.
                re = Math.log1p(4 * x / (mxp1 * mxp1 + yy));
                im = Math.atan2(2 * y, mxp1 * (1 + x) - yy);
            } else {
                // This section handles exception cases that would normally cause
                // underflow or overflow in the main formulas.

                // C99. G.7: Special case for imaginary only numbers
                if (x == 0) {
                    if (imaginary == 0) {
                        return constructor.create(real, imaginary);
                    }
                    // atanh(iy) = i atan(y)
                    return constructor.create(real, Math.atan(imaginary));
                }

                // Real part:
                // real = Math.log1p(4x / ((1-x)^2 + y^2))
                // real = Math.log1p(4x / (1 - 2x + x^2 + y^2))
                // real = Math.log1p(4x / (1 + x(x-2) + y^2))
                // without either overflow or underflow in the squared terms.
                if (x >= SAFE_UPPER) {
                    // (1-x) = -x to machine precision:
                    // log1p(4x / (x^2 + y^2))
                    if (isPosInfinite(x) || isPosInfinite(y)) {
                        re = 0;
                    } else if (y >= SAFE_UPPER) {
                        // Big x and y: divide by x*y
                        re = Math.log1p((4 / y) / (x / y + y / x));
                    } else if (y > 1) {
                        // Big x: divide through by x:
                        re = Math.log1p(4 / (x + y * y / x));
                    } else {
                        // Big x small y, as above but neglect y^2/x:
                        re = Math.log1p(4 / x);
                    }
                } else if (y >= SAFE_UPPER) {
                    if (x > 1) {
                        // Big y, medium x, divide through by y:
                        final double mxp1 = 1 - x;
                        re = Math.log1p((4 * x / y) / (mxp1 * mxp1 / y + y));
                    } else {
                        // Big y, small x, as above but neglect (1-x)^2/y:
                        // Note: log1p(v) == v - v^2/2 + v^3/3 ... Taylor series when v is small.
                        // Here v is so small only the first term matters.
                        re = 4 * x / y / y;
                    }
                } else if (x == 1) {
                    // x = 1, small y:
                    // Special case when x == 1 as (1-x) is invalid.
                    // Simplify the following formula:
                    // real = log( sqrt((x+1)^2+y^2) ) / 2 - log( sqrt((1-x)^2+y^2) ) / 2
                    //      = log( sqrt(4+y^2) ) / 2 - log(y) / 2
                    // if: 4+y^2 -> 4
                    //      = log( 2 ) / 2 - log(y) / 2
                    //      = (log(2) - log(y)) / 2
                    // Multiply by 2 as it will be divided by 4 at the end.
                    // C99: if y=0 raises the ‘‘divide-by-zero’’ floating-point exception.
                    re = 2 * (LN_2 - Math.log(y));
                } else {
                    // Modified from boost which checks y > SAFE_LOWER.
                    // if y*y -> 0 it will be ignored so always include it.
                    final double mxp1 = 1 - x;
                    re = Math.log1p((4 * x) / (mxp1 * mxp1 + y * y));
                }

                // Imaginary part:
                // imag = atan2(2y, (1-x)(1+x) - y^2)
                // if x or y are large, then the formula:
                //   atan2(2y, (1-x)(1+x) - y^2)
                // evaluates to +(PI - theta) where theta is negligible compared to PI.
                if ((x >= SAFE_UPPER) || (y >= SAFE_UPPER)) {
                    im = Math.PI;
                } else if (x <= SAFE_LOWER) {
                    // (1-x)^2 -> 1
                    if (y <= SAFE_LOWER) {
                        // 1 - y^2 -> 1
                        im = Math.atan2(2 * y, 1);
                    } else {
                        im = Math.atan2(2 * y, 1 - y * y);
                    }
                } else {
                    // Medium x, small y.
                    // Modified from boost which checks (y == 0) && (x == 1) and sets re = 0.
                    // This is same as the result from calling atan2(0, 0) so just do that.
                    // 1 - y^2 = 1 so ignore subtracting y^2
                    im = Math.atan2(2 * y, (1 - x) * (1 + x));
                }
            }
        }

        re /= 4;
        im /= 2;
        return constructor.create(changeSign(re, real),
                                  changeSign(im, imaginary));
    }

    /**
     * Returns the
     * <a href="http://mathworld.wolfram.com/InverseHyperbolicCosine.html">
     * inverse hyperbolic cosine</a> of this complex number.
     *
     * <p>\[ \cosh^{-1}(z) = \ln \left(z + \sqrt{z + 1} \sqrt{z - 1} \right) \]
     *
     * <p>The inverse hyperbolic cosine of \( z \) is in the range \( [0, \infty) \) along the real axis and
     * in the range \( [-\pi, \pi] \) along the imaginary axis. Special cases:
     *
     * <ul>
     * <li>{@code z.conj().acosh() == z.acosh().conj()}.
     * <li>If {@code z} is ±0 + i0, returns +0 + iπ/2.
     * <li>If {@code z} is x + i∞ for finite x, returns +∞ + iπ/2.
     * <li>If {@code z} is x + iNaN for finite x, returns NaN + iNaN.
     * <li>If {@code z} is −∞ + iy for positive-signed finite y, returns +∞ + iπ.
     * <li>If {@code z} is +∞ + iy for positive-signed finite y, returns +∞ + i0.
     * <li>If {@code z} is −∞ + i∞, returns +∞ + i3π/4.
     * <li>If {@code z} is +∞ + i∞, returns +∞ + iπ/4.
     * <li>If {@code z} is ±∞ + iNaN, returns +∞ + iNaN.
     * <li>If {@code z} is NaN + iy for finite y, returns NaN + iNaN.
     * <li>If {@code z} is NaN + i∞, returns +∞ + iNaN.
     * <li>If {@code z} is NaN + iNaN, returns NaN + iNaN.
     * </ul>
     *
     * <p>This function is computed using the trigonomic identity:
     *
     * <p>\[ \cosh^{-1}(z) = \pm i \cos^{-1}(z) \]
     *
     * <p>The sign of the multiplier is chosen to give {@code z.acosh().real() >= 0}
     * and compatibility with the C99 standard.
     *
     * @return The inverse hyperbolic cosine of this complex number.
     * @see <a href="http://functions.wolfram.com/ElementaryFunctions/ArcCosh/">ArcCosh</a>
     */
    public Complex acosh() {
        // Define in terms of acos
        // acosh(z) = +-i acos(z)
        // Handle special case:
        // acos(+-0 + iNaN) = π/2 + iNaN
        // acosh(x + iNaN) = NaN + iNaN for all finite x (including zero)
        if (Double.isNaN(imaginary) && Double.isFinite(real)) {
            return NAN;
        }
        return acos(real, imaginary, (re, im) ->
            // Set the sign appropriately for real >= 0
            (negative(im)) ?
                // Multiply by I
                new Complex(-im, re) :
                // Multiply by -I
                new Complex(im, -re)
        );
    }

    /**
     * Returns the
     * <a href="http://mathworld.wolfram.com/Cosine.html">
     * cosine</a> of this complex number.
     *
     * <p>\[ \cos(z) = \frac{1}{2} \left( e^{iz} + e^{-iz} \right) \]
     *
     * <p>This is an even function: \( \cos(z) = \cos(-z) \).
     *
     * <p>This is implemented using real \( x \) and imaginary \( y \) parts:
     *
     * <p>\[ \cos(x + iy) = \cos(x)\cosh(y) - i \sin(x)\sinh(y) \]
     *
     * <p>As per the C99 standard this function is computed using the trigonomic identity:
     *
     * <p>\[ cos(z) = cosh(iz) \]
     *
     * @return The cosine of this complex number.
     * @see <a href="http://functions.wolfram.com/ElementaryFunctions/Cos/">Cos</a>
     */
    public Complex cos() {
        // Define in terms of cosh
        // cos(z) = cosh(iz)
        // Multiply this number by I and compute cosh.
        return cosh(-imaginary, real, Complex::ofCartesian);
    }

    /**
     * Returns the
     * <a href="http://mathworld.wolfram.com/HyperbolicCosine.html">
     * hyperbolic cosine</a> of this complex number.
     *
     * <p>\[ \cosh(z) = \frac{1}{2} \left( e^{z} + e^{-z} \right) \]
     *
     * <p>The hyperbolic cosine of \( z \) is an entire function in the complex plane.
     * and is periodic with respect to the imaginary component with period \( 2\pi i \).
     *
     * <ul>
     * <li>{@code z.conj().cosh() == z.cosh().conj()}.
     * <li>This is an even function: \( \cosh(z) = \cosh(-z) \).
     * <li>If {@code z} is +0 + i0, returns 1 + i0.
     * <li>If {@code z} is +0 + i∞, returns NaN ± i0 (where the sign of the imaginary part of the result is unspecified).
     * <li>If {@code z} is +0 + iNaN, returns NaN ± i0 (where the sign of the imaginary part of the result is unspecified).
     * <li>If {@code z} is x + i∞ for finite nonzero x, returns NaN + iNaN.
     * <li>If {@code z} is x + iNaN for finite nonzero x, returns NaN + iNaN.
     * <li>If {@code z} is +∞ + i0, returns +∞ + i0.
     * <li>If {@code z} is +∞ + iy for finite nonzero y, returns +∞ cis(y) (see {@link #ofCis(double)}).
     * <li>If {@code z} is +∞ + i∞, returns ±∞ + iNaN (where the sign of the real part of the result is unspecified).
     * <li>If {@code z} is +∞ + iNaN, returns +∞ + iNaN.
     * <li>If {@code z} is NaN + i0, returns NaN ± i0 (where the sign of the imaginary part of the result is unspecified).
     * <li>If {@code z} is NaN + iy for all nonzero numbers y, returns NaN + iNaN.
     * <li>If {@code z} is NaN + iNaN, returns NaN + iNaN.
     * </ul>
     *
     * <p>This is implemented using real \( x \) and imaginary \( y \) parts:
     *
     * <p>\[ \cosh(x + iy) = \cosh(x)\cos(y) + i \sinh(x)\sin(y) \]
     *
     * @return The hyperbolic cosine of this complex number.
     * @see <a href="http://functions.wolfram.com/ElementaryFunctions/Cosh/">Cosh</a>
     */
    public Complex cosh() {
        return cosh(real, imaginary, Complex::ofCartesian);
    }

    /**
     * Returns the hyperbolic cosine of the complex number.
     *
     * <p>This function exists to allow implementation of the identity
     * {@code cos(z) = cosh(iz)}.<p>
     *
     * @param real Real part.
     * @param imaginary Imaginary part.
     * @param constructor Constructor.
     * @return The hyperbolic cosine of the complex number.
     */
    private static Complex cosh(double real, double imaginary, ComplexConstructor constructor) {
        // ISO C99: Preserve the even function by mapping to positive
        // f(z) = f(-z)
        if (Double.isInfinite(real) && !Double.isFinite(imaginary)) {
            return constructor.create(Math.abs(real), Double.NaN);
        }
        if (real == 0 && !Double.isFinite(imaginary)) {
            return constructor.create(Double.NaN, changeSign(real, imaginary));
        }
        if (real == 0 && imaginary == 0) {
            return constructor.create(1, changeSign(real, imaginary));
        }
        if (imaginary == 0 && !Double.isFinite(real)) {
            return constructor.create(Math.abs(real), changeSign(imaginary, real));
        }
        return constructor.create(Math.cosh(real) * Math.cos(imaginary),
                                  Math.sinh(real) * Math.sin(imaginary));
    }

    /**
     * Returns the
     * <a href="http://mathworld.wolfram.com/ExponentialFunction.html">
     * exponential function</a> of this complex number.
     *
     * <p>\[ \exp(z) = e^z \]
     *
     * <p>The exponential function of \( z \) is an entire function in the complex plane.
     * Special cases:
     *
     * <ul>
     * <li>{@code z.conj().exp() == z.exp().conj()}.
     * <li>If {@code z} is ±0 + i0, returns 1 + i0.
     * <li>If {@code z} is x + i∞ for finite x, returns NaN + iNaN.
     * <li>If {@code z} is x + iNaN for finite x, returns NaN + iNaN.
     * <li>If {@code z} is +∞ + i0, returns +∞ + i0.
     * <li>If {@code z} is −∞ + iy for finite y, returns +0 cis(y) (see {@link #ofCis(double)}).
     * <li>If {@code z} is +∞ + iy for finite nonzero y, returns +∞ cis(y).
     * <li>If {@code z} is −∞ + i∞, returns ±0 ± i0 (where the signs of the real and imaginary parts of the result are unspecified).
     * <li>If {@code z} is +∞ + i∞, returns ±∞ + iNaN (where the sign of the real part of the result is unspecified).
     * <li>If {@code z} is −∞ + iNaN, returns ±0 ± i0 (where the signs of the real and imaginary parts of the result are unspecified).
     * <li>If {@code z} is +∞ + iNaN, returns ±∞ + iNaN (where the sign of the real part of the result is unspecified).
     * <li>If {@code z} is NaN + i0, returns NaN + i0.
     * <li>If {@code z} is NaN + iy for all nonzero numbers y, returns NaN + iNaN.
     * <li>If {@code z} is NaN + iNaN, returns NaN + iNaN.
     * </ul>
     *
     * <p>Implements the formula:
     *
     * <p>\[ \exp(x + iy) = e^x (\cos(y) + i \sin(y)) \]
     *
     * @return <code>e<sup>this</sup></code>.
     * @see <a href="http://functions.wolfram.com/ElementaryFunctions/Exp/">Exp</a>
     */
    public Complex exp() {
        if (Double.isInfinite(real)) {
            // Set the scale factor applied to cis(y)
            double zeroOrInf;
            if (real < 0) {
                if (!Double.isFinite(imaginary)) {
                    // (−∞ + i∞) or (−∞ + iNaN) returns (±0 ± i0) (where the signs of the
                    // real and imaginary parts of the result are unspecified).
                    // Here we preserve the conjugate equality.
                    return new Complex(0, Math.copySign(0, imaginary));
                }
                // (−∞ + iy) returns +0 cis(y), for finite y
                zeroOrInf = 0;
            } else {
                // (+∞ + i0) returns +∞ + i0.
                if (imaginary == 0) {
                    return this;
                }
                // (+∞ + i∞) or (+∞ + iNaN) returns (±∞ + iNaN) and raises the invalid
                // floating-point exception (where the sign of the real part of the
                // result is unspecified).
                if (!Double.isFinite(imaginary)) {
                    return new Complex(real, Double.NaN);
                }
                // (+∞ + iy) returns (+∞ cis(y)), for finite nonzero y.
                zeroOrInf = real;
            }
            return new Complex(zeroOrInf * Math.cos(imaginary),
                               zeroOrInf * Math.sin(imaginary));
        } else if (Double.isNaN(real)) {
            // (NaN + i0) returns (NaN + i0);
            // (NaN + iy) returns (NaN + iNaN) and optionally raises the invalid floating-point exception
            // (NaN + iNaN) returns (NaN + iNaN)
            return imaginary == 0 ? this : NAN;
        } else if (!Double.isFinite(imaginary)) {
            // (x + i∞) or (x + iNaN) returns (NaN + iNaN) and raises the invalid
            // floating-point exception, for finite x.
            return NAN;
        }
        // real and imaginary are finite.
        // Compute e^a * (cos(b) + i sin(b)).

        // Special case:
        // (±0 + i0) returns (1 + i0)
        final double exp = Math.exp(real);
        if (imaginary == 0) {
            return new Complex(exp, imaginary);
        }
        return new Complex(exp * Math.cos(imaginary),
                           exp * Math.sin(imaginary));
    }

    /**
     * Returns the
     * <a href="http://mathworld.wolfram.com/NaturalLogarithm.html">
     * natural logarithm</a> of this complex number.
     *
     * <p>The natural logarithm of \( z \) is unbounded along the real axis and
     * in the range \( [-\pi, \pi] \) along the imaginary axis. Special cases:
     *
     * <ul>
     * <li>{@code z.conj().log() == z.log().conj()}.
     * <li>If {@code z} is −0 + i0, returns −∞ + iπ.
     * <li>If {@code z} is +0 + i0, returns −∞ + i0.
     * <li>If {@code z} is x + i∞ for finite x, returns +∞ + iπ/2.
     * <li>If {@code z} is x + iNaN for finite x, returns NaN + iNaN.
     * <li>If {@code z} is −∞ + iy for finite positive-signed y, returns +∞ + iπ.
     * <li>If {@code z} is +∞ + iy for finite positive-signed y, returns +∞ + i0.
     * <li>If {@code z} is −∞ + i∞, returns +∞ + i3π/4.
     * <li>If {@code z} is +∞ + i∞, returns +∞ + iπ/4.
     * <li>If {@code z} is ±∞ + iNaN, returns +∞ + iNaN.
     * <li>If {@code z} is NaN + iy for finite y, returns NaN + iNaN.
     * <li>If {@code z} is NaN + i∞, returns +∞ + iNaN.
     * <li>If {@code z} is NaN + iNaN, returns NaN + iNaN.
     * </ul>
     *
     * <p>Implements the formula:
     *
     * <p>\[ \ln(z) = \ln |z| + i \arg(z) \]
     *
     * <p>where \( |z| \) is the absolute and \( \arg(z) \) is the argument.
     *
     * <p>The implementation is based on the method described in:</p>
     * <blockquote>
     * T E Hull, Thomas F Fairgrieve and Ping Tak Peter Tang (1994)
     * Implementing complex elementary functions using exception handling.
     * ACM Transactions on Mathematical Software, Vol 20, No 2, pp 215-244.
     * </blockquote>
     *
     * @return The natural logarithm of this complex number.
     * @see Math#log(double)
     * @see #abs()
     * @see #arg()
     * @see <a href="http://functions.wolfram.com/ElementaryFunctions/Log/">Log</a>
     */
    public Complex log() {
        return log(Math::log, HALF, LN_2, Complex::ofCartesian);
    }

    /**
     * Returns the base 10
     * <a href="http://mathworld.wolfram.com/CommonLogarithm.html">
     * common logarithm</a> of this complex number.
     *
     * <p>The common logarithm of \( z \) is unbounded along the real axis and
     * in the range \( [-\pi, \pi] \) along the imaginary axis. Special cases are as
     * defined in the {@link #log() natural logarithm}:
     *
     * <p>Implements the formula:
     *
     * <p>\[ \log_{10}(z) = \log_{10} |z| + i \arg(z) \]
     *
     * <p>where \( |z| \) is the absolute and \( \arg(z) \) is the argument.
     *
     * @return The base 10 logarithm of this complex number.
     * @see Math#log10(double)
     * @see #abs()
     * @see #arg()
     */
    public Complex log10() {
        return log(Math::log10, LOG_10E_O_2, LOG10_2, Complex::ofCartesian);
    }

    /**
     * Returns the logarithm of this complex number using the provided function.
     * Implements the formula:
     * <pre>
     *   log(x + i y) = log(|x + i y|) + i arg(x + i y)
     * </pre>
     *
     * <p>Warning: The argument {@code logOf2} must be equal to {@code log(2)} using the
     * provided log function otherwise scaling using powers of 2 in the case of overflow
     * will be incorrect. This is provided as an internal optimisation.
     *
     * @param log Log function.
     * @param logOfeOver2 The log function applied to e, then divided by 2.
     * @param logOf2 The log function applied to 2.
     * @param constructor Constructor for the returned complex.
     * @return The logarithm of this complex number.
     * @see #abs()
     * @see #arg()
     */
    private Complex log(UnaryOperation log, double logOfeOver2, double logOf2, ComplexConstructor constructor) {
        // Handle NaN
        if (Double.isNaN(real) || Double.isNaN(imaginary)) {
            // Return NaN unless infinite
            if (isInfinite()) {
                return constructor.create(Double.POSITIVE_INFINITY, Double.NaN);
            }
            // No-use of the input constructor
            return NAN;
        }

        // Returns the real part:
        // log(sqrt(x^2 + y^2))
        // log(x^2 + y^2) / 2

        // Compute with positive values
        double x = Math.abs(real);
        double y = Math.abs(imaginary);

        // Find the larger magnitude.
        if (x < y) {
            final double tmp = x;
            x = y;
            y = tmp;
        }

        if (x == 0) {
            // Handle zero: raises the ‘‘divide-by-zero’’ floating-point exception.
            return constructor.create(Double.NEGATIVE_INFINITY,
                                      negative(real) ? Math.copySign(Math.PI, imaginary) : imaginary);
        }

        double re;

        if (x > HALF && x < ROOT2) {
            // x^2+y^2 close to 1. Use log1p(x^2+y^2 - 1) / 2.
            re = Math.log1p(x2y2m1(x, y)) * logOfeOver2;
        } else if (y == 0) {
            // Handle real only number
            re = log.apply(x);
        } else if (x > SAFE_MAX || x < SAFE_MIN || y < SAFE_MIN) {
            // Over/underflow of sqrt(x^2+y^2)
            if (isPosInfinite(x)) {
                // Handle infinity
                re = x;
            } else {
                // Do scaling
                final int expx = Math.getExponent(x);
                final int expy = Math.getExponent(y);
                if (2 * (expx - expy) > PRECISION_1) {
                    // y can be ignored
                    re = log.apply(x);
                } else {
                    // Hull et al:
                    // "It is important that the scaling be chosen so
                    // that there is no possibility of cancellation in this addition"
                    // i.e. sx^2 + sy^2 should not be close to 1.
                    // Their paper uses expx + 2 for underflow but expx for overflow.
                    // It has been modified here to use expx - 2.
                    int scale;
                    if (x > SAFE_MAX) {
                        // overflow
                        scale = expx - 2;
                    } else {
                        // underflow
                        scale = expx + 2;
                    }
                    final double sx = Math.scalb(x, -scale);
                    final double sy = Math.scalb(y, -scale);
                    re = scale * logOf2 + 0.5 * log.apply(sx * sx + sy * sy);
                }
            }
        } else {
            // Safe region that avoids under/overflow
            re = 0.5 * log.apply(x * x + y * y);
        }

        // All ISO C99 edge cases for the imaginary are satisfied by the Math library.
        return constructor.create(re, arg());
    }

    /**
     * Compute {@code x^2 + y^2 - 1} in high precision.
     * Assumes that the values x and y can be multiplied without overflow; that
     * {@code x >= y}; and both values are positive.
     *
     * @param x the x value
     * @param y the y value
     * @return {@code x^2 + y^2 - 1}.
     */
    private static double x2y2m1(double x, double y) {
        // Hull et al used (x-1)*(x+1)+y*y.
        // From the paper on page 236:

        // If x == 1 there is no cancellation.

        // If x > 1, there is also no cancellation, but the argument is now accurate
        // only to within a factor of 1 + 3 EPSILSON (note that x – 1 is exact),
        // so that error = 3 EPSILON.

        // If x < 1, there can be serious cancellation:

        // If 4 y^2 < |x^2 – 1| the cancellation is not serious ... the argument is accurate
        // only to within a factor of 1 + 4 EPSILSON so that error = 4 EPSILON.

        // Otherwise there can be serious cancellation and the relative error in the real part
        // could be enormous.

        // TODO - investigate the computation in high precision using
        // LinearCombination#value(double, double, double, double, double, double)
        // from o.a.c.numbers.arrays.
        final double xm1xp1 = (x - 1) * (x + 1);
        final double yy = y * y;
        if (x < 1 && 4 * yy > Math.abs(xm1xp1)) {
            // Large relative error...
            return xm1xp1 + yy;
        }
        return xm1xp1 + yy;
    }

    /**
     * Returns the complex power of this complex number raised to the power of \( x \).
     * Implements the formula:
     *
     * <p>\[ z^x = e^{x \ln(z)} \]
     *
     * <p>If this complex number is zero then this method returns zero if \( x \) is positive
     * in the real component and zero in the imaginary component;
     * otherwise it returns NaN + iNaN.
     *
     * @param  x The exponent to which this complex number is to be raised.
     * @return <code>this<sup>x</sup></code>.
     * @see #log()
     * @see #multiply(Complex)
     * @see #exp()
     * @see <a href="http://mathworld.wolfram.com/ComplexExponentiation.html">Complex exponentiation</a>
     * @see <a href="http://functions.wolfram.com/ElementaryFunctions/Power/">Power</a>
     */
    public Complex pow(Complex x) {
        if (real == 0 &&
            imaginary == 0) {
            // This value is zero. Test the other.
            if (x.real > 0 &&
                x.imaginary == 0) {
                // 0 raised to positive number is 0
                return ZERO;
            }
            // 0 raised to anything else is NaN
            return NAN;
        }
        return log().multiply(x).exp();
    }

    /**
     * Returns the complex power of this complex number raised to the power of \( x \).
     * Implements the formula:
     *
     * <p>\[ z^x = e^{x \ln(z)} \]
     *
     * <p>If this complex number is zero then this method returns zero if \( x \) is positive;
     * otherwise it returns NaN + iNaN.
     *
     * @param  x The exponent to which this complex number is to be raised.
     * @return <code>this<sup>x</sup></code>.
     * @see #log()
     * @see #multiply(double)
     * @see #exp()
     * @see #pow(Complex)
     * @see <a href="http://functions.wolfram.com/ElementaryFunctions/Power/">Power</a>
     */
    public Complex pow(double x) {
        if (real == 0 &&
            imaginary == 0) {
            // This value is zero. Test the other.
            if (x > 0) {
                // 0 raised to positive number is 0
                return ZERO;
            }
            // 0 raised to anything else is NaN
            return NAN;
        }
        return log().multiply(x).exp();
    }

    /**
     * Returns the
     * <a href="http://mathworld.wolfram.com/Sine.html">
     * sine</a> of this complex number.
     *
     * <p>\[ \sin(z) = \frac{1}{2} i \left( e^{-iz} - e^{iz} \right) \]
     *
     * <p>This is an odd function: \( \sin(z) = -\sin(-z) \).
     *
     * <p>This is implemented using real \( x \) and imaginary \( y \) parts:
     *
     * <p>\[ \sin(x + iy) = \sin(x)\cosh(y) + i \cos(x)\sinh(y) \]
     *
     * <p>As per the C99 standard this function is computed using the trigonomic identity:
     *
     * <p>\[ \sin(z) = -i \sinh(iz) \]
     *
     * @return The sine of this complex number.
     * @see <a href="http://functions.wolfram.com/ElementaryFunctions/Sin/">Sin</a>
     */
    public Complex sin() {
        // Define in terms of sinh
        // sin(z) = -i sinh(iz)
        // Multiply this number by I, compute sinh, then multiply by back
        return sinh(-imaginary, real, Complex::multiplyNegativeI);
    }

    /**
     * Returns the
     * <a href="http://mathworld.wolfram.com/HyperbolicSine.html">
     * hyperbolic sine</a> of this complex number.
     *
     * <p>\[ \sinh(z) = \frac{1}{2} \left( e^{z} - e^{-z} \right) \]
     *
     * <p>The hyperbolic sine of \( z \) is an entire function in the complex plane.
     * and is periodic with respect to the imaginary component with period \( 2\pi i \).
     *
     * <ul>
     * <li>{@code z.conj().sinh() == z.sinh().conj()}.
     * <li>This is an odd function: \( \sinh(z) = -\sinh(-z) \).
     * <li>If {@code z} is +0 + i0, returns +0 + i0.
     * <li>If {@code z} is +0 + i∞, returns ±0 + iNaN (where the sign of the real part of the result is unspecified).
     * <li>If {@code z} is +0 + iNaN, returns ±0 + iNaN (where the sign of the real part of the result is unspecified).
     * <li>If {@code z} is x + i∞ for positive finite x, returns NaN + iNaN.
     * <li>If {@code z} is x + iNaN for finite nonzero x, returns NaN + iNaN.
     * <li>If {@code z} is +∞ + i0, returns +∞ + i0.
     * <li>If {@code z} is +∞ + iy for positive finite y, returns +∞ cis(y) (see {@link #ofCis(double)}.
     * <li>If {@code z} is +∞ + i∞, returns ±∞ + iNaN (where the sign of the real part of the result is unspecified).
     * <li>If {@code z} is +∞ + iNaN, returns ±∞ + iNaN (where the sign of the real part of the result is unspecified).
     * <li>If {@code z} is NaN + i0, returns NaN + i0.
     * <li>If {@code z} is NaN + iy for all nonzero numbers y, returns NaN + iNaN.
     * <li>If {@code z} is NaN + iNaN, returns NaN + iNaN.
     * </ul>
     *
     * <p>This is implemented using real \( x \) and imaginary \( y \) parts:
     *
     * <p>\[ \sinh(x + iy) = \sinh(x)\cos(y) + i \cosh(x)\sin(y) \]
     *
     * @return The hyperbolic sine of this complex number.
     * @see <a href="http://functions.wolfram.com/ElementaryFunctions/Sinh/">Sinh</a>
     */
    public Complex sinh() {
        return sinh(real, imaginary, Complex::ofCartesian);
    }

    /**
     * Returns the hyperbolic sine of the complex number.
     *
     * <p>This function exists to allow implementation of the identity
     * {@code sin(z) = -i sinh(iz)}.<p>
     *
     * @param real Real part.
     * @param imaginary Imaginary part.
     * @param constructor Constructor.
     * @return The hyperbolic sine of the complex number.
     */
    private static Complex sinh(double real, double imaginary, ComplexConstructor constructor) {
        if ((Double.isInfinite(real) && !Double.isFinite(imaginary)) ||
            (real == 0 && !Double.isFinite(imaginary))) {
            return constructor.create(real, Double.NaN);
        }
        if (imaginary == 0 && !Double.isFinite(real)) {
            return constructor.create(real, imaginary);
        }
        return constructor.create(Math.sinh(real) * Math.cos(imaginary),
                                  Math.cosh(real) * Math.sin(imaginary));
    }

    /**
     * Returns the
     * <a href="http://mathworld.wolfram.com/SquareRoot.html">
     * square root</a> of this complex number.
     *
     * <p>\[ \sqrt{x + iy} = \frac{1}{2} \sqrt{2} \left( \sqrt{ \sqrt{x^2 + y^2} + x } + i\ \text{sgn}(y) \sqrt{ \sqrt{x^2 + y^2} - x } \right) \]
     *
     * <p>The square root of \( z \) is in the range \( [0, +\infty) \) along the real axis and
     * is unbounded along the imaginary axis. Special cases:
     *
     * <ul>
     * <li>{@code z.conj().sqrt() == z.sqrt().conj()}.
     * <li>If {@code z} is ±0 + i0, returns +0 + i0.
     * <li>If {@code z} is x + i∞ for all x (including NaN), returns +∞ + i∞.
     * <li>If {@code z} is x + iNaN for finite x, returns NaN + iNaN.
     * <li>If {@code z} is −∞ + iy for finite positive-signed y, returns +0 + i∞.
     * <li>If {@code z} is +∞ + iy for finite positive-signed y, returns +∞ + i0.
     * <li>If {@code z} is −∞ + iNaN, returns NaN ± i∞ (where the sign of the imaginary part of the result is unspecified).
     * <li>If {@code z} is +∞ + iNaN, returns +∞ + iNaN.
     * <li>If {@code z} is NaN + iy for finite y, returns NaN + iNaN.
     * <li>If {@code z} is NaN + iNaN, returns NaN + iNaN.
     * </ul>
     *
     * <p>Implements the following algorithm to compute \( \sqrt{x + iy} \):
     * <ol>
     * <li>Let \( t = \sqrt{2 (|x| + |x + iy|)} \)
     * <li>if \( x \geq 0 \) return \( \frac{t}{2} + i \frac{y}{t} \)
     * <li>else return \( \frac{|y|}{t} + i\ \text{sgn}(y) \frac{t}{2} \)
     * </ol>
     * where:
     * <ul>
     * <li>\( |x| =\ \){@link Math#abs(double) abs}(x)
     * <li>\( |x + y i| =\ \){@link Complex#abs}
     * <li>\( \text{sgn}(y) =\ \){@link Math#copySign(double,double) copySign}(1.0, y)
     * </ul>
     *
     * <p>The implementation is overflow and underflow safe based on the method described in:</p>
     * <blockquote>
     * T E Hull, Thomas F Fairgrieve and Ping Tak Peter Tang (1994)
     * Implementing complex elementary functions using exception handling.
     * ACM Transactions on Mathematical Software, Vol 20, No 2, pp 215-244.
     * </blockquote>
     *
     * @return The square root of this complex number.
     * @see <a href="http://functions.wolfram.com/ElementaryFunctions/Sqrt/">Sqrt</a>
     */
    public Complex sqrt() {
        return sqrt(real, imaginary);
    }

    /**
     * Returns the square root of the complex number {@code sqrt(x + i y)}.
     *
     * @param real Real component.
     * @param imaginary Imaginary component.
     * @return The square root of the complex number.
     */
    private static Complex sqrt(double real, double imaginary) {
        // Handle NaN
        if (Double.isNaN(real) || Double.isNaN(imaginary)) {
            // Check for infinite
            if (Double.isInfinite(imaginary)) {
                return new Complex(Double.POSITIVE_INFINITY, imaginary);
            }
            if (Double.isInfinite(real)) {
                if (real == Double.NEGATIVE_INFINITY) {
                    return new Complex(Double.NaN, Math.copySign(Double.POSITIVE_INFINITY, imaginary));
                }
                return new Complex(Double.POSITIVE_INFINITY, Double.NaN);
            }
            return NAN;
        }

        // Compute with positive values and determine sign at the end
        final double x = Math.abs(real);
        final double y = Math.abs(imaginary);

        // Compute
        double t;

        if (inRegion(x, y, SAFE_MIN, SAFE_MAX)) {
            // No over/underflow of x^2 + y^2
            t = Math.sqrt(2 * (Math.sqrt(x * x + y * y) + x));
        } else {
            // Potential over/underflow. First check infinites and real/imaginary only.

            // Check for infinite
            if (isPosInfinite(y)) {
                return new Complex(Double.POSITIVE_INFINITY, imaginary);
            } else if (isPosInfinite(x)) {
                if (real == Double.NEGATIVE_INFINITY) {
                    return new Complex(0, Math.copySign(Double.POSITIVE_INFINITY, imaginary));
                }
                return new Complex(Double.POSITIVE_INFINITY, Math.copySign(0, imaginary));
            } else if (y == 0) {
                // Real only
                final double sqrtAbs = Math.sqrt(x);
                if (real < 0) {
                    return new Complex(0, Math.copySign(sqrtAbs, imaginary));
                }
                return new Complex(sqrtAbs, imaginary);
            } else if (x == 0) {
                // Imaginary only
                final double sqrtAbs = Math.sqrt(y) / ROOT2;
                return new Complex(sqrtAbs, Math.copySign(sqrtAbs, imaginary));
            } else {
                // Over/underflow
                // scale so that abs(x) is near 1, with even exponent.
                final int scale = getMaxExponent(x, y) & MASK_INT_TO_EVEN;
                final double sx = Math.scalb(x, -scale);
                final double sy = Math.scalb(y, -scale);
                final double st = Math.sqrt(2 * (Math.sqrt(sx * sx + sy * sy) + sx));
                // Rescale. This works if exponent is even:
                // st * sqrt(2^scale) = st * (2^scale)^0.5 = st * 2^(scale*0.5)
                t = Math.scalb(st, scale / 2);
            }
        }

        if (real >= 0) {
            return new Complex(t / 2, imaginary / t);
        }
        return new Complex(y / t, Math.copySign(t / 2, imaginary));
    }

    /**
     * Returns the
     * <a href="http://mathworld.wolfram.com/Tangent.html">
     * tangent</a> of this complex number.
     *
     * <p>\[ \tan(z) = \frac{i(e^{-iz} - e^{iz})}{e^{-iz} + e^{iz}} \]
     *
     * <p>This is an odd function: \( \tan(z) = -\tan(-z) \).
     *
     * <p>This is implemented using real \( x \) and imaginary \( y \) parts:</p>
     * \[ \tan(x + iy) = \frac{\sin(2x)}{\cos(2x)+\cosh(2y)} + i \frac{\sinh(2y)}{\cos(2x)+\cosh(2y)} \]
     *
     * <p>As per the C99 standard this function is computed using the trigonomic identity:</p>
     * \[ \tan(z) = -i \tanh(iz) \]
     *
     * @return The tangent of this complex number.
     * @see <a href="http://functions.wolfram.com/ElementaryFunctions/Tan/">Tangent</a>
     */
    public Complex tan() {
        // Define in terms of tanh
        // tan(z) = -i tanh(iz)
        // Multiply this number by I, compute tanh, then multiply by back
        return tanh(-imaginary, real, Complex::multiplyNegativeI);
    }

    /**
     * Returns the
     * <a href="http://mathworld.wolfram.com/HyperbolicTangent.html">
     * hyperbolic tangent</a> of this complex number.
     *
     * <p>\[ \tanh(z) = \frac{e^z - e^{-z}}{e^z + e^{-z}} \]
     *
     * <p>The hyperbolic tangent of \( z \) is an entire function in the complex plane.
     * and is periodic with respect to the imaginary component with period \( \pi i \)
     * and has poles of the first order along the imaginary line, at coordinates
     * \( (0, \pi(\frac{1}{2} + n)) \).
     * Note that the {@code double} floating-point representation is unable to exactly represent
     * \( \pi/2 \) and there is no value for which a pole error occurs.
     *
     * <ul>
     * <li>{@code z.conj().tanh() == z.tanh().conj()}.
     * <li>This is an odd function: \( \tanh(z) = -\tanh(-z) \).
     * <li>If {@code z} is +0 + i0, returns +0 + i0.
     * <li>If {@code z} is x + i∞ for finite x, returns NaN + iNaN.
     * <li>If {@code z} is x + iNaN for finite x, returns NaN + iNaN.
     * <li>If {@code z} is +∞ + iy for positive-signed finite y, returns 1 + i0 sin(2y).
     * <li>If {@code z} is +∞ + i∞, returns 1 ± i0 (where the sign of the imaginary part of the result is unspecified).
     * <li>If {@code z} is +∞ + iNaN, returns 1 ± i0 (where the sign of the imaginary part of the result is unspecified).
     * <li>If {@code z} is NaN + i0, returns NaN + i0.
     * <li>If {@code z} is NaN + iy for all nonzero numbers y, returns NaN + iNaN.
     * <li>If {@code z} is NaN + iNaN, returns NaN + iNaN.
     * </ul>
     *
     * <p>This is implemented using real \( x \) and imaginary \( y \) parts:
     *
     * <p>\[ \tan(x + iy) = \frac{\sinh(2x)}{\cosh(2x)+\cos(2y)} + i \frac{\sin(2y)}{\cosh(2x)+\cos(2y)} \]
     *
     * @return The hyperbolic tangent of this complex number.
     * @see <a href="http://functions.wolfram.com/ElementaryFunctions/Tanh/">Tanh</a>
     */
    public Complex tanh() {
        return tanh(real, imaginary, Complex::ofCartesian);
    }

    /**
     * Returns the hyperbolic tangent of this complex number.
     *
     * <p>This function exists to allow implementation of the identity
     * {@code tan(z) = -i tanh(iz)}.<p>
     *
     * @param real Real part.
     * @param imaginary Imaginary part.
     * @param constructor Constructor.
     * @return The hyperbolic tangent of the complex number.
     */
    private static Complex tanh(double real, double imaginary, ComplexConstructor constructor) {
        if (Double.isInfinite(real)) {
            if (Double.isFinite(imaginary)) {
                return constructor.create(Math.copySign(1, real), Math.copySign(0, sin2(imaginary)));
            }
            // imaginary is infinite or NaN
            return constructor.create(Math.copySign(1, real), Math.copySign(0, imaginary));
        }

        if (real == 0) {
            if (Double.isFinite(imaginary)) {
                // Identity: sin x / (1 + cos x) = tan(x/2)
                return constructor.create(real, Math.tan(imaginary));
            }
            return constructor.create(Double.NaN, Double.NaN);
        }
        if (imaginary == 0) {
            if (Double.isNaN(real)) {
                return constructor.create(Double.NaN, imaginary);
            }
            // Identity: sinh x / (1 + cosh x) = tanh(x/2)
            return constructor.create(Math.tanh(real), imaginary);
        }

        final double real2 = 2 * real;

        // Math.cosh returns positive infinity for infinity.
        // cosh -> inf
        final double divisor = Math.cosh(real2) + cos2(imaginary);

        // Math.sinh returns the input infinity for infinity.
        // sinh -> inf for positive x; else -inf
        final double sinhRe2 = Math.sinh(real2);

        // Avoid inf / inf
        if (Double.isInfinite(sinhRe2) && Double.isInfinite(divisor)) {
            // Handle as if real was infinite
            return constructor.create(Math.copySign(1, real), Math.copySign(0, imaginary));
        }
        return constructor.create(sinhRe2 / divisor,
                                  sin2(imaginary) / divisor);
    }

    /**
     * Safely compute {@code cos(2*a)} when {@code a} is finite.
     * Note that {@link Math#cos(double)} returns NaN when the input is infinite.
     * If {@code 2*a} is finite use {@code Math.cos(2*a)}; otherwise use the identity:
     * <pre>
     * <code>
     *   cos(2a) = 2 cos<sup>2</sup>(a) - 1
     * </code>
     * </pre>
     *
     * @param a Angle a.
     * @return The cosine of 2a.
     * @see Math#cos(double)
     */
    private static double cos2(double a) {
        final double twoA = 2 * a;
        if (Double.isFinite(twoA)) {
            return Math.cos(twoA);
        }
        final double cosA = Math.cos(a);
        return 2 * cosA * cosA - 1;
    }

    /**
     * Safely compute {@code sin(2*a)} when {@code a} is finite.
     * Note that {@link Math#sin(double)} returns NaN when the input is infinite.
     * If {@code 2*a} is finite use {@code Math.sin(2*a)}; otherwise use the identity:
     * <pre>
     * <code>
     *   sin(2a) = 2 sin(a) cos(a)
     * </code>
     * </pre>
     *
     * @param a Angle a.
     * @return The sine of 2a.
     * @see Math#sin(double)
     */
    private static double sin2(double a) {
        final double twoA = 2 * a;
        if (Double.isFinite(twoA)) {
            return Math.sin(twoA);
        }
        return 2 * Math.sin(a) * Math.cos(a);
    }

    /**
     * Returns the argument of this complex number.
     *
     * <p>The argument is the angle phi between the positive real axis and
     * the point representing this number in the complex plane.
     * The value returned is between \( -\pi \) (not inclusive)
     * and \( \pi \) (inclusive), with negative values returned for numbers with
     * negative imaginary parts.
     *
     * <p>If either real or imaginary part (or both) is NaN, NaN is returned.
     * Infinite parts are handled as {@linkplain Math#atan2} handles them,
     * essentially treating finite parts as zero in the presence of an
     * infinite coordinate and returning a multiple of \( \frac{\pi}{4} \) depending on
     * the signs of the infinite parts.
     *
     * <p>This code follows the
     * <a href="http://www.iso-9899.info/wiki/The_Standard">ISO C Standard</a>, Annex G,
     * in calculating the returned value using the {@code atan2(y, x)} method for complex
     * \( x + iy \).
     *
     * @return The argument of this complex number.
     * @see Math#atan2(double, double)
     */
    public double arg() {
        // Delegate
        return Math.atan2(imaginary, real);
    }

    /**
     * Returns the n-th roots of this complex number.
     * The nth roots are defined by the formula:
     *
     * <p>\[ z_k = |z|^{\frac{1}{n}} \left( \cos \left(\phi + \frac{2\pi k}{n} \right) + i \sin \left(\phi + \frac{2\pi k}{n} \right) \right) \]
     *
     * <p>for \( k=0, 1, \ldots, n-1 \), where \( |z| \) and \( \phi \)
     * are respectively the {@link #abs() modulus} and
     * {@link #arg() argument} of this complex number.
     *
     * <p>If one or both parts of this complex number is NaN, a list with all
     * all elements set to {@code NaN + i NaN} is returned.</p>
     *
     * @param n Degree of root.
     * @return A list of all {@code n}-th roots of this complex number.
     * @throws IllegalArgumentException if {@code n} is zero.
     * @see <a href="http://functions.wolfram.com/ElementaryFunctions/Root/">Root</a>
     */
    public List<Complex> nthRoot(int n) {
        if (n == 0) {
            throw new IllegalArgumentException("cannot compute zeroth root");
        }

        final List<Complex> result = new ArrayList<>();

        // nth root of abs -- faster / more accurate to use a solver here?
        final double nthRootOfAbs = Math.pow(abs(), 1.0 / n);

        // Compute nth roots of complex number with k = 0, 1, ... n-1
        final double nthPhi = arg() / n;
        final double slice = 2 * Math.PI / n;
        double innerPart = nthPhi;
        for (int k = 0; k < Math.abs(n); k++) {
            // inner part
            final double realPart = nthRootOfAbs *  Math.cos(innerPart);
            final double imaginaryPart = nthRootOfAbs *  Math.sin(innerPart);
            result.add(new Complex(realPart, imaginaryPart));
            innerPart += slice;
        }

        return result;
    }

    /**
     * Returns a string representation of the complex number.
     *
     * <p>The string will represent the numeric values of the real and imaginary parts.
     * The values are split by a separator and surrounded by parentheses.
     * The string can be {@link #parse(String) parsed} to obtain an instance with the same value.
     *
     * <p>The format for complex number \( x + i y \) is {@code "(x,y)"}, with \( x \) and
     * \( y \) converted as if using {@link Double#toString(double)}.
     *
     * @return A string representation of the complex number.
     * @see #parse(String)
     * @see Double#toString(double)
     */
    @Override
    public String toString() {
        return new StringBuilder(TO_STRING_SIZE)
            .append(FORMAT_START)
            .append(real).append(FORMAT_SEP)
            .append(imaginary)
            .append(FORMAT_END)
            .toString();
    }

    /**
     * Returns {@code true} if the values are equal according to semantics of
     * {@link Double#equals(Object)}.
     *
     * @param x Value
     * @param y Value
     * @return {@code Double.valueof(x).equals(Double.valueOf(y))}.
     */
    private static boolean equals(double x, double y) {
        return Double.doubleToLongBits(x) == Double.doubleToLongBits(y);
    }

    /**
     * Check that a value is negative. It must meet all the following conditions:
     * <ul>
     *  <li>it is not {@code NaN},</li>
     *  <li>it is negative signed,</li>
     * </ul>
     *
     * <p>Note: This is true for negative zero.</p>
     *
     * @param d Value.
     * @return {@code true} if {@code d} is negative.
     */
    private static boolean negative(double d) {
        return d < 0 || Double.doubleToLongBits(d) == NEGATIVE_ZERO_LONG_BITS;
    }

    /**
     * Check that a value is positive infinity. Used to replace {@link Double#isInfinite()}
     * when the input value is known to be positive (i.e. in the case where it have been
     * set using {@link Math#abs(double)}).
     *
     * @param d Value.
     * @return {@code true} if {@code d} is +inf.
     */
    private static boolean isPosInfinite(double d) {
        return d == Double.POSITIVE_INFINITY;
    }

    /**
     * Create a complex number given the real and imaginary parts, then multiply by {@code -i}.
     * This is used in functions that implement trigonomic identities. It is the functional
     * equivalent of:
     *
     * <pre>
     *   z = new Complex(real, imaginary).multiplyImaginary(-1);
     * </pre>
     *
     * @param real Real part.
     * @param imaginary Imaginary part.
     * @return {@code Complex} object.
     */
    private static Complex multiplyNegativeI(double real, double imaginary) {
        return new Complex(imaginary, -real);
    }

    /**
     * Change the sign of the magnitude based on the signed value.
     *
     * <p>If the signed value is negative then the result is {@code -magnitude}; otherwise
     * return {@code magnitude}.
     *
     * <p>A signed value of {@code -0.0} is treated as negative. A signed value of {@code NaN}
     * is treated as positive.
     *
     * <p>This is not the same as {@link Math#copySign(double, double)} as this method
     * will change the sign based on the signed value rather than copy the sign.
     *
     * @param magnitude the magnitude
     * @param signedValue the signed value
     * @return magnitude or -magnitude.
     * @see #negative(double)
     */
    private static double changeSign(double magnitude, double signedValue) {
        return negative(signedValue) ? -magnitude : magnitude;
    }

    /**
     * Returns the largest unbiased exponent used in the representation of the
     * two numbers. Special cases:
     *
     * <ul>
     * <li>If either argument is NaN or infinite, then the result is
     * {@link Double#MAX_EXPONENT} + 1.
     * <li>If both arguments are zero or subnormal, then the result is
     * {@link Double#MIN_EXPONENT} -1.
     * </ul>
     *
     * @param a the first value
     * @param b the second value
     * @return The maximum unbiased exponent of the values.
     * @see Math#getExponent(double)
     */
    private static int getMaxExponent(double a, double b) {
        // This could return:
        // Math.getExponent(Math.max(Math.abs(a), Math.abs(b)))
        // A speed test is required to determine performance.

        return Math.max(Math.getExponent(a), Math.getExponent(b));
    }

    /**
     * Checks if both x and y are in the region defined by the minimum and maximum.
     *
     * @param x x value.
     * @param y y value.
     * @param min the minimum (exclusive).
     * @param max the maximum (exclusive).
     * @return true if inside the region.
     */
    private static boolean inRegion(double x, double y, double min, double max) {
        return (x < max) && (x > min) && (y < max) && (y > min);
    }

    /**
     * Creates an exception.
     *
     * @param message Message prefix.
     * @param error Input that caused the error.
     * @param cause Underlying exception (if any).
     * @return A new instance.
     */
    private static NumberFormatException parsingException(String message,
                                                          Object error,
                                                          Throwable cause) {
        // Not called with a null message or error
        final StringBuilder sb = new StringBuilder(100)
            .append(message)
            .append(" '").append(error).append('\'');
        if (cause != null) {
            sb.append(": ").append(cause.getMessage());
        }

        return new NumberFormatException(sb.toString());
    }
}
