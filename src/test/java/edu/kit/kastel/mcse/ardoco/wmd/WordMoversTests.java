/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.wmd;

import static junit.framework.TestCase.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.WordMovers;

/**
 * Created by Majer on 22. 09. 2016. Updated by Jan Keim in 2022
 */
class WordMoversTests {

    @Test
    void distance() throws IOException {
        var vectors = WordVectorSerializer.loadStaticModel(new File("src/util/resources/tinyw2v.model"));
        var wm = WordMovers.builder().wordVectors(vectors).build();

        try (var stream = Files.lines(Paths.get("src/test/resources/gensim_distances.csv"))) {

            stream.forEach(x -> {
                var triple = x.split(",");

                var distance = wm.distance(triple[0].trim(), triple[1].trim());
                var gensimDistance = Double.parseDouble(triple[2].trim());

                assertEquals(String.format("%.5f", gensimDistance), String.format("%.5f", distance));
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
