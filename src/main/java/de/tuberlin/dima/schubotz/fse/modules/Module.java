package de.tuberlin.dima.schubotz.fse.modules;

import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import eu.stratosphere.api.java.ExecutionEnvironment;
import org.apache.commons.cli.Option;

import java.util.Collection;
import java.util.ServiceLoader;

/**
 * Created by jjl4 on 8/8/14.
 */
public abstract class Module {
    /**
     * Gets options for command line.
     * @return options
     */
    public abstract Collection<Option> getOptionsAsIterable();
    /**
     * Configures environment.
     * @param env ExecutionEnvironment
     */
    public abstract void configure(ExecutionEnvironment env, DataStorage data);

    private static final ServiceLoader<Module> moduleLoader;
    static {
        moduleLoader = ServiceLoader.load(Module.class);
    }

    /**
     * Gets module to execute, given expected subclass and its name.
     * Guaranteed to return class of expected subclass that extends
     * module if no exception is thrown
     * @param moduleName name of module
     * @param expectedClass module subclass expected
     * @return class
     * @throws IllegalArgumentException if unable to find class
     * @throws ClassCastException if unable to cast to expectedClass
     */
    public static <T extends Module> T getModule(String moduleName, Class<T> expectedClass)
        throws IllegalArgumentException, ClassCastException {
        for (final Module module : moduleLoader) {
            if (module.getClass().getName().equals(moduleName)) {
                return expectedClass.cast(module);
            }
        }
        throw new IllegalArgumentException("Unable to find module: " + moduleName);
    }
}

