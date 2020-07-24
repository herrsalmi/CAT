package com.utils;

import java.util.function.Function;

/**
 * project CAT
 * Created by ayyoub on 11/2/17.
 */
public class SimpsonsRule {

    private Function<Double, Double> f;

    /**********************************************************************
     * Integrate f from a to b using Simpson's rule.
     * Increase N for more precision.
     **********************************************************************/
    private double simpson(double a, double b) {
        int precision = 10000;                    // precision parameter
        double h = (b - a) / (precision - 1);     // step size

        // 1/3 terms
        double sum = 1.0 / 3.0 * (f.apply(a) + f.apply(b));

        // 4/3 terms
        for (int i = 1; i < precision - 1; i += 2) {
            double x = a + h * i;
            sum += 4.0 / 3.0 * f.apply(x);
        }

        // 2/3 terms
        for (int i = 2; i < precision - 1; i += 2) {
            double x = a + h * i;
            sum += 2.0 / 3.0 * f.apply(x);
        }

        return sum * h;
    }

    public double integrate(int n, int m) {
        f = p -> 2 * Math.pow(p * (1 - p), n) * Math.pow(1 - 2 * p * (1 - p), m);
        return simpson(0, 1);
    }

}

