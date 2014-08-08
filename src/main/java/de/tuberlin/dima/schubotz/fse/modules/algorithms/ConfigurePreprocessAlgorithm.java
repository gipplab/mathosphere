package de.tuberlin.dima.schubotz.fse.modules.algorithms;

import eu.stratosphere.api.java.ExecutionEnvironment;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by jjl4 on 8/8/14.
 */
public class ConfigurePreprocessAlgorithm implements Algorithm {
    public void configure (ExecutionEnvironment env) {

    }
    public Collection getOptionsAsIterable() {
        return Collections.emptyList();
    }
}
