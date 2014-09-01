package de.tuberlin.dima.schubotz.fse;

import de.tuberlin.dima.schubotz.fse.client.ClientConsole;
import de.tuberlin.dima.schubotz.fse.modules.Module;
import de.tuberlin.dima.schubotz.fse.modules.algorithms.Algorithm;
import de.tuberlin.dima.schubotz.fse.modules.inputs.Input;
import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import de.tuberlin.dima.schubotz.fse.settings.SettingNames;
import de.tuberlin.dima.schubotz.fse.settings.Settings;
import de.tuberlin.dima.schubotz.fse.utils.SafeLogWrapper;
import eu.stratosphere.api.java.ExecutionEnvironment;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Pattern;

/**
 * Performs the queries for the NTCIR-Math11-Workshop 2014 fully automated.
 */

public class MainProgram {
    /**
	 * Main execution environment for Stratosphere.
	 */
	private static ExecutionEnvironment env;
	private static final SafeLogWrapper LOG = new SafeLogWrapper(MainProgram.class);
    /**
     * Used for line splitting so that CsvReader is not looking for "\n" in XML
     */
    public static final String CSV_LINE_SEPARATOR = "\u001D";
    /**
     * Used for field splitting so that CsvReader doesn't get messed up on comma latex tokens
     */
    public static final String CSV_FIELD_SEPARATOR = "\u001E";
	/**
	 * Delimiter used in between Tex and Keyword tokens
	 */
	public static final String STR_SEPARATOR = "\u001F";
    /**
	 * Pattern which will return word tokens
	 */
	public static final Pattern WORD_SPLIT = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS);

    private MainProgram() {
    }


    public static void main (String[] args) throws Exception {
        //Turn off debugging for now
        LOG.setLevel(SafeLogWrapper.SafeLogWrapperLevel.INFO);

        final boolean parsed = ClientConsole.parseParameters(args);

        if (parsed) {
            configureEnv();

            final DataStorage data = new DataStorage();

            //Run input module specified by command line
            final Input inputModule = Module.getModule(
                    Settings.getProperty(SettingNames.INPUT), Input.class);
            inputModule.configure(env, data);

            //Run algorith module specified by command line
            final Algorithm algoModule = Module.getModule(
                    Settings.getProperty(SettingNames.ALGORITHM), Algorithm.class);
            algoModule.configure(env, data);

            /* Trust user to run input module for now
            //Run input modules required by algorithm
            for (final Class clazz : algorithm.getRequiredInputsAsIterable()) {
                final Module addInputObj = (Module) getObjectFromGenericClass(clazz, Input.class);
                addInputObj.configure(env, data);
            }*/


            /* Algorithms are tied to output (preprocess, etc.)
            final Class outputClass = getClass(
                    Settings.getProperty(SettingNames.OUTPUT_OPTION),Output.class.getPackage().getName());
            final Module outputObj = (Module) getObjectFromGenericClass(outputClass, Output.class);
            outputObj.configure(env, data);
            */

            env.execute("Mathosphere");
        }
	}
    /**
     * Configure ExecutionEnvironment
     */
    private static void configureEnv() {
        env = ExecutionEnvironment.getExecutionEnvironment();
        env.setDegreeOfParallelism(Integer.parseInt(Settings.getProperty(SettingNames.NUM_SUB_TASKS)));
    }

    /**
     * Gets module to execute, given expected superclass and its name.
     * Guaranteed to return class that extends expected class
     * if no exception is thrown
     * @param className name of class
     * @param expectedClass superclass expected
     * @return class
     * @throws IllegalArgumentException if unable to find class
     */
    /*
    public static Class getSubClass(String className, Class<?> expectedClass) throws IllegalArgumentException {
        try {
            final String packageName = expectedClass.getPackage().getName();
            final String fullName = packageName + '.' + className;
            final Class returnedClass = Class.forName(fullName);
            if (expectedClass.isAssignableFrom(returnedClass)) {
                return Class.forName(packageName);
            } else {
                throw new ClassNotFoundException();
            }
        } catch (final ClassNotFoundException ignore) {
            throw new IllegalArgumentException ("Unable to find class: " + className + " that was a subclass of " +
                    expectedClass.getName());
        }
    }


    /**
     * Gets the algorithm to execute based on name. If it doesn't exist, throw exceptions.
     */
    /*
    public static Algorithm getAlgorithm(String planName) throws IllegalArgumentException {
        final String planClassname = MainProgram.class.getClass().getPackage().getName() + ".algorithms." + planName;

        try {
            final Class<?> planClass = Class.forName(planClassname);

            final Class planInterface = Algorithm.class;
            final ClassLoader classLoader = planInterface.getClassLoader();
            final Class<?>[] interfaces = new Class<?>[] {planInterface};
            //Construct handler containing algorithm to configure
            final InvocationHandler handler = new PlanInvocationHandler(
                    (Algorithm) planClass.newInstance());
            //Construct proxy class to run configure method, return it
            return (Algorithm) Proxy.newProxyInstance(classLoader, interfaces, handler);
        } catch (final ClassNotFoundException ignore) {
            throw new IllegalArgumentException ("Unable to find algorithm: " + planName);
        } catch (final InstantiationException ignore) {
            throw new IllegalArgumentException ("Unable to instantiate algorithm: " + planName);
        } catch (final IllegalAccessException ignore) {
            throw new IllegalArgumentException ("Unable to access algorithm: " + planName + ", is it public?");
        }
    }

    /**
     * Handler to invoke methods on a Algorithm object
     */
        /*
    private static class PlanInvocationHandler implements InvocationHandler {
        private final Object object;
        protected PlanInvocationHandler(Object object) {
            this.object = object;
        }
        /**
         * Invoke given method on algorithm this handler was constructed with.
         * @param obj ignored (required by interface)
         * @param method method to execute
         * @param args arguments for method
         * @return method return
         */
    /*
        @Override
        public Object invoke(Object obj, Method method, Object[] args)
                throws InvocationTargetException, IllegalArgumentException, IllegalAccessException {
            return method.invoke(this.object, args);
        }
    }
    */
}

