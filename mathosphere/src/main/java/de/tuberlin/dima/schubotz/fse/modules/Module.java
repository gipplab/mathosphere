package de.tuberlin.dima.schubotz.fse.modules;

import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import org.apache.commons.cli.Option;
import org.apache.flink.api.java.ExecutionEnvironment;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by jjl4 on 8/8/14.
 */
public interface Module extends Serializable {
    /**
     * Gets options for command line.
     * @return options
     */
    public Collection<Option> getOptionsAsIterable();
    /**
     * Configures environment.
     * @param env ExecutionEnvironment
     */
    public void configure(ExecutionEnvironment env, DataStorage data) throws Exception;

}

