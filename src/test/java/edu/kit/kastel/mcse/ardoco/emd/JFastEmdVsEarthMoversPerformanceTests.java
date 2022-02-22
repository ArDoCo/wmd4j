/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.emd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Created by Majer on 22. 09. 2016. Updated by Jan Keim in 2022
 */
class JFastEmdVsEarthMoversPerformanceTests {
    private static Logger logger = LogManager.getLogger();

    @Disabled("Disabled for CI, execute manually and only locally")
    @Test
    void performanceComparison() {
        var earthMovers = new EarthMovers();

        var size = 20;
        var a = EarthMoversUtils.randomVector(size);
        var b = EarthMoversUtils.randomVector(size);
        var m = EarthMoversUtils.matrix(a, b);

        var repeats = 10000;

        // warm up
        for (var i = 0; i < repeats; i++) {
            earthMovers.distance(a, b, m, 1);
            EarthMoversUtils.jfastemd(a, b, m, 1);
        }

        var start = System.nanoTime();

        for (var i = 0; i < repeats; i++) {
            EarthMoversUtils.jfastemd(a, b, m, 1);
        }

        var duration = System.nanoTime() - start;
        logger.debug(duration + " = jfastemd");

        start = System.nanoTime();
        for (var i = 0; i < repeats; i++) {
            earthMovers.distance(a, b, m, 1);
        }

        var durationOptimized = System.nanoTime() - start;
        logger.debug(duration + " = optimized-jfastemd");

        Assertions.assertTrue(durationOptimized < duration);
    }
}
