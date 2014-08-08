package de.tuberlin.dima.schubotz.fse.modules.output;

import de.tuberlin.dima.schubotz.fse.modules.Module;
import eu.stratosphere.api.java.ExecutionEnvironment;
import org.apache.commons.cli.Option;

import java.util.Collection;

/**
 * Created by jjl4 on 8/8/14.
 */
public interface Output extends Module {
     /**
     * Configures output.
     * @param env ExecutionEnvironment
     */
    void configure(ExecutionEnvironment env);

    /**
     * Gets options for command line.
     * @return options
     */
    Collection<Option> getOptionsAsIterable();
}
