package de.tuberlin.dima.schubotz.fse.modules;

import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import eu.stratosphere.api.java.ExecutionEnvironment;
import org.apache.commons.cli.Option;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

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
    public void configure(ExecutionEnvironment env, DataStorage data);

}

