package de.tuberlin.dima.schubotz.fse.modules;

import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import eu.stratosphere.api.java.ExecutionEnvironment;
import org.apache.commons.cli.Option;

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
public abstract class Module {
    private static final Pattern PACKAGE = Pattern.compile("\\.");

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

        final ClassPathScanningCandidateComponentProvider provider = new
                ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(expectedClass));

        // scan in modules package, replace dots with slashes
        final Set<BeanDefinition> components = provider.findCandidateComponents(
                PACKAGE.matcher(Module.class.getPackage().getName()).replaceAll("/"));
        try {
            for (final BeanDefinition component : components) {
                final Class cls = Class.forName(component.getBeanClassName());
                if (cls.getSimpleName().equals(moduleName)) {
                    return expectedClass.cast(getObjectFromGenericClass(cls));
                }
            }
        } catch (final ClassNotFoundException e) {
            throw new IllegalArgumentException("Unable to find module: " + moduleName);
        }
        throw new IllegalArgumentException("Unable to find module: " + moduleName);
    }

    /**
     * Constructs object instance from a generic class.
     * Always guaranteed to work if it does not throw an exception.
     * @param clazz generic class
     * @return object of specific class. throws exception rather than returning null
     * @throws IllegalArgumentException if for any reason unable to create the object
     */
    private static <T> T getObjectFromGenericClass(Class<T> clazz)
            throws IllegalArgumentException {
        try {
            final Constructor<T> objectConstructor = clazz.getConstructor();
            return objectConstructor.newInstance();
        } catch (final NoSuchMethodException ignore) {
            throw new IllegalArgumentException("Unable to find constructor for class: " + clazz.getName());
        } catch (final InstantiationException ignore) {
            throw new IllegalArgumentException("Unable to instantiate class: " + clazz.getName());
        } catch (final InvocationTargetException ignore) {
            throw new IllegalArgumentException("Unable to invoke class: " + clazz.getName());
        } catch (final IllegalAccessException ignore) {
            throw new IllegalArgumentException("Unable to access class: " + clazz.getName() + ", is it public?");
        }
    }
}

