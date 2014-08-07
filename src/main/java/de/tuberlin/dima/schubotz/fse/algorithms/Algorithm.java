package de.tuberlin.dima.schubotz.fse.algorithms;

import eu.stratosphere.api.java.ExecutionEnvironment;

/**
 * Defines main method in algorithms.
 * Created by jjl4 on 8/7/14.
 */
public interface Algorithm {
    /**
     * Configures plan.
     */
    void configure(ExecutionEnvironment env);
}
