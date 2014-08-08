package de.tuberlin.dima.schubotz.fse.modules;

import org.apache.commons.cli.Option;

import java.util.Collection;

/**
 * Created by jjl4 on 8/8/14.
 */
public interface Module {
    /**
     * Gets options for command line.
     * @return options
     */
    Collection<Option> getOptionsAsIterable();
}
