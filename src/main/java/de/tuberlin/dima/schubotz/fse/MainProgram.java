package de.tuberlin.dima.schubotz.fse;

import de.tuberlin.dima.schubotz.common.utils.SafeLogWrapper;
import de.tuberlin.dima.schubotz.fse.algorithms.Algorithm;
import de.tuberlin.dima.schubotz.fse.client.ClientConsole;
import eu.stratosphere.api.java.ExecutionEnvironment;


import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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
        ClientConsole cli = new ClientConsole();
        Settings settings = new Settings();
        final boolean parsed = cli.parseParameters(args, settings);
        if (parsed) {
            configureEnv();
            configurePlan();
            env.execute("Mathosphere");
        }
	}
    /**
     * Configure ExecutionEnvironment
     */
    private static void configureEnv() {
        env = ExecutionEnvironment.getExecutionEnvironment();
        env.setDegreeOfParallelism(Integer.parseInt(Settings.getProperty("numSubTasks")));
    }

    /**
     * Configure ExecutionEnvironment
     */
    private static void configurePlan() {
        final String planName = Settings.getProperty("main");
        final String planClassname = MainProgram.class.getClass().getPackage().getName() + ".algorithms." + planName;

        try {
            final Class<?> planClass = Class.forName(planClassname);

            final Class planInterface = Algorithm.class;
            final ClassLoader classLoader = planInterface.getClassLoader();
            final Class<?>[] interfaces = new Class<?>[] {planInterface};
            //Construct handler containing algorithm to configure
            final InvocationHandler handler = new PlanInvocationHandler(
                    (Algorithm) planClass.newInstance());
            //Construct proxy class to run configure method
            final Algorithm algorithmToConfigure =
                    (Algorithm) Proxy.newProxyInstance(classLoader, interfaces, handler);

            algorithmToConfigure.configure(env);

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
    private static class PlanInvocationHandler implements InvocationHandler {
        private final Algorithm algorithm;
        protected PlanInvocationHandler(Algorithm algorithm) {
            this.algorithm = algorithm;
        }
        /**
         * Invoke given method on algorithm this handler was constructed with.
         * @param obj ignored (required by interface)
         * @param method method to execute
         * @param args arguments for method
         * @return method return
         */
        @Override
        public Object invoke(Object obj, Method method, Object[] args)
                throws InvocationTargetException, IllegalArgumentException, IllegalAccessException {
            return method.invoke(this.algorithm, args);
        }
    }
}

