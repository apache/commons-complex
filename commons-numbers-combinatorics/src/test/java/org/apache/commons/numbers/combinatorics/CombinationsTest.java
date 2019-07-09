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
package org.apache.commons.numbers.combinatorics;

import java.util.Iterator;
import java.util.Comparator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link Combinations} class.
 */
class CombinationsTest {
    @Test
    void testAccessor1() {
        final int n = 5;
        final int k = 3;
        Assertions.assertEquals(n, new Combinations(n, k).getN());
    }
    @Test
    void testAccessor2() {
        final int n = 5;
        final int k = 3;
        Assertions.assertEquals(k, new Combinations(n, k).getK());
    }

    @Test
    void testLexicographicIterator() {
        checkLexicographicIterator(new Combinations.LexicographicComparator(5, 3));
        checkLexicographicIterator(new Combinations.LexicographicComparator(6, 4));
        checkLexicographicIterator(new Combinations.LexicographicComparator(8, 2));
        checkLexicographicIterator(new Combinations.LexicographicComparator(6, 1));
        checkLexicographicIterator(new Combinations.LexicographicComparator(3, 3));
        checkLexicographicIterator(new Combinations.LexicographicComparator(1, 1));
        checkLexicographicIterator(new Combinations.LexicographicComparator(1, 0));
        checkLexicographicIterator(new Combinations.LexicographicComparator(0, 0));
        checkLexicographicIterator(new Combinations.LexicographicComparator(4, 2));
        checkLexicographicIterator(new Combinations.LexicographicComparator(123, 2));
    }

    @Test
    void testLexicographicComparatorWrongIterate1() {
        final int n = 5;
        final int k = 3;
        final Comparator<int[]> comp = new Combinations.LexicographicComparator(n, k);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> comp.compare(new int[] {1}, new int[] {0, 1, 2})
        );
    }

    @Test
    void testLexicographicComparatorWrongIterate2() {
        final int n = 5;
        final int k = 3;
        final Comparator<int[]> comp = new Combinations.LexicographicComparator(n, k);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> comp.compare(new int[] {0, 1, 2}, new int[] {0, 1, 2, 3})
        );
    }

    @Test
    void testLexicographicComparatorWrongIterate3() {
        final int n = 5;
        final int k = 3;
        final Comparator<int[]> comp = new Combinations.LexicographicComparator(n, k);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> comp.compare(new int[] {1, 2, 5}, new int[] {0, 1, 2})
        );
    }

    @Test
    void testLexicographicComparatorWrongIterate4() {
        final int n = 5;
        final int k = 3;
        final Comparator<int[]> comp = new Combinations.LexicographicComparator(n, k);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> comp.compare(new int[] {1, 2, 4}, new int[] {-1, 1, 2})
        );
    }

    @Test
    void testLexicographicComparatorAccessors() {
        final int n = 9;
        final int k = 6;
        final Combinations.LexicographicComparator comp =
            new Combinations.LexicographicComparator(n, k);
        Assertions.assertEquals(n, comp.getN());
        Assertions.assertEquals(k, comp.getK());
    }

    @Test
    void testLexicographicComparator() {
        final int n = 5;
        final int k = 3;
        final Comparator<int[]> comp = new Combinations.LexicographicComparator(n, k);
        Assertions.assertEquals(1, comp.compare(new int[] {1, 2, 4},
                                            new int[] {1, 2, 3}));
        Assertions.assertEquals(-1, comp.compare(new int[] {0, 1, 4},
                                             new int[] {0, 2, 4}));
        Assertions.assertEquals(0, comp.compare(new int[] {1, 3, 4},
                                            new int[] {1, 3, 4}));
    }

    /**
     * Check that iterates can be passed unsorted.
     */
    @Test
    void testLexicographicComparatorUnsorted() {
        final int n = 5;
        final int k = 3;
        final Comparator<int[]> comp = new Combinations.LexicographicComparator(n, k);
        Assertions.assertEquals(1, comp.compare(new int[] {1, 4, 2},
                                            new int[] {1, 3, 2}));
        Assertions.assertEquals(-1, comp.compare(new int[] {0, 4, 1},
                                             new int[] {0, 4, 2}));
        Assertions.assertEquals(0, comp.compare(new int[] {1, 4, 3},
                                            new int[] {1, 3, 4}));
    }

    @Test
    void testEmptyCombination() {
        final Iterator<int[]> iter = new Combinations(12345, 0).iterator();
        Assertions.assertTrue(iter.hasNext());
        final int[] c = iter.next();
        Assertions.assertEquals(0, c.length);
        Assertions.assertFalse(iter.hasNext());
    }

    @Test
    void testFullSetCombination() {
        final int n = 67;
        final Iterator<int[]> iter = new Combinations(n, n).iterator();
        Assertions.assertTrue(iter.hasNext());
        final int[] c = iter.next();
        Assertions.assertEquals(n, c.length);

        for (int i = 0; i < n; i++) {
            Assertions.assertEquals(i, c[i]);
        }

        Assertions.assertFalse(iter.hasNext());
    }

    /**
     * Verifies that the iterator generates a lexicographically
     * increasing sequence of b(n,k) arrays, each having length k
     * and each array itself increasing.
     *
     * @param comp Comparator.
     */
    private static void checkLexicographicIterator(Combinations.LexicographicComparator comp) {
        final int n = comp.getN();
        final int k = comp.getK();

        int[] lastIterate = null;

        long numIterates = 0;
        for (int[] iterate : new Combinations(n, k)) {
            Assertions.assertEquals(k, iterate.length);

            // Check that the sequence of iterates is ordered.
            if (lastIterate != null) {
                Assertions.assertEquals(1, comp.compare(iterate, lastIterate));
            }

            // Check that each iterate is ordered.
            for (int i = 1; i < iterate.length; i++) {
                Assertions.assertTrue(iterate[i] > iterate[i - 1]);
            }

            lastIterate = iterate;
            ++numIterates;
        }

        // Check the number of iterates.
        Assertions.assertEquals(BinomialCoefficient.value(n, k), numIterates);
    }

    @Test
    void testCombinationsIteratorFail1() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Combinations(4, 5).iterator()
        );
    }
    @Test
    void testCombinationsIteratorFail2() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Combinations(-1, -2).iterator()
        );
    }
}
