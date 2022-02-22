/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.emd;

import static junit.framework.TestCase.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * @author Telmo Menezes (telmo@telmomenezes.com)
 */
class EarthMoversTests {

    EarthMovers earthMovers = new EarthMovers();
    Random random = new Random();

    @Test
    void distance() {

        // compare earctMovers results to jfastemd (reference implementation) results on 10k random vectors
        for (var i = 0; i < 10000; i++) {

            var size = random.nextInt(10) + 1;

            var a = EarthMoversUtils.randomVector(size);
            var b = EarthMoversUtils.randomVector(size);
            var m = EarthMoversUtils.matrix(a, b);

            assertEquals(EarthMoversUtils.jfastemd(a, b, m, 1), earthMovers.distance(a, b, m, 1));
        }
    }

}
