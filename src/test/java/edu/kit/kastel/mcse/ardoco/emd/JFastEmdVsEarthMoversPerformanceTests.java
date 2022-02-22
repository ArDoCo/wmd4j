/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.emd;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Created by Majer on 22. 09. 2016. Updated by Jan Keim in 2022
 */
public class JFastEmdVsEarthMoversPerformanceTests {

    @Disabled("Disabled for CI, execute manually and only locally")
    @Test
    public void performanceComparison() {
        EarthMovers earthMovers = new EarthMovers();

        int size = 20;
        double[] a = EarthMoversUtils.randomVector(size);
        double[] b = EarthMoversUtils.randomVector(size);
        double[][] m = EarthMoversUtils.matrix(a, b);

        int repeats = 10000;

        // warm up
        for (int i = 0; i < repeats; i++) {
            earthMovers.distance(a, b, m, 1);
            EarthMoversUtils.jfastemd(a, b, m, 1);
        }

        long start = System.nanoTime();

        for (int i = 0; i < repeats; i++) {
            EarthMoversUtils.jfastemd(a, b, m, 1);
        }

        System.out.println((System.nanoTime() - start) + " = jfastemd");

        start = System.nanoTime();
        for (int i = 0; i < repeats; i++) {
            earthMovers.distance(a, b, m, 1);
        }

        System.out.println((System.nanoTime() - start) + " = optimized-jfastemd");

    }
}
