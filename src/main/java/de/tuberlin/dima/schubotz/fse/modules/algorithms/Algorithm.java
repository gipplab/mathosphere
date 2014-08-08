package de.tuberlin.dima.schubotz.fse.modules.algorithms;

import de.tuberlin.dima.schubotz.fse.modules.Module;
import eu.stratosphere.api.java.ExecutionEnvironment;

import java.util.Collection;

/**
 * Defines main method in algorithms.
 * Created by jjl4 on 8/7/14.
 */
public interface Algorithm extends Module {
    /**
     * Configures algorithm.
     * @param env ExecutionEnvironment
     */
    void configure(ExecutionEnvironment env);



    /**
     * Gets required classes
     * @return classes
     */
    Collection<Class> getRequiredInputsAsIterable();
}
