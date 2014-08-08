package de.tuberlin.dima.schubotz.fse;

import de.tuberlin.dima.schubotz.common.utils.SafeLogWrapper;
import de.tuberlin.dima.schubotz.fse.modules.algorithms.Algorithm;
import de.tuberlin.dima.schubotz.fse.client.ClientConsole;
import de.tuberlin.dima.schubotz.fse.modules.inputs.Input;
import de.tuberlin.dima.schubotz.fse.modules.output.Output;
import de.tuberlin.dima.schubotz.fse.settings.SettingNames;
import de.tuberlin.dima.schubotz.fse.settings.Settings;
import eu.stratosphere.api.java.ExecutionEnvironment;


import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Performs the queries for the NTCIR-Math11-Workshop 2014 fully automated.
 */

public class MainProgram {
	/**
	 * Main execution environment for Stratosphere.
	 */
	private static ExecutionEnvironment env;
	/**
	 * Log for this class. Leave all logging implementations up to
	 * Stratosphere and its config files.
	 */
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


	public static void main (String[] args) throws Exception {
        final Algorithm algorithm = ClientConsole.parseParameters(args);
        final Collection<Class> modulesToExecute = new ArrayList<>();
        configureEnv();

        if (algorithm != null) {
            final Class inputClass = getClass(
                    Settings.getProperty(SettingNames.INPUT_OPTION),getParentPackageName(Input.class));
            if (Input.class.isAssignableFrom(inputClass)) {
                final Constructor<? extends Input> inputConstructor = Input.class.cast(inputClass).getClass()
                        .getConstructor();
                final Input inputObj = inputConstructor.newInstance();
                inputObj.configure(env);
            } else {
                throw new IllegalArgumentException(
                        "Input class: " + Settings.getProperty(SettingNames.INPUT_OPTION) + "could not be found.");
            }
            for (final Class clazz : algorithm.getRequiredInputsAsIterable()) {
                if (Input.class.isAssignableFrom(clazz)) {
                    final Constructor<? extends Input> addInputConstructor = Input.class.cast(clazz).getClass()
                            .getConstructor();
                    final Input addInputObj = addInputConstructor.newInstance();
                    addInputObj.configure(env);
                } else{
                    throw new IllegalArgumentException(
                            "Programmer required input class: " + clazz.getSimpleName() + " could not be found.");
                }
            }

            algorithm.configure(env);

            final Class outputClass = getClass(
                    Settings.getProperty(SettingNames.OUTPUT_OPTION),getParentPackageName(Output.class));
            if (Output.class.isAssignableFrom(outputClass)) {
                final Constructor<? extends Output> outputConstructor = Output.class.cast(outputClass).getClass()
                        .getConstructor();
                final Output outputObj = outputConstructor.newInstance();
                outputObj.configure(env);
            }
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
     * Gets class to execute based on name.
     * @param inputName name of class
     * @param packageName simple name of parent package to search for (e.g. ...fse.inputs.Input
     *                    has package name of "inputs")
     * @return class
     * @throws IllegalArgumentException if unable to find class
     */
    public static Class getClass(String inputName, String packageName) throws IllegalArgumentException {
        final String inputClassname = MainProgram.class.getClass().getPackage().getName() +
                '.' + packageName + '.' + inputName;
        try {
            return Class.forName(inputClassname);
        } catch (final ClassNotFoundException ignore) {
            throw new IllegalArgumentException ("Unable to find class: " + inputName);
        }
    }

    /**
     * Get parent package name based on class. (E.g. Input.class would return "inputs")
     * @param clazz class name
     * @return parent package name
     */
    public static String getParentPackageName(Class clazz) {
        final String[] split = clazz.getPackage().getName().split(".");
        return split[split.length-1];
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

