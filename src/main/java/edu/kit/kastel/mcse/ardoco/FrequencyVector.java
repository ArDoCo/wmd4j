/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco;

import java.util.concurrent.atomic.AtomicLong;

import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Created by Majer on 21.9.2016.
 */
public class FrequencyVector {

    private AtomicLong frequency;
    private INDArray vector;

    public FrequencyVector(INDArray vector) {
        this(1, vector);
    }

    public FrequencyVector(long frequency, INDArray vector) {
        this.frequency = new AtomicLong(frequency);
        this.vector = vector;
    }

    public void incrementFrequency() {
        frequency.incrementAndGet();
    }

    public long getFrequency() {
        return frequency.get();
    }

    public INDArray getVector() {
        return vector;
    }
}
