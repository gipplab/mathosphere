package com.formulasearchengine.mathosphere.mathpd.distances.earthmover;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Felix Hamborg on 13.12.16.
 */
public class EarthMoverDistanceWrapper {
    public static Signature histogramToSignature(Map<String, Integer> histogram) {
        Signature signature = new Signature();
        Feature[] features = new Feature[histogram.size()];
        double[] weights = new double[histogram.size()];
        List<String> orderedKeys = new ArrayList<>(histogram.keySet());

        for (int i = 0; i < histogram.size(); i++) {
            features[i] = new Feature2D(i, histogram.get(orderedKeys.get(i)));
            weights[i] = 1.0;
        }

        signature.setFeatures(features);
        signature.setWeights(weights);
        signature.setNumberOfFeatures(features.length);

        return signature;
    }


}
