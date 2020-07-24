package com.csc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Project checkStatusConcordance
 * Created by ayyoub on 5/16/17.
 */
public class HypergeometricDistribution {

    private static final int MAX_SIZE = 42;
    private static final ArrayList<BigInteger> f = new ArrayList<>();

    static {
        f.add(BigInteger.ONE);
        getFactorial(MAX_SIZE);
    }

    private HypergeometricDistribution(){

    }

    /**
     * Function that returns factorial. Uses dynamic programming to speed up
     * calculations. Quite efficient for factorials below 1000.
     *
     * @param nr Factorial to calculate.
     * @return factorial or null for negative numbers.
     */
    private static BigInteger getFactorial(int nr) throws OutOfMemoryError {
        if (nr < 0)
            return null;
        for (int i = f.size(); i <= nr; i++)
            f.add(f.get(i - 1).multiply(BigInteger.valueOf(i)));
        return f.get(nr);
    }

    /**
     * Using multiplicative formula.
     *
     * @param n nr of elements, nonnegative integer, with k ? n
     * @param k nr of distinct elements, nonnegative integer
     * @return Binomial coefficient or null for invalid inputs.
     */
    static BigInteger getBinomialCoefficient(int n, int k)
            throws OutOfMemoryError {
        if (k < 1 || k > n)
            return null;
        return BigInteger.valueOf(n).pow(k).divide(Objects.requireNonNull(getFactorial(k)));
    }

    /**
     * Based on <a
     * href="http://mathworld.wolfram.com/FishersExactTest.html"> Fisher's
     * exact test</a>.
     *
     * @param a ArrayList, nonnegative integers
     * @return Hypergeometric distribution.
     */
    public static BigDecimal getValue(List<Integer> a, int scale, RoundingMode rm) throws OutOfMemoryError {
        return getValue(new int[][]{{a.get(0), a.get(1)}, {a.get(2), a.get(3)}}, scale, rm);
    }

    /**
     * Based on <a
     * href="http://mathworld.wolfram.com/FishersExactTest.html"> Fisher's
     * exact test</a>.
     *
     * @param a table [], nonnegative integers
     * @return Hypergeometric distribution.
     */
    public static BigDecimal getValue(int[][] a, int scale, RoundingMode rm) throws OutOfMemoryError {
        ArrayList<Integer> lR = new ArrayList<>();
        ArrayList<Integer> lC = new ArrayList<>();
        ArrayList<Integer> lE = new ArrayList<>();
        int n = 0;

        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                if (a[i][j] < 0)
                    return null;

                n += a[i][j];
                add(lC, j, a[i][j]);
                add(lR, i, a[i][j]);
                lE.add(a[i][j]);
            }
        }
        BigDecimal term1 = //
                new BigDecimal(multiplyFactorials(lC).multiply(multiplyFactorials(lR)));
        BigDecimal term2 = //
                new BigDecimal(Objects.requireNonNull(getFactorial(n)).multiply(multiplyFactorials(lE)));

        return term1.divide(term2, scale, rm);
    }

    // utility method
    private static BigInteger multiplyFactorials(List<Integer> c) {
        BigInteger sum = BigInteger.ONE;
        for (Integer i : c) {
            sum = sum.multiply(Objects.requireNonNull(getFactorial(i)));
        }
        return sum;
    }

    // utility method
    private static void add(List<Integer> r, int nr, int val) {
        while (r.size() <= nr)
            r.add(0);
        r.set(nr, r.get(nr) + val);
    }
}
