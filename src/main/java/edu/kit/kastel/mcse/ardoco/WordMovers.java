/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Stream;

import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;

import edu.kit.kastel.mcse.ardoco.emd.EarthMovers;

/**
 * Created by Majer on 22. 09. 2016. Updated by Jan Keim in 2022
 */
public class WordMovers {

    private static final double DEFAULT_STOPWORD_WEIGHT = 0.5;

    private WordVectors wordVectors;
    private Set<String> stopwords;
    private double stopwordWeight;

    private EarthMovers earthMovers;

    private WordMovers(Builder builder) {
        this.wordVectors = builder.wordVectors;
        this.stopwords = builder.stopwords;
        this.stopwordWeight = builder.stopwordWeight;
        this.earthMovers = new EarthMovers();
    }

    public double distance(String a, String b) {

        if (a.isBlank() || b.isBlank()) {
            throw new IllegalArgumentException();
        }

        return distance(a.split(" "), b.split(" "));
    }

    public double distance(String[] tokensA, String[] tokensB) {

        if (tokensA.length < 1 || tokensB.length < 1) {
            throw new IllegalArgumentException();
        }

        var mapA = bagOfVectors(tokensA);
        var mapB = bagOfVectors(tokensB);

        if (mapA.isEmpty() || mapB.isEmpty()) {
            throw new NoSuchElementException(
                    "Can't find any word vectors for given input text ..." + Arrays.toString(tokensA) + "|" + Arrays.toString(tokensB));
        }
        // vocabulary of current tokens
        var vocab = Stream.of(mapA.keySet(), mapB.keySet()).flatMap(Collection::stream).distinct().toList();
        var matrix = new double[vocab.size()][vocab.size()];

        for (var i = 0; i < matrix.length; i++) {
            var tokenA = vocab.get(i);
            for (var j = 0; j < matrix.length; j++) {
                var tokenB = vocab.get(j);
                if (mapsContainTokens(mapA, mapB, tokenA, tokenB)) {
                    var tokenAVector = mapA.get(tokenA).getVector();
                    var tokenBVector = mapB.get(tokenB).getVector();
                    var distance = tokenAVector.distance2(tokenBVector);
                    // if tokenA and tokenB are stopwords, calculate distance according to stopword weight
                    distance *= getWeightForTokens(tokenA, tokenB);
                    matrix[i][j] = distance;
                    matrix[j][i] = distance;
                }
            }
        }

        var freqA = frequencies(vocab, mapA);
        var freqB = frequencies(vocab, mapB);

        return earthMovers.distance(freqA, freqB, matrix, 0);
    }

    private double getWeightForTokens(String tokenA, String tokenB) {
        if (stopwords != null && tokenA.length() != 1 && tokenB.length() != 1) {
            return stopwords.contains(tokenA) || stopwords.contains(tokenB) ? stopwordWeight : 1;
        }
        return 1;
    }

    private boolean mapsContainTokens(Map<String, FrequencyVector> mapA, Map<String, FrequencyVector> mapB, String tokenA, String tokenB) {
        return mapA.containsKey(tokenA) && mapB.containsKey(tokenB);
    }

    private Map<String, FrequencyVector> bagOfVectors(String[] tokens) {

        Map<String, FrequencyVector> map = new LinkedHashMap<>(tokens.length);
        Arrays.stream(tokens).filter(x -> wordVectors.hasWord(x)).forEach(x -> map.merge(x, new FrequencyVector(wordVectors.getWordVectorMatrix(x)), (v, o) -> {
            v.incrementFrequency();
            return v;
        }));

        return map;
    }

    /*
     * Normalized frequencies for vocab
     */
    private double[] frequencies(List<String> vocab, Map<String, FrequencyVector> map) {
        return vocab.stream().mapToDouble(x -> {
            if (map.containsKey(x)) {
                return (double) map.get(x).getFrequency() / map.size();
            }
            return 0d;
        }).toArray();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private WordVectors wordVectors;
        private Set<String> stopwords;

        private double stopwordWeight = DEFAULT_STOPWORD_WEIGHT;

        private Builder() {
        }

        public WordMovers build() {
            return new WordMovers(this);
        }

        public Builder wordVectors(WordVectors wordVectors) {
            this.wordVectors = wordVectors;
            return this;
        }

        public Builder stopwords(Set<String> stopwords) {
            this.stopwords = stopwords;
            return this;
        }

        public Builder stopwordWeight(double stopwordWeight) {
            this.stopwordWeight = stopwordWeight;
            return this;
        }

    }
}
