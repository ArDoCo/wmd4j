/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.emd;

import static junit.framework.TestCase.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * @author Telmo Menezes (telmo@telmomenezes.com)
 */
public class EarthMoversTests {

    EarthMovers earthMovers = new EarthMovers();
    Random random = new Random();

    @Test
    public void distance() {

        // compare earctMovers results to jfastemd (reference implementation) results on 10k random vectors
        for (int i = 0; i < 10000; i++) {

            int size = random.nextInt(10) + 1;

            double[] a = EarthMoversUtils.randomVector(size);
            double[] b = EarthMoversUtils.randomVector(size);
            double[][] m = EarthMoversUtils.matrix(a, b);

            assertEquals(EarthMoversUtils.jfastemd(a, b, m, 1), earthMovers.distance(a, b, m, 1));
        }
    }

}
