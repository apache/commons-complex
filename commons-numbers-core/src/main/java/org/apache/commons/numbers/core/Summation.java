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
package org.apache.commons.numbers.core;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

/** Class providing accurate floating-point summations. The methods provided
 * use a compensated summation technique to reduce numerical errors.
 * The approach is based on the <em>Sum2S</em> algorithm described in the
 * 2005 paper <a href="https://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.2.1547">
 * Accurate Sum and Dot Product</a> by Takeshi Ogita, Siegfried M. Rump,
 * and Shin'ichi Oishi published in <em>SIAM J. Sci. Comput</em>.
 *
 * <p>Method results follow the standard rules for IEEE 754 addition. For example,
 * if any input value is NaN, the result is NaN.
 */
public final class Summation {

    /** Utility class; no instantiation. */
    private Summation() {}

    /** Compute the sum of the input values.
     * @param a first value
     * @param b second value
     * @param c third value
     * @return sum of the input values
     */
    public static double value(final double a, final double b, final double c) {
        double sum = a;
        double comp = 0d;

        final double sb = sum + b;
        comp += ExtendedPrecision.twoSumLow(sum, b, sb);
        sum = sb;

        final double sc = sum + c;
        comp += ExtendedPrecision.twoSumLow(sum, c, sc);
        sum = sc;

        return summationResult(sum, comp);
    }

    /** Compute the sum of the input values.
     * @param a first value
     * @param b second value
     * @param c third value
     * @param d fourth value
     * @return sum of the input values
     */
    public static double value(final double a, final double b, final double c, final double d) {
        double sum = a;
        double comp = 0d;

        final double sb = sum + b;
        comp += ExtendedPrecision.twoSumLow(sum, b, sb);
        sum = sb;

        final double sc = sum + c;
        comp += ExtendedPrecision.twoSumLow(sum, c, sc);
        sum = sc;

        final double sd = sum + d;
        comp += ExtendedPrecision.twoSumLow(sum, d, sd);
        sum = sd;

        return summationResult(sum, comp);
    }

    /** Compute the sum of the input values.
     * @param a first value
     * @param b second value
     * @param c third value
     * @param d fourth value
     * @param e fifth value
     * @return sum of the input values
     */
    public static double value(final double a, final double b, final double c, final double d,
            final double e) {
        double sum = a;
        double comp = 0d;

        final double sb = sum + b;
        comp += ExtendedPrecision.twoSumLow(sum, b, sb);
        sum = sb;

        final double sc = sum + c;
        comp += ExtendedPrecision.twoSumLow(sum, c, sc);
        sum = sc;

        final double sd = sum + d;
        comp += ExtendedPrecision.twoSumLow(sum, d, sd);
        sum = sd;

        final double se = sum + e;
        comp += ExtendedPrecision.twoSumLow(sum, e, se);
        sum = se;

        return summationResult(sum, comp);
    }

    /** Compute the sum of the input values.
     * @param a first value
     * @param b second value
     * @param c third value
     * @param d fourth value
     * @param e fifth value
     * @param f sixth value
     * @return sum of the input values
     */
    public static double value(final double a, final double b, final double c, final double d,
            final double e, final double f) {
        double sum = a;
        double comp = 0d;

        final double sb = sum + b;
        comp += ExtendedPrecision.twoSumLow(sum, b, sb);
        sum = sb;

        final double sc = sum + c;
        comp += ExtendedPrecision.twoSumLow(sum, c, sc);
        sum = sc;

        final double sd = sum + d;
        comp += ExtendedPrecision.twoSumLow(sum, d, sd);
        sum = sd;

        final double se = sum + e;
        comp += ExtendedPrecision.twoSumLow(sum, e, se);
        sum = se;

        final double sf = sum + f;
        comp += ExtendedPrecision.twoSumLow(sum, f, sf);
        sum = sf;

        return summationResult(sum, comp);
    }

    /** Compute the sum of the input values.
     * @param a array containing values to sum
     * @return sum of the input values
     */
    public static double value(final double[] a) {
        double sum = 0d;
        double comp = 0d;

        for (final double x : a) {
            final double s = sum + x;
            comp += ExtendedPrecision.twoSumLow(sum, x, s);
            sum = s;
        }

        return summationResult(sum, comp);
    }

    /** Return a new {@link Accumulator} instance for computing a
     * running sum of values. The returned instance contains an
     * initial value of zero.
     * @return accumulator instance for computing a running sum of values
     */
    public static Accumulator accumulator() {
        return accumulator(0d);
    }

    /** Return a new {@link Accumulator} instance for computing a running
     * sum of values.
     * @param initial initial value of the accumulator
     * @return accumulator instance for computing a running sum of values
     */
    public static Accumulator accumulator(final double initial) {
        return new Accumulator(initial);
    }

    /** Return the final result from a summation operation.
     * @param sum standard sum value
     * @param comp compensation value
     * @return final summation result
     */
    static double summationResult(final double sum, final double comp) {
        // only add comp if finite; otherwise, return the raw sum
        // to comply with standard double addition rules
        return Double.isFinite(comp) ?
                sum + comp :
                sum;
    }

    /** Class for computing a high-accuracy running sum of values. The algorithm used is
     * the same as that for the static {@link Summation} methods.
     */
    public static final class Accumulator implements DoubleSupplier, DoubleConsumer {
        /** Sum value. */
        private double sum;
        /** Compensation value. */
        private double comp;

        /** Construct a new instance with the given initial value.
         * @param initial initial value
         */
        private Accumulator(final double initial) {
            sum = initial;
        }

        /** Add a value to the summation.
         * @param a value to add
         * @return this instance
         */
        public Accumulator add(final double a) {
            final double t = sum + a;
            comp += ExtendedPrecision.twoSumLow(sum, a, t);
            sum = t;

            return this;
        }

        /** Add a value to the summation. This is equivalent
         * to {@link #add(double)} but without the current instance
         * as the return value.
         * @param value value to add
         */
        @Override
        public void accept(final double value) {
            add(value);
        }

        /** Get the summation result. Values can still be added
         * to the instance after calling this method.
         * @return summation result
         */
        @Override
        public double getAsDouble() {
            return summationResult(sum, comp);
        }
    }
}
